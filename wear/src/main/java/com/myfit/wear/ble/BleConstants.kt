package com.myfit.wear.ble

import java.util.UUID

object BleConstants {
    val SERVICE_UUID        = UUID.fromString("12345678-1234-1234-1234-1234567890AB")
    val SYNC_REQUEST_UUID   = UUID.fromString("12345678-1234-1234-1234-1234567890A1")
    val SYNC_DATA_UUID      = UUID.fromString("12345678-1234-1234-1234-1234567890A2")
    val SYNC_ACK_UUID       = UUID.fromString("12345678-1234-1234-1234-1234567890A3")
    val PHONE_TO_WATCH_UUID = UUID.fromString("12345678-1234-1234-1234-1234567890A4")
    val CLIENT_CONFIG_UUID  = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

    const val CHUNK_SIZE    = 500
    const val PREFS_NAME    = "myfit_prefs"
    const val KEY_LAST_SYNC = "last_sync_ms"
}
