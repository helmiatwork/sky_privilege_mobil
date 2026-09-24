package com.skyprivilege.domain.repository

import com.skyprivilege.domain.model.LocationContext
import com.skyprivilege.domain.model.RedemptionClaim

class NonStackingConflictException(
    val errorMessage: String = "Order ini sudah menggunakan diskon SkyPrivilege"
) : Exception(errorMessage)

interface RedemptionRepository {
    suspend fun requestClaimToken(
        orderId: String,
        pnrHash: String,
        outletId: Long,
        cashierId: Long,
        amountCents: Long,
        signals: LocationContext,
        ticketPhotoData: String? = null,
        bagSize: String = "M",
        wrapType: String = "standard",
        paymentMethod: String = "qris",
        grossAmountCents: Long = 6500000L,
        netAmountCents: Long = 4000000L
    ): Result<String>

    suspend fun submitRedemption(claim: RedemptionClaim): Result<String>

    suspend fun requestVoid(
        redemptionId: Long,
        supervisorPin: String,
        reason: String
    ): Result<Boolean>

    suspend fun getRedemptions(cashierId: Long, todayOnly: Boolean = false): Result<List<com.skyprivilege.domain.model.RedemptionHistoryItem>>
}
