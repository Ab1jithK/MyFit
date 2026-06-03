package com.myfit.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myfit.ble.PhoneSyncStatus
import com.myfit.core.data.db.entity.SyncEventEntity
import com.myfit.core.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncLogScreen(
    onBack: () -> Unit,
    vm: SyncLogViewModel = hiltViewModel()
) {
    val events    by vm.events.collectAsStateWithLifecycle()
    val bleStatus by vm.bleSyncStatus.collectAsStateWithLifecycle()
    val lastSync  by vm.lastSyncLabel.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Black,
        topBar = {
            TopAppBar(
                title = { Text("SYNC LOG", style = MaterialTheme.typography.labelLarge, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface1)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(12.dp))
            SyncStatusHeader(bleStatus, lastSync, vm::startScan)
            Spacer(Modifier.height(16.dp))

            if (events.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No sync history yet", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(events, key = { it.id }) { event ->
                        SyncEventRow(event)
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncStatusHeader(
    status: PhoneSyncStatus,
    lastSync: String,
    onConnect: () -> Unit
) {
    val (dotColor, label) = when (status) {
        PhoneSyncStatus.Done       -> Performance to lastSync
        PhoneSyncStatus.Syncing    -> Active      to "Syncing…"
        PhoneSyncStatus.Connecting -> Caution     to "Connecting…"
        PhoneSyncStatus.Scanning   -> Caution     to "Scanning for watch…"
        PhoneSyncStatus.Error      -> Warning     to "Sync error"
        PhoneSyncStatus.Idle       -> TextDisabled to lastSync
    }
    val showConnect = status == PhoneSyncStatus.Idle || status == PhoneSyncStatus.Error

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface1, RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(Modifier.size(10.dp).background(dotColor, CircleShape))
        Text(label, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, modifier = Modifier.weight(1f))
        if (showConnect) {
            Button(
                onClick = onConnect,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Active)
            ) {
                Text("CONNECT", style = MaterialTheme.typography.labelSmall, color = Black)
            }
        }
    }
}

@Composable
private fun SyncEventRow(event: SyncEventEntity) {
    val deviceLabel = if (event.device == "phone") "PHONE" else "WATCH"
    val deviceColor = if (event.device == "phone") Active else Performance
    val typeLabel   = when (event.eventType) {
        "set_recorded"  -> "Set logged"
        "session_start" -> "Session started"
        "session_end"   -> "Session ended"
        else            -> event.eventType
    }
    val statusColor = when (event.syncStatus) {
        "merged"   -> Performance
        "pending"  -> Caution
        "conflict" -> Warning
        else       -> TextDisabled
    }
    val timeLabel = remember(event.recordedAt) {
        SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(event.recordedAt))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Surface1, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(deviceLabel, style = MaterialTheme.typography.labelSmall, color = deviceColor, modifier = Modifier.width(44.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(typeLabel, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
            Text(event.entityId.take(8) + "…", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(timeLabel, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Box(Modifier.size(6.dp).background(statusColor, CircleShape).align(Alignment.End))
        }
    }
}
