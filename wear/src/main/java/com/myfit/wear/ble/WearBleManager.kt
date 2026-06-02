package com.myfit.wear.ble

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.SharedPreferences
import com.myfit.wear.data.dao.WearSyncDao
import com.myfit.wear.data.dao.WearWorkoutDao
import com.myfit.wear.data.entity.SyncEventEntity
import dagger.hilt.android.qualifiers.ApplicationContext
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

enum class SyncStatus { Idle, Advertising, Syncing, Done, Error }

@Singleton
class WearBleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workoutDao: WearWorkoutDao,
    private val syncDao: WearSyncDao
) {
    private val scope     = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val btManager = context.getSystemService(BluetoothManager::class.java)
    private val btAdapter = btManager?.adapter
    private val prefs: SharedPreferences =
        context.getSharedPreferences(BleConstants.PREFS_NAME, Context.MODE_PRIVATE)

    private val _syncStatus = MutableStateFlow(SyncStatus.Idle)
    val syncStatus = _syncStatus.asStateFlow()

    private var gattServer: BluetoothGattServer? = null
    private val incomingChunks = mutableListOf<ByteArray>()
    private var expectedChunkCount = -1

    fun startAdvertising() {
        if (btAdapter?.isEnabled != true) return
        setupGattServer()
        val advertiser = btAdapter.bluetoothLeAdvertiser ?: return
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setConnectable(true).setTimeout(0).build()
        val data = AdvertiseData.Builder()
            .addServiceUuid(android.os.ParcelUuid(BleConstants.SERVICE_UUID))
            .setIncludeDeviceName(false).build()
        advertiser.startAdvertising(settings, data, advertiseCallback)
        _syncStatus.value = SyncStatus.Advertising
    }

    fun stopAdvertising() {
        btAdapter?.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        gattServer?.close(); gattServer = null
        _syncStatus.value = SyncStatus.Idle
    }

    private fun setupGattServer() {
        val service = BluetoothGattService(BleConstants.SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        service.addCharacteristic(BluetoothGattCharacteristic(
            BleConstants.SYNC_REQUEST_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        ))

        val dataChar = BluetoothGattCharacteristic(
            BleConstants.SYNC_DATA_UUID,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        dataChar.addDescriptor(BluetoothGattDescriptor(
            BleConstants.CLIENT_CONFIG_UUID,
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        ))
        service.addCharacteristic(dataChar)

        service.addCharacteristic(BluetoothGattCharacteristic(
            BleConstants.SYNC_ACK_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        ))

        service.addCharacteristic(BluetoothGattCharacteristic(
            BleConstants.PHONE_TO_WATCH_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        ))

        gattServer = btManager?.openGattServer(context, gattServerCallback)
        gattServer?.addService(service)
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                _syncStatus.value = SyncStatus.Syncing
                sendWatchData(device)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                if (_syncStatus.value == SyncStatus.Syncing) _syncStatus.value = SyncStatus.Done
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice, requestId: Int, offset: Int,
            characteristic: BluetoothGattCharacteristic
        ) {
            if (characteristic.uuid == BleConstants.SYNC_REQUEST_UUID) {
                val lastSync = prefs.getLong(BleConstants.KEY_LAST_SYNC, 0L)
                val payload  = JSONObject().put("since", lastSync).toString().toByteArray()
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, payload)
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice, requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray
        ) {
            when (characteristic.uuid) {
                BleConstants.PHONE_TO_WATCH_UUID -> handleIncomingChunk(value)
                BleConstants.SYNC_ACK_UUID -> {
                    prefs.edit().putLong(BleConstants.KEY_LAST_SYNC, System.currentTimeMillis()).apply()
                    _syncStatus.value = SyncStatus.Done
                }
            }
            if (responseNeeded)
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice, requestId: Int,
            descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray
        ) {
            if (responseNeeded)
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
        }
    }

    private fun handleIncomingChunk(raw: ByteArray) {
        val prefix = raw.copyOf(minOf(raw.size, 20)).toString(Charsets.UTF_8)
        val sep1   = prefix.indexOf(':')
        val sep2   = prefix.indexOf(':', sep1 + 1)
        if (sep1 < 0 || sep2 < 0) return
        val idx   = prefix.substring(0, sep1).toIntOrNull() ?: return
        val total = prefix.substring(sep1 + 1, sep2).toIntOrNull() ?: return
        val data  = raw.sliceArray((sep2 + 1) until raw.size)

        if (idx == 0) { incomingChunks.clear(); expectedChunkCount = total }
        incomingChunks.add(data)

        if (incomingChunks.size == expectedChunkCount) {
            val json = incomingChunks.fold(byteArrayOf()) { acc, b -> acc + b }.toString(Charsets.UTF_8)
            scope.launch { mergeIncomingData(json) }
        }
    }

    private suspend fun mergeIncomingData(json: String) {
        val data = SyncPayload.deserialize(json)
        data.sessions.forEach { incoming ->
            val existing = syncDao.getSessionById(incoming.id)
            syncDao.upsertSession(incoming)
            syncDao.insertSyncEvent(SyncEventEntity(
                id = UUID.randomUUID().toString(), eventType = "session_start",
                entityId = incoming.id, recordedAt = incoming.startedAt,
                device = "phone", syncStatus = "merged",
                resolvedAt = if (existing != null) System.currentTimeMillis() else null
            ))
        }
        data.sets.forEach { incoming ->
            val existing = syncDao.getSetById(incoming.id)
            if (SyncPayload.shouldMergeSet(incoming.recordedAt, existing?.recordedAt)) {
                syncDao.upsertSet(incoming)
                syncDao.insertSyncEvent(SyncEventEntity(
                    id = UUID.randomUUID().toString(), eventType = "set_recorded",
                    entityId = incoming.id, recordedAt = incoming.recordedAt,
                    device = "phone", syncStatus = "merged",
                    resolvedAt = if (existing != null) System.currentTimeMillis() else null
                ))
            }
        }
    }

    private fun sendWatchData(device: BluetoothDevice) {
        scope.launch {
            val lastSync = prefs.getLong(BleConstants.KEY_LAST_SYNC, 0L)
            val sessions = workoutDao.getAllSessionsSince(lastSync)
            val sets     = workoutDao.getAllSetsSince(lastSync)
            val json     = SyncPayload.serialize(SyncData(sessions, sets))
            val chunks   = SyncPayload.chunk(json)
            val dataChar = gattServer?.getService(BleConstants.SERVICE_UUID)
                ?.getCharacteristic(BleConstants.SYNC_DATA_UUID) ?: return@launch
            chunks.forEach { chunk ->
                dataChar.value = chunk
                gattServer?.notifyCharacteristicChanged(device, dataChar, false)
                kotlinx.coroutines.delay(20)
            }
        }
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) { _syncStatus.value = SyncStatus.Error }
    }
}
