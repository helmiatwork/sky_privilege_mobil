package com.skyprivilege.data.repository

import com.skyprivilege.data.remote.dto.CreateCorrectionRequest
import com.skyprivilege.data.remote.dto.CreateCorrectionResponse
import com.skyprivilege.data.remote.dto.RecordAttendanceRequest
import com.skyprivilege.data.remote.dto.RecordAttendanceResponse
import com.skyprivilege.data.remote.dto.TodayAttendanceResponse
import com.skyprivilege.domain.repository.AttendanceRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class AttendanceRepositoryImpl(
    private val httpClient: HttpClient
) : AttendanceRepository {

    override suspend fun getTodayAttendance(
        cashierId: Long,
        outletId: Long
    ): Result<TodayAttendanceResponse> {
        return runCatching {
            httpClient.get("/api/v1/attendances/today") {
                parameter("cashier_id", cashierId)
                parameter("outlet_id", outletId)
            }.body<TodayAttendanceResponse>()
        }
    }

    override suspend fun recordAttendance(
        cashierId: Long,
        outletId: Long,
        type: String?,
        latitude: Double?,
        longitude: Double?,
        accuracy: Float?
    ): Result<RecordAttendanceResponse> {
        return runCatching {
            val response = httpClient.post("/api/v1/attendances/record_time") {
                contentType(ContentType.Application.Json)
                setBody(
                    RecordAttendanceRequest(
                        cashierId = cashierId,
                        outletId = outletId,
                        type = type,
                        latitude = latitude,
                        longitude = longitude,
                        accuracy = accuracy
                    )
                )
            }.body<RecordAttendanceResponse>()

            if (response.success) {
                response
            } else {
                throw IllegalStateException(response.error ?: "Gagal mencatat absensi")
            }
        }
    }

    override suspend fun submitCorrectionRequest(
        cashierId: Long,
        outletId: Long,
        targetDate: String,
        correctionType: String,
        requestedCheckInAt: String?,
        requestedCheckOutAt: String?,
        reason: String
    ): Result<CreateCorrectionResponse> {
        return runCatching {
            val response = httpClient.post("/api/v1/attendance_corrections") {
                contentType(ContentType.Application.Json)
                setBody(
                    CreateCorrectionRequest(
                        cashierId = cashierId,
                        outletId = outletId,
                        targetDate = targetDate,
                        correctionType = correctionType,
                        requestedCheckInAt = requestedCheckInAt,
                        requestedCheckOutAt = requestedCheckOutAt,
                        reason = reason
                    )
                )
            }.body<CreateCorrectionResponse>()

            if (response.success) {
                response
            } else {
                throw IllegalStateException(response.error ?: "Gagal mengirim pengajuan koreksi")
            }
        }
    }

    override suspend fun getAttendanceHistory(cashierId: Long): Result<List<com.skyprivilege.domain.model.AttendanceHistoryItem>> {
        return runCatching {
            val response = httpClient.get("/api/v1/attendances") {
                parameter("cashier_id", cashierId)
            }.body<com.skyprivilege.data.remote.dto.AttendanceListResponse>()

            if (response.success) {
                response.attendances
            } else {
                throw IllegalStateException(response.error ?: "Gagal memuat riwayat absensi")
            }
        }
    }
}
