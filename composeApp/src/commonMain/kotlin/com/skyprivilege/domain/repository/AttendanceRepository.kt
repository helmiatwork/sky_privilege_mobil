package com.skyprivilege.domain.repository

import com.skyprivilege.data.remote.dto.CreateCorrectionResponse
import com.skyprivilege.data.remote.dto.RecordAttendanceResponse
import com.skyprivilege.data.remote.dto.TodayAttendanceResponse

interface AttendanceRepository {
    suspend fun getTodayAttendance(
        cashierId: Long,
        outletId: Long
    ): Result<TodayAttendanceResponse>

    suspend fun recordAttendance(
        cashierId: Long,
        outletId: Long,
        type: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        accuracy: Float? = null
    ): Result<RecordAttendanceResponse>

    suspend fun submitCorrectionRequest(
        cashierId: Long,
        outletId: Long,
        targetDate: String,
        correctionType: String,
        requestedCheckInAt: String?,
        requestedCheckOutAt: String?,
        reason: String
    ): Result<CreateCorrectionResponse>

    suspend fun getAttendanceHistory(cashierId: Long): Result<List<com.skyprivilege.domain.model.AttendanceHistoryItem>>
}
