package com.myfit.feature.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myfit.ble.PhoneBleManager
import com.myfit.ble.PhoneSyncStatus
import com.myfit.core.data.db.dao.SyncEventDao
import com.myfit.core.data.db.entity.SyncEventEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class SyncLogViewModel @Inject constructor(
    private val syncEventDao: SyncEventDao,
    private val bleManager: PhoneBleManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val events: StateFlow<List<SyncEventEntity>> = syncEventDao.getAllSyncEvents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val bleSyncStatus: StateFlow<PhoneSyncStatus> = bleManager.syncStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PhoneSyncStatus.Idle)

    val lastSyncLabel: StateFlow<String> = bleManager.syncStatus
        .map { readSyncLabel() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), readSyncLabel())

    private fun readSyncLabel(): String {
        val prefs = context.getSharedPreferences("myfit_prefs", Context.MODE_PRIVATE)
        val lastMs = prefs.getLong("last_sync_ms", 0L)
        if (lastMs == 0L) return "Never synced"
        val diff = System.currentTimeMillis() - lastMs
        return when {
            diff < 60_000     -> "Synced just now"
            diff < 3_600_000  -> "Synced ${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
            diff < 86_400_000 -> "Synced ${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
            else              -> "Synced ${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
        }
    }

    fun startScan() = bleManager.startScanning()
}
