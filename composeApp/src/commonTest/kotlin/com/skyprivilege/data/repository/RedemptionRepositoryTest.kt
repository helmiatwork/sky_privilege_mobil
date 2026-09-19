package com.skyprivilege.data.repository

import com.skyprivilege.domain.model.ClaimStatus
import com.skyprivilege.domain.model.RedemptionClaim
import com.skyprivilege.domain.model.Ticket
import com.skyprivilege.domain.repository.NonStackingConflictException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class RedemptionRepositoryTest {

    private val sampleTicket = Ticket(
        passengerName = "DOE/JOHN MR",
        pnr = "ABC1234",
        fromAirport = "CGK",
        toAirport = "DPS",
        operatingCarrier = "GA",
        flightNumber = "GA402",
        flightDate = "2026-09-19",
        compartmentCode = "Y",
        seatNumber = "12A",
        checkInSequence = "001",
        canonicalHash = "hash_sample_123"
    )

    @Test
    fun testSubmitRedemptionSuccessWithShiftId() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/redemptions", request.url.encodedPath)
            respond(
                content = """
                    {
                        "success": true,
                        "redemption_id": 999
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val repository = RedemptionRepositoryImpl(client)
        val claim = RedemptionClaim(
            ticket = sampleTicket,
            orderId = "MOKA-1001",
            outletId = 1L,
            cashierId = 2L,
            amountCents = 5000000L,
            claimToken = "valid_token_xyz",
            shiftId = 42L,
            status = ClaimStatus.PENDING
        )

        val result = repository.submitRedemption(claim)
        assertTrue(result.isSuccess)
        assertEquals("999", result.getOrNull())
    }

    @Test
    fun testSubmitRedemptionReturnsNonStackingConflictExceptionOn409() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """
                    {
                        "success": false,
                        "error": "Order ini sudah menggunakan diskon SkyPrivilege"
                    }
                """.trimIndent(),
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val repository = RedemptionRepositoryImpl(client)
        val claim = RedemptionClaim(
            ticket = sampleTicket,
            orderId = "MOKA-ORDER-ALREADY-USED",
            outletId = 1L,
            cashierId = 2L,
            amountCents = 5000000L,
            claimToken = "valid_token_xyz",
            shiftId = 42L,
            status = ClaimStatus.PENDING
        )

        val result = repository.submitRedemption(claim)
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertIs<NonStackingConflictException>(exception)
        assertEquals("Order ini sudah menggunakan diskon SkyPrivilege", exception.errorMessage)
    }
}
