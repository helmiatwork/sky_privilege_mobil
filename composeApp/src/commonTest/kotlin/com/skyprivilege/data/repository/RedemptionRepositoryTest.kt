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

    @Test
    fun testGetRedemptionsSuccess() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/redemptions", request.url.encodedPath)
            assertEquals("2", request.url.parameters["cashier_id"])
            respond(
                content = """
                    {
                        "success": true,
                        "redemptions": [
                            {
                                "id": 101,
                                "pnr_canonical_hash": "f7f12381abcde",
                                "pnr_masked": "f7f12381...",
                                "flight_number": "GA410",
                                "outlet_name": "Sky Lounge Terminal 3 CGK",
                                "cashier_name": "Kasir Terminal 3",
                                "discount_amount": 25000.0,
                                "status": "approved",
                                "is_overridden": false,
                                "created_at": "2026-09-19T15:43:00Z",
                                "date_formatted": "19 Sep 2026",
                                "time_formatted": "15:43 WIB"
                            }
                        ]
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
        val result = repository.getRedemptions(cashierId = 2L)

        assertTrue(result.isSuccess)
        val list = result.getOrNull()
        kotlin.test.assertNotNull(list)
        assertEquals(1, list.size)
        val item = list.first()
        assertEquals(101L, item.id)
        assertEquals("f7f12381...", item.pnrMasked)
        assertEquals("GA410", item.flightNumber)
        assertEquals(25000.0, item.discountAmount)
        assertEquals("15:43 WIB", item.timeFormatted)
    }

    @Test
    fun testRequestClaimTokenIncludesTicketPhotoDataInRequestBody() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/redemptions/claim", request.url.encodedPath)
            val content = request.body as? io.ktor.http.content.OutgoingContent.ByteArrayContent
            val bodyString = content?.bytes()?.decodeToString().orEmpty()
            assertTrue(bodyString.contains("ticket_photo_data"))
            assertTrue(bodyString.contains("data:image/jpeg;base64,claim_photo_123"))
            respond(
                content = """
                    {
                        "success": true,
                        "claim_token": "token_abc_123",
                        "expires_in": 120
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
        val result = repository.requestClaimToken(
            orderId = "ORD-123",
            pnrHash = "hash123",
            outletId = 1L,
            cashierId = 2L,
            amountCents = 2500000L,
            signals = com.skyprivilege.domain.model.LocationContext(
                gps = com.skyprivilege.domain.model.GpsCoordinate(latitude = -6.12, longitude = 106.65, accuracyMeters = 10f, isMock = false),
                wifi = com.skyprivilege.domain.model.WifiContext(bssid = "00:11:22:33:44:55", ssid = "TestWiFi", rssiDbm = -50),
                deviceIntegrity = com.skyprivilege.domain.model.DeviceIntegrityContext(deviceRecognition = "MEETS_BASIC_INTEGRITY", isRooted = false, isEmulator = false)
            ),
            ticketPhotoData = "data:image/jpeg;base64,claim_photo_123"
        )

        assertTrue(result.isSuccess)
        assertEquals("token_abc_123", result.getOrNull())
    }

    @Test
    fun testSubmitRedemptionIncludesTicketPhotoDataInRequestBody() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/redemptions", request.url.encodedPath)
            val content = request.body as? io.ktor.http.content.OutgoingContent.ByteArrayContent
            val bodyString = content?.bytes()?.decodeToString().orEmpty()
            assertTrue(bodyString.contains("ticket_photo_data"))
            assertTrue(bodyString.contains("data:image/jpeg;base64,submit_photo_456"))
            respond(
                content = """
                    {
                        "success": true,
                        "redemption_id": 888
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
            orderId = "MOKA-1002",
            outletId = 1L,
            cashierId = 2L,
            amountCents = 5000000L,
            claimToken = "valid_token_xyz",
            shiftId = 42L,
            status = ClaimStatus.PENDING,
            ticketPhotoData = "data:image/jpeg;base64,submit_photo_456"
        )

        val result = repository.submitRedemption(claim)
        assertTrue(result.isSuccess)
        assertEquals("888", result.getOrNull())
    }
}
