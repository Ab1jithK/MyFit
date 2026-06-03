package com.myfit.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.myfit.wear.ble.WearSyncService
import com.myfit.wear.navigation.WearNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WearMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.bluetooth.BluetoothAdapter.getDefaultAdapter() != null) {
            WearSyncService.start(this)
        }
        setContent { WearNavGraph() }
    }
}
