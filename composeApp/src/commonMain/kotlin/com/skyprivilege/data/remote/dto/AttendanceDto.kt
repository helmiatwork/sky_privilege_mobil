package com.skyprivilege.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecordAttendanceRequest(
    @SerialName("cashier_id") val cashierId: Long,
    @SerialName("outlet_id") val outletId: Long? = null,
    val type: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val accuracy: Float? = null
)

@Serializable
data class AttendanceRecordDto(
    val id: Long? = null,
    @SerialName("cashier_id") val cashierId: Long? = null,
    @SerialName("outlet_id") val outletId: Long? = null,
    val date: String? = null,
    @SerialName("check_in_at") val checkInAt: String? = null,
    @SerialName("check_out_at") val checkOutAt: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    val status: String? = null,
    @SerialName("duration_minutes") val durationMinutes: Int = 0,
    @SerialName("check_in_latitude") val checkInLatitude: Double? = null,
    @SerialName("check_in_longitude") val checkInLongitude: Double? = null,
    @SerialName("check_in_accuracy") val checkInAccuracy: Float? = null,
    @SerialName("check_out_latitude") val checkOutLatitude: Double? = null,
    @SerialName("check_out_longitude") val checkOutLongitude: Double? = null,
    @SerialName("check_out_accuracy") val checkOutAccuracy: Float? = null
)

@Serializable
data class RecordAttendanceResponse(
    val success: Boolean,
    val message: String? = null,
    val action: String? = null,
    val attendance: AttendanceRecordDto? = null,
    val error: String? = null
)

@Serializable
data class TodayAttendanceResponse(
    val success: Boolean,
    val today: String? = null,
    @SerialName("has_checked_in") val hasCheckedIn: Boolean = false,
    @SerialName("has_checked_out") val hasCheckedOut: Boolean = false,
    val attendance: AttendanceRecordDto? = null,
    val error: String? = null
)

@Serializable
data class CreateCorrectionRequest(
    @SerialName("cashier_id") val cashierId: Long,
    @SerialName("outlet_id") val outletId: Long,
    @SerialName("target_date") val targetDate: String,
    @SerialName("correction_type") val correctionType: String,
    @SerialName("requested_check_in_at") val requestedCheckInAt: String? = null,
    @SerialName("requested_check_out_at") val requestedCheckOutAt: String? = null,
    val reason: String
)

@Serializable
data class CorrectionRequestDto(
    val id: Long,
    val status: String,
    @SerialName("correction_type") val correctionType: String,
    @SerialName("target_date") val targetDate: String,
    @SerialName("requested_check_in_at") val requestedCheckInAt: String? = null,
    @SerialName("requested_check_out_at") val requestedCheckOutAt: String? = null,
    val reason: String? = null
)

@Serializable
data class CreateCorrectionResponse(
    val success: Boolean,
    val message: String? = null,
    @SerialName("correction_request") val correctionRequest: CorrectionRequestDto? = null,
    val error: String? = null
)

@Serializable
data class AttendanceListResponse(
    val success: Boolean,
    val attendances: List<com.skyprivilege.domain.model.AttendanceHistoryItem> = emptyList(),
    val error: String? = null
)

