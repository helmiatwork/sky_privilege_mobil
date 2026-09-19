package com.skyprivilege.data.repository

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
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ShiftRepositoryTest {

    @Test
    fun testOpenShiftSuccess() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/shifts/open", request.url.encodedPath)
            respond(
                content = """
                    {
                        "success": true,
                        "shift": {
                            "id": 1001,
                            "cashier_id": 5,
                            "outlet_id": 10,
                            "opened_at": "2026-09-19T08:00:00Z",
                            "status": "open"
                        }
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

        val repository = ShiftRepositoryImpl(client)
        val result = repository.openShift(cashierId = 5, outletId = 10, openingCash = 100000.0)

        assertTrue(result.isSuccess)
        val shift = result.getOrNull()
        assertNotNull(shift)
        assertEquals(1001L, shift.id)
        assertEquals("open", shift.status)
    }

    @Test
    fun testCloseShiftSuccess() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/shifts/close", request.url.encodedPath)
            respond(
                content = """
                    {
                        "success": true,
                        "z_report": {
                            "shift_id": 1001,
                            "cashier_id": 5,
                            "status": "closed",
                            "total_redemptions_count": 3,
                            "total_discount_cents": 15000000,
                            "total_discount_amount": 150000.0,
                            "closing_cash": 650000.0
                        }
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

        val repository = ShiftRepositoryImpl(client)
        val result = repository.closeShift(cashierId = 5, closingCash = 650000.0)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response?.zReport)
        assertEquals(1001L, response.zReport?.shiftId)
        assertEquals(3, response.zReport?.totalRedemptionsCount)
    }

    @Test
    fun testGetCurrentShiftSuccess() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/shifts/current", request.url.encodedPath)
            respond(
                content = """
                    {
                        "success": true,
                        "shift": {
                            "id": 1001,
                            "cashier_id": 5,
                            "outlet_id": 10,
                            "opened_at": "2026-09-19T08:00:00Z",
                            "status": "open"
                        },
                        "x_report": {
                            "shift_id": 1001,
                            "cashier_id": 5,
                            "outlet_id": 10,
                            "status": "open",
                            "total_redemptions_count": 2,
                            "total_discount_amount": 100000.0,
                            "total_discount_cents": 10000000
                        }
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

        val repository = ShiftRepositoryImpl(client)
        val result = repository.getCurrentShift(cashierId = 5)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response?.xReport)
        assertEquals(2, response.xReport?.totalRedemptionsCount)
    }
}
