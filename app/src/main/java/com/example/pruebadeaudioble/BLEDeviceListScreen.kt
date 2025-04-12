package com.example.pruebadeaudioble

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pruebadeaudioble.ble.BleScanResult
import com.example.pruebadeaudioble.components.DeviceCard

@Composable
fun BLEDeviceListScreen(
    scanResults: List<BleScanResult>,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onStartScan) {
                Text("Iniciar escaneo")
            }
            Button(onClick = onStopScan) {
                Text("Detener escaneo")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(scanResults) { result ->
                DeviceCard(result)
            }
        }
    }
}