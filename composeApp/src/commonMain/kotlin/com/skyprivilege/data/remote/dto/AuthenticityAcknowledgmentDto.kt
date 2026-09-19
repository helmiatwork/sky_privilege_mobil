package com.skyprivilege.data.remote.dto

import com.skyprivilege.domain.model.TicketGuideline
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthenticityAcknowledgmentDto(
    @SerialName("cashier_id") val cashierId: Long,
    @SerialName("outlet_id") val outletId: Long,
    @SerialName("device_id") val deviceId: Long? = null,
    @SerialName("wifi_bssid") val wifiBssid: String? = null,
    @SerialName("wifi_ssid") val wifiSsid: String? = null,
    @SerialName("wifi_rssi") val wifiRssi: Int? = null,
    @SerialName("gps_latitude") val gpsLatitude: Double? = null,
    @SerialName("gps_longitude") val gpsLongitude: Double? = null,
    @SerialName("gps_accuracy") val gpsAccuracy: Float? = null
)

@Serializable
data class AuthenticityAcknowledgmentResponse(
    val status: String,
    val success: Boolean = true,
    @SerialName("acknowledgment_id") val acknowledgmentId: Long? = null,
    @SerialName("recorded_at") val recordedAt: String? = null,
    val error: String? = null
)

@Serializable
data class GuidelinesResponse(
    val status: String? = null,
    val success: Boolean = true,
    val data: List<TicketGuideline> = emptyList(),
    val guidelines: List<TicketGuideline> = emptyList(),
    val error: String? = null
)
