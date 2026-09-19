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
        signals: LocationContext
    ): Result<String>

    suspend fun submitRedemption(claim: RedemptionClaim): Result<String>

    suspend fun requestVoid(
        redemptionId: Long,
        supervisorPin: String,
        reason: String
    ): Result<Boolean>

    suspend fun getRedemptions(cashierId: Long): Result<List<com.skyprivilege.domain.model.RedemptionHistoryItem>>
}
