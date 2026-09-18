package com.skyprivilege.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class GpsCoordinate(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val isMock: Boolean
)

@Serializable
data class WifiContext(
    val bssid: String,
    val ssid: String,
    val rssiDbm: Int
)

@Serializable
data class DeviceIntegrityContext(
    val deviceRecognition: String,
    val isRooted: Boolean,
    val isEmulator: Boolean
)

@Serializable
data class LocationContext(
    val gps: GpsCoordinate? = null,
    val wifi: WifiContext? = null,
    val cellTowerId: String? = null,
    val deviceIntegrity: DeviceIntegrityContext? = null
)
