package com.skyprivilege.domain.repository

import com.skyprivilege.data.remote.dto.CloseShiftResponse
import com.skyprivilege.data.remote.dto.CurrentShiftResponse
import com.skyprivilege.data.remote.dto.ShiftDto

interface ShiftRepository {
    suspend fun openShift(
        cashierId: Long,
        outletId: Long,
        deviceId: Long? = null,
        openingCash: Double = 0.0,
        openingSelfieKey: String? = null
    ): Result<ShiftDto>

    suspend fun closeShift(
        cashierId: Long,
        outletId: Long? = null,
        closingCash: Double? = null,
        shiftId: Long? = null
    ): Result<CloseShiftResponse>

    suspend fun getCurrentShift(
        cashierId: Long,
        outletId: Long? = null
    ): Result<CurrentShiftResponse>
}
