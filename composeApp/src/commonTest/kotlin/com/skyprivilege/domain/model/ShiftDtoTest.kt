package com.skyprivilege.domain.model

import com.skyprivilege.data.remote.dto.CloseShiftResponse
import com.skyprivilege.data.remote.dto.CurrentShiftResponse
import com.skyprivilege.data.remote.dto.OpenShiftRequest
import com.skyprivilege.data.remote.dto.OpenShiftResponse
import com.skyprivilege.data.remote.dto.ShiftDto
import com.skyprivilege.data.remote.dto.XReportDto
import com.skyprivilege.data.remote.dto.ZReportDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ShiftDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testShiftStatusEnumValues() {
        assertEquals(ShiftStatus.OPEN, ShiftStatus.valueOf("OPEN"))
        assertEquals(ShiftStatus.CLOSED, ShiftStatus.valueOf("CLOSED"))
        assertEquals(ShiftStatus.FORCE_CLOSED, ShiftStatus.valueOf("FORCE_CLOSED"))
    }

    @Test
    fun testOpenShiftRequestAndResponseSerialization() {
        val request = OpenShiftRequest(
            cashierId = 12L,
            outletId = 34L,
            deviceId = 56L,
            openingCash = 500000.0,
            openingSelfieKey = "selfie_key_test",
            authMethodOpened = "face"
        )

        val encodedReq = json.encodeToString(request)
        val decodedReq = json.decodeFromString<OpenShiftRequest>(encodedReq)
        assertEquals(12L, decodedReq.cashierId)
        assertEquals(500000.0, decodedReq.openingCash)

        val responseJson = """
            {
                "success": true,
                "shift": {
                    "id": 99,
                    "cashier_id": 12,
                    "outlet_id": 34,
                    "device_id": 56,
                    "opened_at": "2026-09-19T08:00:00Z",
                    "status": "open",
                    "opening_cash": 500000.0
                }
            }
        """.trimIndent()

        val decodedRes = json.decodeFromString<OpenShiftResponse>(responseJson)
        assertTrue(decodedRes.success)
        assertNotNull(decodedRes.shift)
        assertEquals(99L, decodedRes.shift?.id)
        assertEquals("open", decodedRes.shift?.status)
    }

    @Test
    fun testCurrentShiftWithXReportSerialization() {
        val responseJson = """
            {
                "success": true,
                "shift": {
                    "id": 99,
                    "cashier_id": 12,
                    "outlet_id": 34,
                    "opened_at": "2026-09-19T08:00:00Z",
                    "status": "open"
                },
                "x_report": {
                    "shift_id": 99,
                    "cashier_id": 12,
                    "outlet_id": 34,
                    "status": "open",
                    "total_redemptions_count": 5,
                    "total_discount_amount": 250000.0,
                    "total_discount_cents": 25000000
                }
            }
        """.trimIndent()

        val decoded = json.decodeFromString<CurrentShiftResponse>(responseJson)
        assertTrue(decoded.success)
        assertEquals(5, decoded.xReport?.totalRedemptionsCount)
        assertEquals(25000000L, decoded.xReport?.totalDiscountCents)
    }

    @Test
    fun testCloseShiftWithZReportSerialization() {
        val responseJson = """
            {
                "success": true,
                "z_report": {
                    "shift_id": 99,
                    "cashier_id": 12,
                    "status": "closed",
                    "total_redemptions_count": 10,
                    "total_discount_cents": 50000000,
                    "total_discount_amount": 500000.0,
                    "opening_cash": 500000.0,
                    "closing_cash": 800000.0,
                    "closed_at": "2026-09-19T17:00:00Z"
                }
            }
        """.trimIndent()

        val decoded = json.decodeFromString<CloseShiftResponse>(responseJson)
        assertTrue(decoded.success)
        assertEquals(10, decoded.zReport?.totalRedemptionsCount)
        assertEquals(50000000L, decoded.zReport?.totalDiscountCents)
        assertEquals(800000.0, decoded.zReport?.closingCash)
    }
}
