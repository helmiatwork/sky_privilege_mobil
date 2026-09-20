package com.skyprivilege.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RedemptionHistoryItem(
    val id: Long,
    @SerialName("pnr_masked") val pnrMasked: String = "",
    @SerialName("flight_number") val flightNumber: String = "",
    @SerialName("outlet_name") val outletName: String = "",
    @SerialName("cashier_name") val cashierName: String = "",
    @SerialName("discount_amount") val discountAmount: Double = 0.0,
    val status: String = "approved",
    @SerialName("is_overridden") val isOverridden: Boolean = false,
    @SerialName("time_formatted") val timeFormatted: String = "",
    @SerialName("date_formatted") val dateFormatted: String = "",
    @SerialName("is_today") val isToday: Boolean = false
)
