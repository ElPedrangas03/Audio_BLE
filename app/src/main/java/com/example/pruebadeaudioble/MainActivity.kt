package com.example.pruebadeaudioble

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import com.example.pruebadeaudioble.ble.BleManager
import com.example.pruebadeaudioble.BLEDeviceListScreen
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                val context = LocalContext.current
                val bleManager = remember { BleManager(context) }
                val scanResults by bleManager.scanResults.collectAsState()

                BLEDeviceListScreen(
                    scanResults = scanResults,
                    onStartScan = { bleManager.startScan() },
                    onStopScan = { bleManager.stopScan() }
                )
            }
        }
    }
}
