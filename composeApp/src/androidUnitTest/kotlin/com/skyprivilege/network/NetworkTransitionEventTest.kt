package com.skyprivilege.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NetworkTransitionEventTest {

    @Test
    fun testWifiToCellularIsFailover() {
        val event = NetworkTransitionEvent(
            previousType = NetworkType.WIFI,
            currentType = NetworkType.CELLULAR,
            isFailover = true,
            timestampMs = System.currentTimeMillis()
        )

        assertEquals(NetworkType.WIFI, event.previousType)
        assertEquals(NetworkType.CELLULAR, event.currentType)
        assertTrue(event.isFailover)
    }

    @Test
    fun testCellularToWifiIsNotFailover() {
        val event = NetworkTransitionEvent(
            previousType = NetworkType.CELLULAR,
            currentType = NetworkType.WIFI,
            isFailover = false,
            timestampMs = System.currentTimeMillis()
        )

        assertEquals(NetworkType.CELLULAR, event.previousType)
        assertEquals(NetworkType.WIFI, event.currentType)
        assertFalse(event.isFailover)
    }

    @Test
    fun testNetworkTypeEnumValues() {
        val types = NetworkType.values().map { it.name }
        assertTrue(types.contains("NONE"))
        assertTrue(types.contains("WIFI"))
        assertTrue(types.contains("CELLULAR"))
        assertTrue(types.contains("OTHER"))
    }
}
