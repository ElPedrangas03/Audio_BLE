package com.example.pruebadeaudioble

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pruebadeaudioble.R
import com.example.pruebadeaudioble.ble.BleConnector

@Composable
fun BLEDeviceListScreen(
    scanResults: List<BleScanResult>,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit
) {
    val context = LocalContext.current
    val bleConnector = remember { BleConnector(context) }
    val services by bleConnector.discoveredServices.collectAsState()
    var selectedDevice by remember { mutableStateOf<BleScanResult?>(null) }

    if (selectedDevice != null && services.isNotEmpty()) {
        // Mostrar la pantalla de detalles GATT
        GattDetailsScreen(
            services = services,
            bleConnector = bleConnector,
            onBack = { selectedDevice = null }
        )
    } else {
        // Mostrar lista normal
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
                        BLEDeviceCard(device = device) {
                            bleConnector.connectToDevice(
                                BluetoothAdapter.getDefaultAdapter()
                                    ?.getRemoteDevice(device.address)!!
                            )
                            selectedDevice = device
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BLEDeviceCard(
    device: BleScanResult,
    onClick: () -> Unit
) {
    val signalColor = when {
        device.rssi > -60 -> Color(0xFF4CAF50)
        device.rssi > -80 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    val signalBars = when {
        device.rssi > -60 -> "📶📶📶"
        device.rssi > -75 -> "📶📶"
        device.rssi > -85 -> "📶"
        else -> "❌"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.bluetooth),
                contentDescription = "Icono BLE",
                modifier = Modifier.size(50.dp).padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = device.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "MAC: ${device.address}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "RSSI: ${device.rssi} dBm $signalBars",
                    color = signalColor,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun GattDetailsScreen(
    services: List<BluetoothGattService>,
    bleConnector: BleConnector,
    onBack: () -> Unit
) {
    val batteryLevel by bleConnector.batteryLevel.collectAsState()
    val deviceInfo by bleConnector.deviceInfo.collectAsState()
    val heartRate by bleConnector.heartRate.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Información del dispositivo", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        // BATERÍA
        batteryLevel?.let {
            Text(
                text = "Batería: $it%",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        // DISPOSITIVO
        deviceInfo["Modelo"]?.let {
            Text("Modelo: $it", style = MaterialTheme.typography.bodyLarge)
        }
        deviceInfo["Fabricante"]?.let {
            Text("Fabricante: $it", style = MaterialTheme.typography.bodyLarge)
        }
        deviceInfo["Firmware"]?.let {
            Text("Firmware: $it", style = MaterialTheme.typography.bodyLarge)
        }

        // FRECUENCIA CARDÍACA
        heartRate?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text("❤Frecuencia cardíaca: $it bpm", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(services.take(5)) { service ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Servicio: ${service.uuid}")
                        Text("Características: ${service.characteristics.size}")
                    }
                }
            }
        }
    }
}