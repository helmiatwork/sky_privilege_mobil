package com.skyprivilege.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ClaimStatus {
    PENDING,
    APPROVED,
    FLAGGED,
    REJECTED,
    VOIDED
}

@Serializable
data class RedemptionClaim(
    val claimId: String? = null,
    val ticket: Ticket,
    val orderId: String,
    val outletId: Long,
    val cashierId: Long,
    val amountCents: Long,
    val claimToken: String? = null,
    val shiftId: Long? = null,
    val status: ClaimStatus = ClaimStatus.PENDING,
    val trustScore: Float = 0.0f,
    val timestampEpochMs: Long = 0L,
    val ticketPhoto: String? = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
    val ticketPhotoData: String? = null,
    @SerialName("bag_size") val bagSize: String = "M",
    @SerialName("wrap_type") val wrapType: String = "standard",
    @SerialName("payment_method") val paymentMethod: String = "qris",
    @SerialName("gross_amount_cents") val grossAmountCents: Long = 6500000L,
    @SerialName("net_amount_cents") val netAmountCents: Long = 4000000L
)
