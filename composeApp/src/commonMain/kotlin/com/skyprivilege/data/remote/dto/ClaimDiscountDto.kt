package com.skyprivilege.data.remote.dto

import com.skyprivilege.domain.model.LocationContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClaimDiscountRequest(
    @SerialName("order_id") val orderId: String,
    @SerialName("pnr_hash") val pnrHash: String,
    @SerialName("outlet_id") val outletId: Long,
    @SerialName("cashier_id") val cashierId: Long,
    @SerialName("amount_cents") val amountCents: Long,
    val signals: LocationContext,
    @SerialName("ticket_photo_data") val ticketPhotoData: String? = null
)

@Serializable
data class ClaimDiscountResponse(
    val success: Boolean,
    val verdict: String? = null,
    val score: Float? = null,
    @SerialName("claim_token") val claimToken: String? = null,
    @SerialName("expires_in") val expiresInSeconds: Int? = null,
    val error: String? = null
)

@Serializable
data class RedemptionsListResponse(
    val success: Boolean,
    val redemptions: List<com.skyprivilege.domain.model.RedemptionHistoryItem> = emptyList(),
    val error: String? = null
)

