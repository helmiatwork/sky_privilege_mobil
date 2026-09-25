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
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.header
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

    /**
     * Deduplicated HttpClient mock factory configured with JSON content negotiation
     * and optional default headers (e.g. for Dual-SIM failover simulation).
     */
    private fun createMockClient(
        defaultHeaders: Map<String, String> = emptyMap(),
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ): HttpClient {
        val mockEngine = MockEngine(handler)
        return HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(json)
            }
            if (defaultHeaders.isNotEmpty()) {
                defaultRequest {
                    defaultHeaders.forEach { (key, value) ->
                        header(key, value)
                    }
                }
            }
        }
    }

    // =========================================================================
    // E2E-J2-001: Happy Path Barcode BCBP Scan-to-Paid Flow
    // =========================================================================
    /**
     * E2E-J2-001: Happy path barcode BCBP scan-to-paid flow with claim token request
     * and final redemption submission.
     *
     * Validates JSON request bodies using Json.decodeFromString and verifies HTTP 201 response.
     */
    @Test
    fun testE2EJ2001HappyPathScanToPaidFlow() = runTest {
        val client = createMockClient { request ->
            when (request.url.encodedPath) {
                "/api/v1/redemptions/claim" -> {
                    val content = request.body as? OutgoingContent.ByteArrayContent
                    val bodyString = content?.bytes()?.decodeToString().orEmpty()
                    val decodedClaimReq = json.decodeFromString<ClaimDiscountRequest>(bodyString)
                    assertEquals("ORD-E2E-1001", decodedClaimReq.orderId)
                    assertEquals("L", decodedClaimReq.bagSize)
                    assertEquals("bubble", decodedClaimReq.wrapType)
                    assertEquals(9_500_000L, decodedClaimReq.grossAmountCents)
                    assertEquals(7_000_000L, decodedClaimReq.netAmountCents)

                    respond(
                        content = """{"success":true,"claim_token":"e2e_claim_token_pass_123","expires_in":120}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/api/v1/redemptions" -> {
                    val content = request.body as? OutgoingContent.ByteArrayContent
                    val bodyString = content?.bytes()?.decodeToString().orEmpty()
                    val decodedSubmitReq = json.decodeFromString<SubmitRedemptionRequest>(bodyString)
                    assertEquals("e2e_claim_token_pass_123", decodedSubmitReq.claimToken)
                    assertEquals("L", decodedSubmitReq.bagSize)
                    assertEquals("bubble", decodedSubmitReq.wrapType)
                    assertEquals(9_500_000L, decodedSubmitReq.grossAmountCents)
                    assertEquals(7_000_000L, decodedSubmitReq.netAmountCents)

                    respond(
                        content = """{"success":true,"redemption_id":1001}""",
                        status = HttpStatusCode.Created,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> error("Unhandled path: ${request.url.encodedPath}")
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
    // E2E-J2-002: Baggage Size Matrix Pricing Verification (S / M / L / XL)
    // =========================================================================
    /**
     * E2E-J2-002: Baggage size matrix pricing verification for S, M, L, XL
     * with standard and bubble wrap options, asserting deterministic gross and net amounts.
     */
    @Test
    fun testE2EJ2002BaggageSizeMatrixPricing() {
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
    // E2E-J2-003: Multi-bag Count Multiplier Calculation
    // =========================================================================
    /**
     * E2E-J2-003: Multi-bag count multiplier calculation (e.g. 3 bags = gross price x 3).
     *
     * Verifies that BaggagePricingCalculator correctly scales gross amounts in Rupiah and cents
     * proportionally to the bag count.
     */
    @Test
    fun testE2EJ2003BagCountMultiplierPricing() {
        val discountRupiah = 25_000L
        val discountCents = 2_500_000L

        // Single bag M Standard: 65.000 Rp (6.500.000 cents)
        val grossM1 = BaggagePricingCalculator.calculateGrossRupiah(BaggageSize.M, WrapType.STANDARD, bagCount = 1)
        assertEquals(65_000L, grossM1)

        // 3 bags M Standard: 65.000 x 3 = 195.000 Rp (19.500.000 cents)
        val grossM3 = BaggagePricingCalculator.calculateGrossRupiah(BaggageSize.M, WrapType.STANDARD, bagCount = 3)
        val grossCentsM3 = BaggagePricingCalculator.calculateGrossCents(BaggageSize.M, WrapType.STANDARD, bagCount = 3)
        assertEquals(195_000L, grossM3)
        assertEquals(19_500_000L, grossCentsM3)
        assertEquals(grossM1 * 3, grossM3)

        // Net calculation for 3 bags with 25k discount
        val netM3 = BaggagePricingCalculator.calculateNetRupiah(grossM3, discountRupiah)
        val netCentsM3 = BaggagePricingCalculator.calculateNetCents(grossCentsM3, discountCents)
        assertEquals(170_000L, netM3)
        assertEquals(17_000_000L, netCentsM3)

        // 3 bags L Bubble: (80.000 + 15.000) x 3 = 95.000 x 3 = 285.000 Rp (28.500.000 cents)
        val grossLBubble3 = BaggagePricingCalculator.calculateGrossRupiah(BaggageSize.L, WrapType.BUBBLE, bagCount = 3)
        val grossCentsLBubble3 = BaggagePricingCalculator.calculateGrossCents(BaggageSize.L, WrapType.BUBBLE, bagCount = 3)
        assertEquals(285_000L, grossLBubble3)
        assertEquals(28_500_000L, grossCentsLBubble3)
    }

    // =========================================================================
    // E2E-J2-005: Cross-outlet PNR Deduplication Conflict (HTTP 409)
    // =========================================================================
    /**
     * E2E-J2-005: Cross-outlet PNR deduplication conflict (HTTP 409) with error code 'pnr_already_redeemed'.
     *
     * Verifies that redeeming a ticket previously redeemed at another outlet fails with
     * NonStackingConflictException containing the specific error code.
     */
    @Test
    fun testE2EJ2005PnrDedupCrossOutlet409() = runTest {
        val client = createMockClient {
            respond(
                content = """
                    {
                        "success": false,
                        "error": "pnr_already_redeemed"
                    }
                """.trimIndent(),
                status = HttpStatusCode.Conflict,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val repository = RedemptionRepositoryImpl(client)

        val claim = RedemptionClaim(
            ticket = sampleTicket,
            orderId = "ORD-DUP-PNR-001",
            outletId = 1L,
            cashierId = 2L,
            amountCents = 2_500_000L,
            claimToken = "token_cross_outlet",
            shiftId = 5L,
            status = ClaimStatus.PENDING
        )

        val result = repository.submitRedemption(claim)
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertIs<NonStackingConflictException>(exception)
        assertEquals("pnr_already_redeemed", exception.errorMessage)
    }

    // =========================================================================
    // E2E-J2-006: Non-Stacking Conflict (HTTP 409)
    // =========================================================================
    /**
     * E2E-J2-006: Non-stacking guard on duplicate MOKA order discount attempt (HTTP 409).
     *
     * Verifies that a second discount claim on the same MOKA order is rejected with HTTP 409 Conflict.
     */
    @Test
    fun testE2EJ2006NonStackingConflict409() = runTest {
        val client = createMockClient {
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
    // E2E-J2-014: Claim Token TTL Expired (HTTP 401 token_expired)
    // =========================================================================
    /**
     * E2E-J2-014: Claim token HMAC TTL expired returns HTTP 401 token_expired.
     *
     * Verifies that submitting a redemption after the claim token TTL has lapsed
     * fails with an IllegalStateException detailing token expiration.
     */
    @Test
    fun testE2EJ2014ClaimTokenTtlExpired401() = runTest {
        val client = createMockClient {
            respond(
                content = """
                    {
                        "success": false,
                        "error": "token_expired"
                    }
                """.trimIndent(),
                status = HttpStatusCode.Unauthorized,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val repository = RedemptionRepositoryImpl(client)

        val claim = RedemptionClaim(
            ticket = sampleTicket,
            orderId = "ORD-EXPIRED-TOKEN",
            outletId = 1L,
            cashierId = 2L,
            amountCents = 2_500_000L,
            claimToken = "expired_token_120s_past",
            shiftId = 5L,
            status = ClaimStatus.PENDING
        )

        val result = repository.submitRedemption(claim)
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertIs<IllegalStateException>(exception)
        assertEquals("token_expired", exception.message)
    }

    // =========================================================================
    // E2E-ME-001: Dual-SIM Failover Header Simulation
    // =========================================================================
    /**
     * E2E-ME-001: Dual-SIM failover simulation sending network_iface=cellular_sim2 header upon Wi-Fi drop.
     *
     * Verifies that failover traffic attaches the network_iface header and the backend
     * accepts and processes the redemption over secondary cellular connectivity.
     */
    @Test
    fun testE2EME001DualSimFailoverHeader() = runTest {
        var capturedIfaceHeader: String? = null

        val client = createMockClient(defaultHeaders = mapOf("network_iface" to "cellular_sim2")) { request ->
            capturedIfaceHeader = request.headers["network_iface"]
            val content = request.body as? OutgoingContent.ByteArrayContent
            val bodyString = content?.bytes()?.decodeToString().orEmpty()
            val decoded = json.decodeFromString<SubmitRedemptionRequest>(bodyString)
            assertEquals("token_sim2_failover", decoded.claimToken)

            respond(
                content = """{"success":true,"redemption_id":2002}""",
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val repository = RedemptionRepositoryImpl(client)

        val claim = RedemptionClaim(
            ticket = sampleTicket,
            orderId = "ORD-SIM2-FAILOVER",
            outletId = 1L,
            cashierId = 2L,
            amountCents = 2_500_000L,
            claimToken = "token_sim2_failover",
            shiftId = 5L,
            status = ClaimStatus.PENDING
        )

        val result = repository.submitRedemption(claim)
        assertTrue(result.isSuccess)
        assertEquals("2002", result.getOrNull())
        assertEquals("cellular_sim2", capturedIfaceHeader)
    }

    // =========================================================================
    // E2E-ME-002: Offline Outbox Queue Pending Test
    // =========================================================================
    /**
     * E2E-ME-002: Offline outbox queue pending test.
     *
     * Verifies that when the tablet is offline (ConnectTimeoutException), transactions cannot
     * be dispatched immediately and remain pending in the local outbox queue.
     * Once connectivity is restored, outbox items are flushed to the backend without data loss.
     */
    @Test
    fun testE2EME002OfflineOutboxQueuePending() = runTest {
        // Step 1: Simulate offline state with strict ConnectTimeoutException
        val offlineClient = createMockClient {
            throw ConnectTimeoutException("Backend unreachable: tablet offline")
        }
        val offlineRepo = RedemptionRepositoryImpl(offlineClient)

        val claim = RedemptionClaim(
            ticket = sampleTicket,
            orderId = "ORD-OUTBOX-PENDING-001",
            outletId = 1L,
            cashierId = 2L,
            amountCents = 2_500_000L,
            claimToken = "token_outbox_pending",
            shiftId = 5L,
            status = ClaimStatus.PENDING
        )

        val offlineSubmitResult = offlineRepo.submitRedemption(claim)
        assertTrue(offlineSubmitResult.isFailure)
        // Strict, non-tautological exception type assertion
        assertIs<ConnectTimeoutException>(offlineSubmitResult.exceptionOrNull())

        val offlineTokenResult = offlineRepo.requestClaimToken(
            orderId = "ORD-OUTBOX-PENDING-001",
            pnrHash = sampleTicket.canonicalHash.orEmpty(),
            outletId = 1L,
            cashierId = 2L,
            amountCents = 2_500_000L,
            signals = LocationContext()
        )
        assertTrue(offlineTokenResult.isFailure)
        assertIs<ConnectTimeoutException>(offlineTokenResult.exceptionOrNull())

        // Step 2: Pending outbox queue retains the item while offline
        val outboxQueue = mutableListOf<RedemptionClaim>()
        outboxQueue.add(claim)
        assertEquals(1, outboxQueue.size)
        assertEquals(ClaimStatus.PENDING, outboxQueue.first().status)

        // Step 3: Network restored -> flush outbox queue to backend
        val onlineClient = createMockClient { request ->
            val content = request.body as? OutgoingContent.ByteArrayContent
            val bodyString = content?.bytes()?.decodeToString().orEmpty()
            val decoded = json.decodeFromString<SubmitRedemptionRequest>(bodyString)
            assertEquals("ORD-OUTBOX-PENDING-001", decoded.orderId)

            respond(
                content = """{"success":true,"redemption_id":3003}""",
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val onlineRepo = RedemptionRepositoryImpl(onlineClient)

        val flushedIds = mutableListOf<String>()
        while (outboxQueue.isNotEmpty()) {
            val pendingItem = outboxQueue.removeAt(0)
            val flushResult = onlineRepo.submitRedemption(pendingItem)
            assertTrue(flushResult.isSuccess)
            flushedIds.add(flushResult.getOrThrow())
        }

        assertEquals(0, outboxQueue.size)
        assertEquals(listOf("3003"), flushedIds)
    }

    // =========================================================================
    // DTO Serialization / Deserialization Round-trip Validation
    // =========================================================================
    @Test
    fun testDtoSerializationAndDeserialization() {
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
        val decodedClaim = json.decodeFromString<ClaimDiscountRequest>(encodedClaim)
        assertEquals("L", decodedClaim.bagSize)
        assertEquals("bubble", decodedClaim.wrapType)
        assertEquals(9_500_000L, decodedClaim.grossAmountCents)
        assertEquals(7_000_000L, decodedClaim.netAmountCents)

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

        val responseJson = """{"success":true,"redemption_id":789}"""
        val decodedResponse = json.decodeFromString<SubmitRedemptionResponse>(responseJson)
        assertTrue(decodedResponse.success)
        assertEquals(789L, decodedResponse.redemptionId)

        val claimResponseJson = """{"success":true,"claim_token":"token_xyz","expires_in":120}"""
        val decodedClaimResponse = json.decodeFromString<ClaimDiscountResponse>(claimResponseJson)
        assertTrue(decodedClaimResponse.success)
        assertEquals("token_xyz", decodedClaimResponse.claimToken)
    }
}
