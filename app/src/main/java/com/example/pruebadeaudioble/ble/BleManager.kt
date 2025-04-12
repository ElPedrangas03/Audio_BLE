package com.example.pruebadeaudioble.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import com.example.pruebadeaudioble.ble.BleScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class BleManager(private val context: Context) {

    private val _scanResults = MutableStateFlow<List<BleScanResult>>(emptyList())
    val scanResults: StateFlow<List<BleScanResult>> = _scanResults

    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val scanner = bluetoothAdapter.bluetoothLeScanner

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val name = device.name ?: "Sin nombre"
            val rssi = result.rssi

            val bleResult = BleScanResult(name, device.address, rssi)
            _scanResults.value = _scanResults.value
                .filterNot { it.address == bleResult.address } + bleResult
        }
    }

    fun startScan() {
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(UUID.fromString("0000184E-0000-1000-8000-00805f9b34fb")))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner.startScan(listOf(filter), settings, scanCallback)
    }

    fun stopScan() {
        scanner.stopScan(scanCallback)
    }
}