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
        grossAmountCents: Long = 7_500_000L,
        netAmountCents: Long = 5_000_000L
    ): Result<String>

    suspend fun submitRedemption(claim: RedemptionClaim): Result<String>

    suspend fun requestVoid(
        redemptionId: Long,
        supervisorPin: String,
        reason: String
    ): Result<Boolean>

    suspend fun getRedemptions(cashierId: Long, todayOnly: Boolean = false): Result<List<com.skyprivilege.domain.model.RedemptionHistoryItem>>

    suspend fun issueEmergencyVoucher(
        serialNumber: String,
        pnr: String,
        supervisorPin: String,
        reason: String,
        cashierId: Long,
        outletId: Long
    ): Result<String>
}
