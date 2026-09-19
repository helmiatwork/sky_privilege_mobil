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

class AttendanceRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testGetTodayAttendanceSuccess() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/attendances/today", request.url.encodedPath)
            assertEquals("2", request.url.parameters["cashier_id"])
            assertEquals("2", request.url.parameters["outlet_id"])
            respond(
                content = """
                    {
                        "success": true,
                        "today": "2026-09-19",
                        "has_checked_in": true,
                        "has_checked_out": false,
                        "attendance": {
                            "id": 501,
                            "cashier_id": 2,
                            "outlet_id": 2,
                            "date": "2026-09-19",
                            "check_in_at": "2026-09-19T07:05:00Z",
                            "check_out_at": null,
                            "status": "present",
                            "duration_minutes": 0,
                            "check_in_latitude": -6.1256,
                            "check_in_longitude": 106.6558,
                            "check_in_accuracy": 15.0
                        }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val repository = AttendanceRepositoryImpl(client)
        val result = repository.getTodayAttendance(cashierId = 2L, outletId = 2L)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response.success)
        assertTrue(response.hasCheckedIn)
        assertEquals("2026-09-19", response.today)
        assertEquals(501L, response.attendance?.id)
        assertEquals("present", response.attendance?.status)
    }

    @Test
    fun testRecordAttendanceCheckInSuccess() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/attendances/record_time", request.url.encodedPath)
            respond(
                content = """
                    {
                        "success": true,
                        "message": "Absensi masuk berhasil dicatat",
                        "action": "check_in",
                        "attendance": {
                            "id": 502,
                            "cashier_id": 2,
                            "outlet_id": 2,
                            "date": "2026-09-19",
                            "check_in_at": "2026-09-19T07:00:00Z",
                            "status": "present",
                            "duration_minutes": 0,
                            "check_in_latitude": -6.1256,
                            "check_in_longitude": 106.6558,
                            "check_in_accuracy": 12.0
                        }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val repository = AttendanceRepositoryImpl(client)
        val result = repository.recordAttendance(
            cashierId = 2L,
            outletId = 2L,
            type = "check_in",
            latitude = -6.1256,
            longitude = 106.6558,
            accuracy = 12.0f
        )

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response.success)
        assertEquals("check_in", response.action)
        assertEquals(502L, response.attendance?.id)
    }

    @Test
    fun testSubmitCorrectionRequestSuccess() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/attendance_corrections", request.url.encodedPath)
            respond(
                content = """
                    {
                        "success": true,
                        "message": "Pengajuan koreksi absensi berhasil dikirim",
                        "correction_request": {
                            "id": 99,
                            "status": "pending",
                            "correction_type": "check_in",
                            "target_date": "2026-09-19",
                            "requested_check_in_at": "2026-09-19 07:00:00",
                            "reason": "Lupa check-in karena langsung melayani antrean boarding"
                        }
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val repository = AttendanceRepositoryImpl(client)
        val result = repository.submitCorrectionRequest(
            cashierId = 2L,
            outletId = 2L,
            targetDate = "2026-09-19",
            correctionType = "check_in",
            requestedCheckInAt = "2026-09-19 07:00:00",
            requestedCheckOutAt = null,
            reason = "Lupa check-in karena langsung melayani antrean boarding"
        )

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertNotNull(response)
        assertTrue(response.success)
        assertEquals(99L, response.correctionRequest?.id)
        assertEquals("pending", response.correctionRequest?.status)
    }

    @Test
    fun testGetAttendanceHistorySuccess() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/api/v1/attendances", request.url.encodedPath)
            assertEquals("2", request.url.parameters["cashier_id"])
            respond(
                content = """
                    {
                        "success": true,
                        "attendances": [
                            {
                                "id": 505,
                                "date": "2026-09-19",
                                "date_formatted": "Sat, 19 Sep 2026",
                                "check_in_at": "06:05",
                                "check_out_at": "15:00",
                                "status": "completed",
                                "check_in_lat": -6.125605,
                                "check_in_lng": 106.655805,
                                "check_in_accuracy": 12.0
                            }
                        ]
                    }
                """.trimIndent(),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }

        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }

        val repository = AttendanceRepositoryImpl(client)
        val result = repository.getAttendanceHistory(cashierId = 2L)

        assertTrue(result.isSuccess)
        val list = result.getOrNull()
        assertNotNull(list)
        assertEquals(1, list.size)
        val item = list.first()
        assertEquals(505L, item.id)
        assertEquals("Sat, 19 Sep 2026", item.dateFormatted)
        assertEquals("06:05", item.checkInAt)
        assertEquals("completed", item.status)
    }
}
