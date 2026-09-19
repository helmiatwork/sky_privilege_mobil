package com.skyprivilege.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class MainAppTabsTest {

    @Test
    fun testTabDefinitions() {
        assertEquals(4, AppTab.values().size)
        assertEquals("Home", AppTab.HOME.title)
        assertEquals("History", AppTab.HISTORY.title)
        assertEquals("Scan", AppTab.SCAN.title)
        assertEquals("Absen", AppTab.ABSEN.title)
    }

    @Test
    fun testFormatRupiah() {
        assertEquals("25.000", formatRupiah(25000L))
        assertEquals("150.000", formatRupiah(150000L))
        assertEquals("1.000.000", formatRupiah(1000000L))
        assertEquals("0", formatRupiah(0L))
    }
}
