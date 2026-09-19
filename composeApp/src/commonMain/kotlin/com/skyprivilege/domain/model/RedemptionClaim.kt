package com.skyprivilege.domain.model

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
    val ticketPhoto: String? = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg=="
)
