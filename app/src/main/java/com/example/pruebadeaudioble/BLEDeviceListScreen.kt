package com.example.pruebadeaudioble

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pruebadeaudioble.ble.BleScanResult
import com.example.pruebadeaudioble.components.DeviceCard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BLEDeviceListScreen(
    scanResults: List<BleScanResult>,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = onStartScan) {
                Text("Iniciar Scan")
            }
            Button(onClick = onStopScan) {
                Text("Detener Scan")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (scanResults.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se han detectado dispositivos BLE aún.")
            }
        } else {
            LazyColumn {
                items(scanResults) { device ->
                    BLEDeviceCard(device)
                }
            }
        }
    }
}

@Composable
fun BLEDeviceCard(device: BleScanResult) {
    val signalColor = when {
        device.rssi > -60 -> Color(0xFF4CAF50) // buena señal
        device.rssi > -80 -> Color(0xFFFFC107) // regular
        else -> Color(0xFFF44336)              // débil
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "Dispositivo BLE",
                tint = signalColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = device.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "MAC: ${device.address}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "RSSI: ${device.rssi} dBm",
                    color = signalColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}