package com.skyprivilege.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OpenShiftRequest(
    @SerialName("cashier_id") val cashierId: Long,
    @SerialName("outlet_id") val outletId: Long,
    @SerialName("device_id") val deviceId: Long? = null,
    @SerialName("opening_cash") val openingCash: Double = 0.0,
    @SerialName("opening_selfie_key") val openingSelfieKey: String? = null,
    @SerialName("auth_method_opened") val authMethodOpened: String = "face"
)

@Serializable
data class CloseShiftRequest(
    @SerialName("cashier_id") val cashierId: Long,
    @SerialName("outlet_id") val outletId: Long? = null,
    @SerialName("closing_cash") val closingCash: Double? = null,
    @SerialName("shift_id") val shiftId: Long? = null
)

@Serializable
data class ShiftDto(
    val id: Long,
    @SerialName("cashier_id") val cashierId: Long,
    @SerialName("outlet_id") val outletId: Long,
    @SerialName("device_id") val deviceId: Long? = null,
    @SerialName("opened_at") val openedAt: String,
    @SerialName("closed_at") val closedAt: String? = null,
    val status: String,
    @SerialName("total_redemptions_count") val totalRedemptionsCount: Int = 0,
    @SerialName("total_discount_cents") val totalDiscountCents: Long = 0,
    @SerialName("opening_cash") val openingCash: Double? = null,
    @SerialName("closing_cash") val closingCash: Double? = null,
    @SerialName("opening_selfie_key") val openingSelfieKey: String? = null
)

@Serializable
data class XReportDto(
    @SerialName("shift_id") val shiftId: Long,
    @SerialName("cashier_id") val cashierId: Long,
    @SerialName("outlet_id") val outletId: Long,
    val status: String,
    @SerialName("total_redemptions_count") val totalRedemptionsCount: Int = 0,
    @SerialName("total_discount_amount") val totalDiscountAmount: Double = 0.0,
    @SerialName("total_discount_cents") val totalDiscountCents: Long = 0
)

@Serializable
data class ZReportDto(
    @SerialName("shift_id") val shiftId: Long,
    @SerialName("cashier_id") val cashierId: Long,
    val status: String,
    @SerialName("total_redemptions_count") val totalRedemptionsCount: Int = 0,
    @SerialName("total_discount_cents") val totalDiscountCents: Long = 0,
    @SerialName("total_discount_amount") val totalDiscountAmount: Double = 0.0,
    @SerialName("opening_cash") val openingCash: Double? = null,
    @SerialName("closing_cash") val closingCash: Double? = null,
    @SerialName("closed_at") val closedAt: String? = null
)

@Serializable
data class OpenShiftResponse(
    val success: Boolean,
    val shift: ShiftDto? = null,
    val error: String? = null
)

@Serializable
data class CurrentShiftResponse(
    val success: Boolean,
    val shift: ShiftDto? = null,
    @SerialName("x_report") val xReport: XReportDto? = null,
    val error: String? = null
)

@Serializable
data class CloseShiftResponse(
    val success: Boolean,
    @SerialName("z_report") val zReport: ZReportDto? = null,
    val error: String? = null
)
