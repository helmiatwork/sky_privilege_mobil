package com.skyprivilege.data.repository

import com.skyprivilege.data.remote.dto.ClaimDiscountRequest
import com.skyprivilege.data.remote.dto.ClaimDiscountResponse
import com.skyprivilege.domain.model.LocationContext
import com.skyprivilege.domain.model.RedemptionClaim
import com.skyprivilege.domain.repository.NonStackingConflictException
import com.skyprivilege.domain.repository.RedemptionRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SubmitRedemptionRequest(
    @SerialName("claim_token") val claimToken: String,
    @SerialName("pnr_canonical_hash") val pnrCanonicalHash: String,
    @SerialName("order_id") val orderId: String,
    @SerialName("outlet_id") val outletId: Long,
    @SerialName("cashier_id") val cashierId: Long,
    @SerialName("amount_cents") val amountCents: Long,
    @SerialName("flight_date") val flightDate: String,
    @SerialName("flight_number") val flightNumber: String,
    @SerialName("passenger_name") val passengerName: String? = null,
    @SerialName("raw_pnr") val rawPnr: String? = null,
    @SerialName("shift_id") val shiftId: Long? = null,
    @SerialName("ticket_photo") val ticketPhoto: String? = null,
    @SerialName("ticket_photo_data") val ticketPhotoData: String? = null,
    @SerialName("bag_size") val bagSize: String = "M",
    @SerialName("wrap_type") val wrapType: String = "standard",
    @SerialName("payment_method") val paymentMethod: String = "qris",
    @SerialName("gross_amount_cents") val grossAmountCents: Long = 6500000L,
    @SerialName("net_amount_cents") val netAmountCents: Long = 4000000L
)

@Serializable
data class SubmitRedemptionResponse(
    val success: Boolean,
    @SerialName("redemption_id") val redemptionId: Long? = null,
    val error: String? = null
)

@Serializable
data class VoidRequest(
    @SerialName("redemption_id") val redemptionId: Long,
    @SerialName("supervisor_pin") val supervisorPin: String,
    val reason: String
)

@Serializable
data class VoidResponse(
    val success: Boolean,
    val error: String? = null
)

class RedemptionRepositoryImpl(
    private val httpClient: HttpClient
) : RedemptionRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun requestClaimToken(
        orderId: String,
        pnrHash: String,
        outletId: Long,
        cashierId: Long,
        amountCents: Long,
        signals: LocationContext,
        ticketPhotoData: String?,
        bagSize: String,
        wrapType: String,
        paymentMethod: String,
        grossAmountCents: Long,
        netAmountCents: Long
    ): Result<String> {
        return runCatching {
            val response = httpClient.post("/api/v1/redemptions/claim") {
                contentType(ContentType.Application.Json)
                setBody(
                    ClaimDiscountRequest(
                        orderId = orderId,
                        pnrHash = pnrHash,
                        outletId = outletId,
                        cashierId = cashierId,
                        amountCents = amountCents,
                        signals = signals,
                        ticketPhotoData = ticketPhotoData,
                        bagSize = bagSize,
                        wrapType = wrapType,
                        paymentMethod = paymentMethod,
                        grossAmountCents = grossAmountCents,
                        netAmountCents = netAmountCents
                    )
                )
            }.body<ClaimDiscountResponse>()

            if (response.success && response.claimToken != null) {
                response.claimToken
            } else {
                throw IllegalStateException(response.error ?: "Claim token request rejected")
            }
        }
    }

    override suspend fun submitRedemption(claim: RedemptionClaim): Result<String> {
        return runCatching {
            val httpResponse = httpClient.post("/api/v1/redemptions") {
                contentType(ContentType.Application.Json)
                setBody(
                    SubmitRedemptionRequest(
                        claimToken = claim.claimToken.orEmpty(),
                        pnrCanonicalHash = claim.ticket.canonicalHash.orEmpty(),
                        orderId = claim.orderId,
                        outletId = claim.outletId,
                        cashierId = claim.cashierId,
                        amountCents = claim.amountCents,
                        flightDate = claim.ticket.flightDate,
                        flightNumber = claim.ticket.flightNumber,
                        passengerName = claim.ticket.passengerName,
                        rawPnr = claim.ticket.pnr,
                        shiftId = claim.shiftId,
                        ticketPhoto = claim.ticketPhoto,
                        ticketPhotoData = claim.ticketPhotoData ?: claim.ticketPhoto,
                        bagSize = claim.bagSize,
                        wrapType = claim.wrapType,
                        paymentMethod = claim.paymentMethod,
                        grossAmountCents = claim.grossAmountCents,
                        netAmountCents = claim.netAmountCents
                    )
                )
            }

            if (httpResponse.status == HttpStatusCode.Conflict) {
                val errorBody = try {
                    json.decodeFromString<SubmitRedemptionResponse>(httpResponse.bodyAsText()).error
                } catch (e: Exception) {
                    null
                }
                throw NonStackingConflictException(errorBody ?: "Order ini sudah menggunakan diskon SkyPrivilege")
            }

            val body = json.decodeFromString<SubmitRedemptionResponse>(httpResponse.bodyAsText())
            if (body.success && body.redemptionId != null) {
                body.redemptionId.toString()
            } else {
                throw IllegalStateException(body.error ?: "Redemption failed")
            }
        }
    }

    override suspend fun requestVoid(
        redemptionId: Long,
        supervisorPin: String,
        reason: String
    ): Result<Boolean> {
        return runCatching {
            val response = httpClient.post("/api/v1/voids") {
                contentType(ContentType.Application.Json)
                setBody(
                    VoidRequest(
                        redemptionId = redemptionId,
                        supervisorPin = supervisorPin,
                        reason = reason
                    )
                )
            }.body<VoidResponse>()

            response.success
        }
    }

    override suspend fun getRedemptions(cashierId: Long, todayOnly: Boolean): Result<List<com.skyprivilege.domain.model.RedemptionHistoryItem>> {
        return runCatching {
            val response = httpClient.get("/api/v1/redemptions") {
                parameter("cashier_id", cashierId)
                if (todayOnly) {
                    parameter("today", "true")
                }
            }.body<com.skyprivilege.data.remote.dto.RedemptionsListResponse>()

            if (response.success) {
                response.redemptions
            } else {
                throw IllegalStateException(response.error ?: "Gagal memuat riwayat transaksi")
            }
        }
    }
}
