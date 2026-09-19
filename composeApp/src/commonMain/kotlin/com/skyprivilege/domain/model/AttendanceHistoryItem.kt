package com.skyprivilege.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceHistoryItem(
    val id: Long,
    val date: String,
    @SerialName("date_formatted") val dateFormatted: String = "",
    @SerialName("check_in_at") val checkInAt: String? = null,
    @SerialName("check_out_at") val checkOutAt: String? = null,
    val status: String = "present",
    @SerialName("check_in_lat") val checkInLat: Double? = null,
    @SerialName("check_in_lng") val checkInLng: Double? = null,
    @SerialName("check_in_accuracy") val checkInAccuracy: Float? = null
)
