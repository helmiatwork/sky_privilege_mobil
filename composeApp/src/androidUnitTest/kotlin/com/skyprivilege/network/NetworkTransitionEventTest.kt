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

    @Test
    fun testComputeTransitionWifiToCellularFailover() {
        val event = computeTransition(oldType = NetworkType.WIFI, newType = NetworkType.CELLULAR)
        kotlin.test.assertNotNull(event)
        assertEquals(NetworkType.WIFI, event.previousType)
        assertEquals(NetworkType.CELLULAR, event.currentType)
        assertTrue(event.isFailover, "Wi-Fi to Cellular must be marked as failover")
    }

    @Test
    fun testComputeTransitionCellularToWifi() {
        val event = computeTransition(oldType = NetworkType.CELLULAR, newType = NetworkType.WIFI)
        kotlin.test.assertNotNull(event)
        assertEquals(NetworkType.CELLULAR, event.previousType)
        assertEquals(NetworkType.WIFI, event.currentType)
        assertFalse(event.isFailover, "Cellular to Wi-Fi must not be marked as failover")
    }

    @Test
    fun testComputeTransitionConnectionLossToNone() {
        val wifiLoss = computeTransition(oldType = NetworkType.WIFI, newType = NetworkType.NONE)
        kotlin.test.assertNotNull(wifiLoss)
        assertEquals(NetworkType.WIFI, wifiLoss.previousType)
        assertEquals(NetworkType.NONE, wifiLoss.currentType)
        assertFalse(wifiLoss.isFailover)

        val cellLoss = computeTransition(oldType = NetworkType.CELLULAR, newType = NetworkType.NONE)
        kotlin.test.assertNotNull(cellLoss)
        assertEquals(NetworkType.CELLULAR, cellLoss.previousType)
        assertEquals(NetworkType.NONE, cellLoss.currentType)
        assertFalse(cellLoss.isFailover)
    }

    @Test
    fun testComputeTransitionIdempotencyNoEventWhenUnchanged() {
        kotlin.test.assertNull(computeTransition(NetworkType.WIFI, NetworkType.WIFI))
        kotlin.test.assertNull(computeTransition(NetworkType.CELLULAR, NetworkType.CELLULAR))
        kotlin.test.assertNull(computeTransition(NetworkType.NONE, NetworkType.NONE))
        kotlin.test.assertNull(computeTransition(NetworkType.OTHER, NetworkType.OTHER))
    }

    @Test
    fun testResolveNetworkTypeHierarchy() {
        // Wi-Fi takes precedence
        assertEquals(NetworkType.WIFI, resolveNetworkType(hasWifi = true, hasCellular = false, hasEthernet = false))
        assertEquals(NetworkType.WIFI, resolveNetworkType(hasWifi = true, hasCellular = true, hasEthernet = true))

        // Cellular next
        assertEquals(NetworkType.CELLULAR, resolveNetworkType(hasWifi = false, hasCellular = true, hasEthernet = false))
        assertEquals(NetworkType.CELLULAR, resolveNetworkType(hasWifi = false, hasCellular = true, hasEthernet = true))

        // Ethernet / Other
        assertEquals(NetworkType.OTHER, resolveNetworkType(hasWifi = false, hasCellular = false, hasEthernet = true))

        // No network connection
        assertEquals(NetworkType.NONE, resolveNetworkType(hasWifi = false, hasCellular = false, hasEthernet = false))
    }
}
