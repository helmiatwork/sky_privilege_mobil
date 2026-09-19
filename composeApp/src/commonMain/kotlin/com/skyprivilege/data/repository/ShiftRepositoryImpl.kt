package com.skyprivilege.data.repository

import com.skyprivilege.data.remote.dto.CloseShiftRequest
import com.skyprivilege.data.remote.dto.CloseShiftResponse
import com.skyprivilege.data.remote.dto.CurrentShiftResponse
import com.skyprivilege.data.remote.dto.OpenShiftRequest
import com.skyprivilege.data.remote.dto.OpenShiftResponse
import com.skyprivilege.data.remote.dto.ShiftDto
import com.skyprivilege.domain.repository.ShiftRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ShiftRepositoryImpl(
    private val httpClient: HttpClient
) : ShiftRepository {

    override suspend fun openShift(
        cashierId: Long,
        outletId: Long,
        deviceId: Long?,
        openingCash: Double,
        openingSelfieKey: String?
    ): Result<ShiftDto> {
        return runCatching {
            val response = httpClient.post("/api/v1/shifts/open") {
                contentType(ContentType.Application.Json)
                setBody(
                    OpenShiftRequest(
                        cashierId = cashierId,
                        outletId = outletId,
                        deviceId = deviceId,
                        openingCash = openingCash,
                        openingSelfieKey = openingSelfieKey
                    )
                )
            }.body<OpenShiftResponse>()

            if (response.success && response.shift != null) {
                response.shift
            } else {
                throw IllegalStateException(response.error ?: "Failed to open shift")
            }
        }
    }

    override suspend fun closeShift(
        cashierId: Long,
        outletId: Long?,
        closingCash: Double?,
        shiftId: Long?
    ): Result<CloseShiftResponse> {
        return runCatching {
            val response = httpClient.post("/api/v1/shifts/close") {
                contentType(ContentType.Application.Json)
                setBody(
                    CloseShiftRequest(
                        cashierId = cashierId,
                        outletId = outletId,
                        closingCash = closingCash,
                        shiftId = shiftId
                    )
                )
            }.body<CloseShiftResponse>()

            if (response.success) {
                response
            } else {
                throw IllegalStateException(response.error ?: "Failed to close shift")
            }
        }
    }

    override suspend fun getCurrentShift(
        cashierId: Long,
        outletId: Long?
    ): Result<CurrentShiftResponse> {
        return runCatching {
            httpClient.get("/api/v1/shifts/current") {
                parameter("cashier_id", cashierId)
                if (outletId != null) {
                    parameter("outlet_id", outletId)
                }
            }.body<CurrentShiftResponse>()
        }
    }
}
