package com.skyprivilege.data.repository

import com.skyprivilege.data.remote.dto.VerifyTicketRequest
import com.skyprivilege.data.remote.dto.VerifyTicketResponse
import com.skyprivilege.domain.model.BarcodeData
import com.skyprivilege.domain.model.Ticket
import com.skyprivilege.domain.model.TicketVerificationException
import com.skyprivilege.domain.repository.TicketRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json

class TicketRepositoryImpl(
    private val httpClient: HttpClient
) : TicketRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun verifyBarcode(barcodeData: BarcodeData, outletId: Long): Result<Ticket> {
        return runCatching {
            val httpResponse = httpClient.post("/api/v1/tickets/verify") {
                contentType(ContentType.Application.Json)
                setBody(
                    VerifyTicketRequest(
                        barcodeData = barcodeData.rawPayload,
                        outletId = outletId
                    )
                )
            }

            val body = json.decodeFromString<VerifyTicketResponse>(httpResponse.bodyAsText())
            if (body.success && body.ticket != null) {
                val t = body.ticket
                Ticket(
                    passengerName = t.passengerName.orEmpty(),
                    pnr = t.pnr.orEmpty(),
                    fromAirport = t.fromAirport.orEmpty(),
                    toAirport = t.toAirport.orEmpty(),
                    operatingCarrier = t.operatingCarrier.orEmpty(),
                    flightNumber = t.flightNumber.orEmpty(),
                    flightDate = t.flightDate.orEmpty(),
                    compartmentCode = t.compartmentCode.orEmpty(),
                    seatNumber = t.seatNumber.orEmpty(),
                    checkInSequence = t.checkInSequence.orEmpty(),
                    canonicalHash = body.pnrHash,
                    isValid = t.valid
                )
            } else {
                throw TicketVerificationException(
                    message = body.error ?: "Gagal memverifikasi tiket",
                    reasonCode = body.reasonCode,
                    redeemedAt = body.redeemedAtFormatted ?: body.redeemedAt,
                    redeemedOutlet = body.redeemedOutlet,
                    redeemedCashier = body.redeemedCashier
                )
            }
        }
    }

    override suspend fun verifyTicket(
        barcodeData: String?,
        imageBase64: String?,
        outletId: Long,
        cashierId: Long?,
        deviceId: Long?,
        shiftId: Long?,
        checklistConfirmed: Boolean,
        latitude: Double?,
        longitude: Double?,
        accuracy: Float?,
        wifiBssid: String?,
        wifiSsid: String?
    ): Result<Ticket> {
        return runCatching {
            val httpResponse = httpClient.post("/api/v1/tickets/verify") {
                contentType(ContentType.Application.Json)
                setBody(
                    VerifyTicketRequest(
                        barcodeData = barcodeData,
                        imageBase64 = imageBase64,
                        outletId = outletId,
                        cashierId = cashierId,
                        deviceId = deviceId,
                        shiftId = shiftId,
                        checklistConfirmed = checklistConfirmed,
                        latitude = latitude,
                        longitude = longitude,
                        accuracy = accuracy,
                        wifiBssid = wifiBssid,
                        wifiSsid = wifiSsid
                    )
                )
            }

            val body = json.decodeFromString<VerifyTicketResponse>(httpResponse.bodyAsText())
            if (body.success && body.valid && body.ticket != null) {
                val t = body.ticket
                Ticket(
                    passengerName = t.passengerName.orEmpty(),
                    pnr = t.pnr.orEmpty(),
                    fromAirport = t.fromAirport.orEmpty(),
                    toAirport = t.toAirport.orEmpty(),
                    operatingCarrier = t.operatingCarrier.orEmpty(),
                    flightNumber = t.flightNumber.orEmpty(),
                    flightDate = t.flightDate.orEmpty(),
                    compartmentCode = t.compartmentCode.orEmpty(),
                    seatNumber = t.seatNumber.orEmpty(),
                    checkInSequence = t.checkInSequence.orEmpty(),
                    canonicalHash = body.pnrHash,
                    isValid = t.valid
                )
            } else {
                throw TicketVerificationException(
                    message = body.error ?: "Tiket tidak valid atau gagal diverifikasi",
                    reasonCode = body.reasonCode,
                    redeemedAt = body.redeemedAtFormatted ?: body.redeemedAt,
                    redeemedOutlet = body.redeemedOutlet,
                    redeemedCashier = body.redeemedCashier
                )
            }
        }
    }

    override suspend fun decodeOffline(rawBarcode: String): Result<Ticket> {
        return Result.failure(UnsupportedOperationException("Offline decoding not implemented on client"))
    }
}
