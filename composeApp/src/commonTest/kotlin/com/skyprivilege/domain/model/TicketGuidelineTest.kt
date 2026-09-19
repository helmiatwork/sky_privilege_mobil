package com.skyprivilege.domain.model

import com.skyprivilege.data.remote.dto.AuthenticityAcknowledgmentDto
import com.skyprivilege.data.remote.dto.AuthenticityAcknowledgmentResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TicketGuidelineTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testTicketGuidelineSerialization() {
        val guideline = TicketGuideline(
            id = 1L,
            title = "Kertas Fisik",
            description = "Tiket thermal cetak asli bandara",
            category = "physical_print",
            displayOrder = 1,
            active = true
        )

        val encoded = json.encodeToString(guideline)
        val decoded = json.decodeFromString<TicketGuideline>(encoded)

        assertEquals(guideline.id, decoded.id)
        assertEquals(guideline.title, decoded.title)
        assertEquals(guideline.description, decoded.description)
        assertEquals(guideline.category, decoded.category)
        assertEquals(guideline.displayOrder, decoded.displayOrder)
        assertTrue(decoded.active)
    }

    @Test
    fun testAuthenticityAcknowledgmentDtoSerialization() {
        val dto = AuthenticityAcknowledgmentDto(
            cashierId = 10L,
            outletId = 20L,
            deviceId = 30L,
            wifiBssid = "00:11:22:33:44:55",
            wifiSsid = "Airport_Staff",
            wifiRssi = -60,
            gpsLatitude = -6.1256,
            gpsLongitude = 106.6558,
            gpsAccuracy = 5.0f
        )

        val encoded = json.encodeToString(dto)
        val decoded = json.decodeFromString<AuthenticityAcknowledgmentDto>(encoded)

        assertEquals(10L, decoded.cashierId)
        assertEquals(20L, decoded.outletId)
        assertEquals(30L, decoded.deviceId)
        assertEquals("Airport_Staff", decoded.wifiSsid)
        assertEquals(-60, decoded.wifiRssi)
        assertEquals(-6.1256, decoded.gpsLatitude)
    }

    @Test
    fun testAuthenticityAcknowledgmentResponse() {
        val responseJson = """
            {
                "status": "success",
                "success": true,
                "acknowledgment_id": 42,
                "recorded_at": "2026-09-19T07:15:00Z"
            }
        """.trimIndent()

        val decoded = json.decodeFromString<AuthenticityAcknowledgmentResponse>(responseJson)
        assertEquals("success", decoded.status)
        assertTrue(decoded.success)
        assertEquals(42L, decoded.acknowledgmentId)
        assertEquals("2026-09-19T07:15:00Z", decoded.recordedAt)
    }
}
