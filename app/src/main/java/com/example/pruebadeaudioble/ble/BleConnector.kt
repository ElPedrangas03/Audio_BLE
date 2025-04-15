package com.example.pruebadeaudioble.ble

import android.bluetooth.*
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class BleConnector(private val context: Context) {

    private var bluetoothGatt: BluetoothGatt? = null

    private val _discoveredServices = MutableStateFlow<List<BluetoothGattService>>(emptyList())
    val discoveredServices: StateFlow<List<BluetoothGattService>> = _discoveredServices

    private val _batteryLevel = MutableStateFlow<Int?>(null)
    val batteryLevel: StateFlow<Int?> = _batteryLevel

    private val _deviceInfo = MutableStateFlow(mutableMapOf<String, String>())
    val deviceInfo: StateFlow<Map<String, String>> = _deviceInfo

    private val _heartRate = MutableStateFlow<Int?>(null)
    val heartRate: StateFlow<Int?> = _heartRate

    fun connectToDevice(device: BluetoothDevice) {
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("BLE", "✅ Conectado a ${gatt.device.address}")
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("BLE", "❌ Desconectado de ${gatt.device.address}")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val servicios = gatt.services
                _discoveredServices.value = servicios

                Log.d("BLE", "🔍 Servicios descubiertos: ${servicios.size}")
                servicios.take(5).forEach { Log.d("BLE", "➡ ${it.uuid}") }

                // BATERÍA
                val batteryChar = servicios.find {
                    it.uuid == UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb")
                }?.getCharacteristic(UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb"))

                batteryChar?.let { gatt.readCharacteristic(it) }

                // INFO DISPOSITIVO
                val infoService = servicios.find {
                    it.uuid == UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
                }

                infoService?.let {
                    listOf(
                        "Modelo" to UUID.fromString("00002A24-0000-1000-8000-00805f9b34fb"),
                        "Fabricante" to UUID.fromString("00002A29-0000-1000-8000-00805f9b34fb"),
                        "Firmware" to UUID.fromString("00002A26-0000-1000-8000-00805f9b34fb")
                    ).forEach { (label, uuid) ->
                        val charac = it.getCharacteristic(uuid)
                        charac?.let { gatt.readCharacteristic(charac) }
                    }
                }

                // FRECUENCIA CARDIACA
                val hrService = servicios.find {
                    it.uuid == UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb")
                }

                val hrChar = hrService?.getCharacteristic(
                    UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb")
                )

                hrChar?.let {
                    Log.d("BLE", "Intentando leer frecuencia cardíaca...")
                    gatt.readCharacteristic(it)
                }
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (characteristic.uuid) {
                    UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb") -> {
                        val battery = characteristic.value.firstOrNull()?.toInt()
                        _batteryLevel.value = battery
                        Log.d("BLE", "🔋 Batería: $battery%")
                    }

                    UUID.fromString("00002A24-0000-1000-8000-00805f9b34fb") -> {
                        _deviceInfo.value = _deviceInfo.value.toMutableMap().apply {
                            put("Modelo", characteristic.getStringValue(0) ?: "Desconocido")
                        }
                    }

                    UUID.fromString("00002A29-0000-1000-8000-00805f9b34fb") -> {
                        _deviceInfo.value = _deviceInfo.value.toMutableMap().apply {
                            put("Fabricante", characteristic.getStringValue(0) ?: "Desconocido")
                        }
                    }

                    UUID.fromString("00002A26-0000-1000-8000-00805f9b34fb") -> {
                        _deviceInfo.value = _deviceInfo.value.toMutableMap().apply {
                            put("Firmware", characteristic.getStringValue(0) ?: "Desconocido")
                        }
                    }

                    UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb") -> {
                        val heartRateValue = characteristic.value.getOrNull(1)?.toInt()
                        _heartRate.value = heartRateValue
                        Log.d("BLE", "❤️ Frecuencia cardíaca: $heartRateValue bpm")
                    }
                }
            }
        }
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
    }
}