package com.skyprivilege.ui

import com.skyprivilege.ui.components.FlatIconType
import com.skyprivilege.ui.components.getTabIcon
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class FlatIconsTest {

    @Test
    fun testAppTabHasNoEmojiIcons() {
        for (tab in AppTab.values()) {
            assertFalse(tab.title.contains("🏠"))
            assertFalse(tab.title.contains("💳"))
            assertFalse(tab.title.contains("📷"))
            assertFalse(tab.title.contains("⏱️"))
            assertFalse(tab.title.contains("👤"))
        }
    }

    @Test
    fun testFlatIconTypesDefined() {
        val types = FlatIconType.values()
        assertEquals(14, types.size)
        assertNotNull(FlatIconType.HOME)
        assertNotNull(FlatIconType.TRANSACTION)
        assertNotNull(FlatIconType.SCAN)
        assertNotNull(FlatIconType.ABSEN)
        assertNotNull(FlatIconType.PROFILE)
        assertNotNull(FlatIconType.CAMERA_SCAN)
        assertNotNull(FlatIconType.GPS_PIN)
        assertNotNull(FlatIconType.EDIT_NOTE)
        assertNotNull(FlatIconType.LOCK_SHIFT)
        assertNotNull(FlatIconType.CHECKLIST)
        assertNotNull(FlatIconType.VOUCHER_TICKET)
        assertNotNull(FlatIconType.HISTORY_CLOCK)
        assertNotNull(FlatIconType.BOOK_SOP)
        assertNotNull(FlatIconType.GARUDA_LOGO)
    }

    @Test
    fun testGarudaLogoIconTypeDefined() {
        assertNotNull(FlatIconType.GARUDA_LOGO)
        assertEquals("GARUDA_LOGO", FlatIconType.GARUDA_LOGO.name)
    }

    @Test
    fun testTabIconMapping() {
        assertEquals(FlatIconType.HOME, getTabIcon(AppTab.HOME))
        assertEquals(FlatIconType.TRANSACTION, getTabIcon(AppTab.HISTORY))
        assertEquals(FlatIconType.SCAN, getTabIcon(AppTab.SCAN))
        assertEquals(FlatIconType.ABSEN, getTabIcon(AppTab.ABSEN))
        assertEquals(FlatIconType.PROFILE, getTabIcon(AppTab.AKUN_SAYA))
    }
}
