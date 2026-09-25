package com.skyprivilege.e2e

import com.skyprivilege.data.remote.dto.ClaimDiscountRequest
import com.skyprivilege.data.remote.dto.ClaimDiscountResponse
import com.skyprivilege.data.repository.RedemptionRepositoryImpl
import com.skyprivilege.data.repository.SubmitRedemptionRequest
import com.skyprivilege.data.repository.SubmitRedemptionResponse
import com.skyprivilege.domain.model.BaggagePricingCalculator
import com.skyprivilege.domain.model.BaggageSize
import com.skyprivilege.domain.model.ClaimStatus
import com.skyprivilege.domain.model.LocationContext
import com.skyprivilege.domain.model.RedemptionClaim
import com.skyprivilege.domain.model.Ticket
import com.skyprivilege.domain.model.WrapType
import com.skyprivilege.domain.repository.NonStackingConflictException
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class E2EJourneysTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val sampleTicket = Ticket(
        passengerName = "PRATAMA/BUDI",
        pnr = "GA9988",
        fromAirport = "CGK",
        toAirport = "DPS",
        operatingCarrier = "GA",
        flightNumber = "GA402",
        flightDate = "2026-09-25",
        compartmentCode = "Y",
        seatNumber = "12A",
        checkInSequence = "001",
        canonicalHash = "e2e_canonical_pnr_hash_64chars_abcdef0123456789abcdef0123456789abcd"
    )

    // =========================================================================
    // 1. Baggage Selection & Deterministic Pricing Flow (S / M / L / XL + Bubble)
    // =========================================================================
    @Test
    fun testBaggageSelectionAndDeterministicPricingFlow() {
        val discountRupiah = 25_000L
        val discountCents = 2_500_000L

        // Size S: 50.000 base
        val grossSStandard = BaggagePricingCalculator.calculateGrossRupiah(BaggageSize.S, WrapType.STANDARD)
        val grossSBubble = BaggagePricingCalculator.calculateGrossRupiah(BaggageSize.S, WrapType.BUBBLE)
        assertEquals(50_000L, grossSStandard)
        assertEquals(65_000L, grossSBubble)
        assertEquals(25_000L, BaggagePricingCalculator.calculateNetRupiah(grossSStandard, discountRupiah))
        assertEquals(40_000L, BaggagePricingCalculator.calculateNetRupiah(grossSBubble, discountRupiah))

        // Size M: 65.000 base
        val grossMStandard = BaggagePricingCalculator.calculateGrossRupiah(BaggageSize.M, WrapType.STANDARD)
        val grossMBubble = BaggagePricingCalculator.calculateGrossRupiah(BaggageSize.M, WrapType.BUBBLE)
        assertEquals(65_000L, grossMStandard)
        assertEquals(80_000L, grossMBubble)
        assertEquals(40_000L, BaggagePricingCalculator.calculateNetRupiah(grossMStandard, discountRupiah))
        assertEquals(55_000L, BaggagePricingCalculator.calculateNetRupiah(grossMBubble, discountRupiah))

        // Size L: 80.000 base
        val grossLStandard = BaggagePricingCalculator.calculateGrossRupiah(BaggageSize.L, WrapType.STANDARD)
        val grossLBubble = BaggagePricingCalculator.calculateGrossRupiah(BaggageSize.L, WrapType.BUBBLE)
        assertEquals(80_000L, grossLStandard)
        assertEquals(95_000L, grossLBubble)
        assertEquals(55_000L, BaggagePricingCalculator.calculateNetRupiah(grossLStandard, discountRupiah))
        assertEquals(70_000L, BaggagePricingCalculator.calculateNetRupiah(grossLBubble, discountRupiah))

        // Size XL: 100.000 base
        val grossXLStandard = BaggagePricingCalculator.calculateGrossRupiah(BaggageSize.XL, WrapType.STANDARD)
        val grossXLBubble = BaggagePricingCalculator.calculateGrossRupiah(BaggageSize.XL, WrapType.BUBBLE)
        assertEquals(100_000L, grossXLStandard)
        assertEquals(115_000L, grossXLBubble)
        assertEquals(75_000L, BaggagePricingCalculator.calculateNetRupiah(grossXLStandard, discountRupiah))
        assertEquals(90_000L, BaggagePricingCalculator.calculateNetRupiah(grossXLBubble, discountRupiah))

        // Cents deterministic precision for Size L + Bubble Wrap
        val grossCentsLBubble = BaggagePricingCalculator.calculateGrossCents(BaggageSize.L, WrapType.BUBBLE)
        val netCentsLBubble = BaggagePricingCalculator.calculateNetCents(grossCentsLBubble, discountCents)
        assertEquals(9_500_000L, grossCentsLBubble)
        assertEquals(7_000_000L, netCentsLBubble)
    }

    // =========================================================================
    // 2. DTO Serialization / Deserialization Validation
    // =========================================================================
    @Test
    fun testDtoSerializationAndDeserializationWithBaggageWrapping() {
        val claimRequest = ClaimDiscountRequest(
            orderId = "ORD-E2E-777",
            pnrHash = sampleTicket.canonicalHash.orEmpty(),
            outletId = 1L,
            cashierId = 2L,
            amountCents = 2_500_000L,
            signals = LocationContext(),
            bagSize = "L",
            wrapType = "bubble",
            paymentMethod = "cash",
            grossAmountCents = 9_500_000L,
            netAmountCents = 7_000_000L
        )

        val encodedClaim = json.encodeToString(claimRequest)
        assertTrue(encodedClaim.contains("\"bag_size\":\"L\""))
        assertTrue(encodedClaim.contains("\"wrap_type\":\"bubble\""))
        assertTrue(encodedClaim.contains("\"gross_amount_cents\":9500000"))
        assertTrue(encodedClaim.contains("\"net_amount_cents\":7000000"))

        val decodedClaim = json.decodeFromString<ClaimDiscountRequest>(encodedClaim)
        assertEquals("L", decodedClaim.bagSize)
        assertEquals("bubble", decodedClaim.wrapType)
        assertEquals(9_500_000L, decodedClaim.grossAmountCents)
        assertEquals(7_000_000L, decodedClaim.netAmountCents)

        // SubmitRedemptionRequest round-trip validation
        val submitRequest = SubmitRedemptionRequest(
            claimToken = "token_e2e_valid_123",
            pnrCanonicalHash = sampleTicket.canonicalHash.orEmpty(),
            orderId = "ORD-E2E-777",
            outletId = 1L,
            cashierId = 2L,
            amountCents = 2_500_000L,
            flightDate = sampleTicket.flightDate,
            flightNumber = sampleTicket.flightNumber,
            passengerName = sampleTicket.passengerName,
            rawPnr = sampleTicket.pnr,
            shiftId = 10L,
            bagSize = "L",
            wrapType = "bubble",
            paymentMethod = "cash",
            grossAmountCents = 9_500_000L,
            netAmountCents = 7_000_000L
        )

        val encodedSubmit = json.encodeToString(submitRequest)
        val decodedSubmit = json.decodeFromString<SubmitRedemptionRequest>(encodedSubmit)
        assertEquals("token_e2e_valid_123", decodedSubmit.claimToken)
        assertEquals("L", decodedSubmit.bagSize)
        assertEquals("bubble", decodedSubmit.wrapType)
        assertEquals(9_500_000L, decodedSubmit.grossAmountCents)
        assertEquals(7_000_000L, decodedSubmit.netAmountCents)

        // Response DTOs
        val responseJson = """{"success":true,"redemption_id":789}"""
        val decodedResponse = json.decodeFromString<SubmitRedemptionResponse>(responseJson)
        assertTrue(decodedResponse.success)
        assertEquals(789L, decodedResponse.redemptionId)

        val claimResponseJson = """{"success":true,"claim_token":"token_xyz","expires_in":120}"""
        val decodedClaimResponse = json.decodeFromString<ClaimDiscountResponse>(claimResponseJson)
        assertTrue(decodedClaimResponse.success)
        assertEquals("token_xyz", decodedClaimResponse.claimToken)
    }

    // =========================================================================
    // 3. Complete End-to-End Repository Scan-to-Paid Flow
    // =========================================================================
    @Test
    fun testCompleteScanToPaidFlowWithRepository() = runTest {
        val mockEngine = MockEngine { request ->
            when (request.url.encodedPath) {
                "/api/v1/redemptions/claim" -> {
                    val content = request.body as? OutgoingContent.ByteArrayContent
                    val bodyString = content?.bytes()?.decodeToString().orEmpty()
                    assertTrue(bodyString.contains("\"bag_size\":\"L\""))
                    assertTrue(bodyString.contains("\"wrap_type\":\"bubble\""))
                    respond(
                        content = """{"success":true,"claim_token":"e2e_claim_token_pass_123","expires_in":120}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/api/v1/redemptions" -> {
                    val content = request.body as? OutgoingContent.ByteArrayContent
                    val bodyString = content?.bytes()?.decodeToString().orEmpty()
                    assertTrue(bodyString.contains("\"claim_token\":\"e2e_claim_token_pass_123\""))
                    assertTrue(bodyString.contains("\"bag_size\":\"L\""))
                    assertTrue(bodyString.contains("\"wrap_type\":\"bubble\""))
                    assertTrue(bodyString.contains("\"gross_amount_cents\":9500000"))
                    assertTrue(bodyString.contains("\"net_amount_cents\":7000000"))
                    respond(
                        content = """{"success":true,"redemption_id":1001}""",
                        status = HttpStatusCode.Created,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> error("Unhandled path: ${request.url.encodedPath}")
            }
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; encodeDefaults = true })
            }
        }
        val repository = RedemptionRepositoryImpl(client)

        // 1. Request Claim Token with Baggage Size L + Bubble Wrap
        val tokenResult = repository.requestClaimToken(
            orderId = "ORD-E2E-1001",
            pnrHash = sampleTicket.canonicalHash.orEmpty(),
            outletId = 1L,
            cashierId = 2L,
            amountCents = 2_500_000L,
            signals = LocationContext(),
            bagSize = "L",
            wrapType = "bubble",
            paymentMethod = "cash",
            grossAmountCents = 9_500_000L,
            netAmountCents = 7_000_000L
        )

        assertTrue(tokenResult.isSuccess)
        val claimToken = tokenResult.getOrNull()
        assertEquals("e2e_claim_token_pass_123", claimToken)

        // 2. Submit Redemption with Baggage Size L + Bubble Wrap
        val claim = RedemptionClaim(
            ticket = sampleTicket,
            orderId = "ORD-E2E-1001",
            outletId = 1L,
            cashierId = 2L,
            amountCents = 2_500_000L,
            claimToken = claimToken,
            shiftId = 5L,
            status = ClaimStatus.PENDING,
            bagSize = "L",
            wrapType = "bubble",
            paymentMethod = "cash",
            grossAmountCents = 9_500_000L,
            netAmountCents = 7_000_000L,
            ticketPhotoData = "data:image/jpeg;base64,mocked_e2e_photo_data"
        )

        val submitResult = repository.submitRedemption(claim)
        assertTrue(submitResult.isSuccess)
        assertEquals("1001", submitResult.getOrNull())
    }

    // =========================================================================
    // 4. Error Handling: Non-Stacking Conflict (HTTP 409)
    // =========================================================================
    @Test
    fun testErrorHandlingNonStackingConflict409() = runTest {
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
            orderId = "ORD-ALREADY-USED-999",
            outletId = 1L,
            cashierId = 2L,
            amountCents = 2_500_000L,
            claimToken = "token_xyz",
            shiftId = 5L,
            status = ClaimStatus.PENDING
        )

        val result = repository.submitRedemption(claim)
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertIs<NonStackingConflictException>(exception)
        assertEquals("Order ini sudah menggunakan diskon SkyPrivilege", exception.errorMessage)
    }

    // =========================================================================
    // 5. Error Handling: Offline State Handling
    // =========================================================================
    @Test
    fun testErrorHandlingOfflineStateHandling() = runTest {
        val mockEngine = MockEngine {
            throw io.ktor.client.network.sockets.ConnectTimeoutException("Backend unreachable or tablet offline")
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
        val repository = RedemptionRepositoryImpl(client)

        val claim = RedemptionClaim(
            ticket = sampleTicket,
            orderId = "ORD-OFFLINE-001",
            outletId = 1L,
            cashierId = 2L,
            amountCents = 2_500_000L,
            claimToken = "token_offline",
            shiftId = 5L,
            status = ClaimStatus.PENDING
        )

        // Submit redemption under offline network state returns failure Result without crash
        val submitResult = repository.submitRedemption(claim)
        assertTrue(submitResult.isFailure)
        assertTrue(submitResult.exceptionOrNull() is Exception)

        // Request claim token under offline network state returns failure Result without crash
        val tokenResult = repository.requestClaimToken(
            orderId = "ORD-OFFLINE-001",
            pnrHash = sampleTicket.canonicalHash.orEmpty(),
            outletId = 1L,
            cashierId = 2L,
            amountCents = 2_500_000L,
            signals = LocationContext()
        )
        assertTrue(tokenResult.isFailure)
        assertTrue(tokenResult.exceptionOrNull() is Exception)
    }
}
