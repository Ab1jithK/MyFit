package com.myfit.ble

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.SharedPreferences
import com.myfit.core.data.db.dao.SetDao
import com.myfit.core.data.db.dao.WorkoutDao
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class PhoneSyncStatus { Idle, Scanning, Connecting, Syncing, Done, Error }

@Singleton
class PhoneBleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workoutDao: WorkoutDao,
    private val setDao: SetDao,
    private val syncRepository: SyncRepository
) {
    private val _syncStatus = MutableStateFlow(PhoneSyncStatus.Idle)
    val syncStatus = _syncStatus.asStateFlow()

    // A malformed payload from a peer must surface as a sync error, not crash the app
    private val scope = CoroutineScope(
        Dispatchers.IO + SupervisorJob() + CoroutineExceptionHandler { _, _ -> _syncStatus.value = PhoneSyncStatus.Error }
    )
    private val btManager = context.getSystemService(BluetoothManager::class.java)
    private val btAdapter = btManager?.adapter
    private val prefs: SharedPreferences =
        context.getSharedPreferences(BleConstants.PREFS_NAME, Context.MODE_PRIVATE)

    private var activeGatt: BluetoothGatt? = null
    private val assembler = ChunkAssembler()

    private val hasPairedWatch get() = prefs.getBoolean(BleConstants.KEY_WATCH_PAIRED, false)

    private fun isBonded(device: BluetoothDevice) = device.bondState == BluetoothDevice.BOND_BONDED

    /** Lets the phone pair with a different watch on the next scan (unpair the old one in system settings too). */
    fun forgetWatch() {
        prefs.edit().remove(BleConstants.KEY_WATCH_PAIRED).apply()
    }

    private fun fail(gatt: BluetoothGatt) {
        _syncStatus.value = PhoneSyncStatus.Error
        gatt.disconnect()
    }

    fun startScanning() {
        if (btAdapter?.isEnabled != true) return
        val filter = ScanFilter.Builder()
            .setServiceUuid(android.os.ParcelUuid(BleConstants.SERVICE_UUID)).build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
        btAdapter.bluetoothLeScanner?.startScan(listOf(filter), settings, scanCallback)
        _syncStatus.value = PhoneSyncStatus.Scanning
    }

    fun stopScanning() {
        btAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
        if (_syncStatus.value == PhoneSyncStatus.Scanning) _syncStatus.value = PhoneSyncStatus.Idle
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // Once a watch is paired, ignore unknown advertisers rather than prompting to pair with them
            if (hasPairedWatch && !isBonded(result.device)) return
            stopScanning()
            _syncStatus.value = PhoneSyncStatus.Connecting
            result.device.connectGatt(context, false, gattCallback)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                activeGatt = gatt
                gatt.requestMtu(512)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gatt.close(); activeGatt = null
                assembler.reset()
                if (_syncStatus.value == PhoneSyncStatus.Syncing) _syncStatus.value = PhoneSyncStatus.Done
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            gatt.discoverServices()
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) return fail(gatt)
            _syncStatus.value = PhoneSyncStatus.Syncing
            val dataChar = gatt.getService(BleConstants.SERVICE_UUID)
                ?.getCharacteristic(BleConstants.SYNC_DATA_UUID) ?: return fail(gatt)
            val desc = dataChar.getDescriptor(BleConstants.CLIENT_CONFIG_UUID) ?: return fail(gatt)
            gatt.setCharacteristicNotification(dataChar, true)
            // The watch requires encryption here, so this write triggers the system pairing prompt on first sync
            desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(desc)
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int
        ) {
            if (status != BluetoothGatt.GATT_SUCCESS || !isBonded(gatt.device)) return fail(gatt)
            prefs.edit().putBoolean(BleConstants.KEY_WATCH_PAIRED, true).apply()
            val syncReq = gatt.getService(BleConstants.SERVICE_UUID)
                ?.getCharacteristic(BleConstants.SYNC_REQUEST_UUID) ?: return
            gatt.readCharacteristic(syncReq)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int
        ) {
            if (characteristic.uuid == BleConstants.SYNC_REQUEST_UUID && status == BluetoothGatt.GATT_SUCCESS) {
                val watchLastSync = try {
                    JSONObject(characteristic.value.toString(Charsets.UTF_8)).optLong("since", 0L)
                } catch (e: Exception) { 0L }
                scope.launch { sendPhoneData(gatt, watchLastSync) }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == BleConstants.SYNC_DATA_UUID && isBonded(gatt.device)) {
                handleIncomingChunk(characteristic.value)
            }
        }
    }

    private fun handleIncomingChunk(raw: ByteArray) {
        val json = assembler.add(raw) ?: return
        scope.launch {
            syncRepository.mergeIncoming(SyncPayload.deserialize(json))
            sendAck(activeGatt ?: return@launch)
            prefs.edit().putLong(BleConstants.KEY_LAST_SYNC, System.currentTimeMillis()).apply()
            _syncStatus.value = PhoneSyncStatus.Done
        }
    }

    private suspend fun sendPhoneData(gatt: BluetoothGatt, watchLastSync: Long) {
        val phoneLastSync = prefs.getLong(BleConstants.KEY_LAST_SYNC, 0L)
        val since         = minOf(watchLastSync, phoneLastSync)
        val sessions      = workoutDao.getAllSessionsSince(since)
        val sets          = setDao.getAllSetsSince(since)
        val json          = SyncPayload.serialize(SyncData(sessions, sets))
        val phoneToWatch  = gatt.getService(BleConstants.SERVICE_UUID)
            ?.getCharacteristic(BleConstants.PHONE_TO_WATCH_UUID) ?: return
        SyncPayload.chunk(json).forEach { chunk ->
            phoneToWatch.value = chunk
            gatt.writeCharacteristic(phoneToWatch)
            kotlinx.coroutines.delay(30)
        }
    }

    private fun sendAck(gatt: BluetoothGatt) {
        val ackChar = gatt.getService(BleConstants.SERVICE_UUID)
            ?.getCharacteristic(BleConstants.SYNC_ACK_UUID) ?: return
        ackChar.value = """{"received":1}""".toByteArray()
        gatt.writeCharacteristic(ackChar)
    }
}
