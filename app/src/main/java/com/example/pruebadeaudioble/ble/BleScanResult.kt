package com.example.pruebadeaudioble.ble

data class BleScanResult(
    val name: String,
    val address: String,
    val rssi: Int
)