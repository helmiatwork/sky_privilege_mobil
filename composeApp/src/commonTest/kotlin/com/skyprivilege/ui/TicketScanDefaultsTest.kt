package com.skyprivilege.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TicketScanDefaultsTest {

    @Test
    fun testCanSubmitBarcodeRejectsBlankOrNull() {
        assertFalse(TicketScanDefaults.canSubmitBarcode(null, isVerifying = false))
        assertFalse(TicketScanDefaults.canSubmitBarcode("", isVerifying = false))
        assertFalse(TicketScanDefaults.canSubmitBarcode("   ", isVerifying = false))
    }

    @Test
    fun testCanSubmitBarcodeRejectsWhenVerifying() {
        assertFalse(TicketScanDefaults.canSubmitBarcode("GA410", isVerifying = true))
    }

    @Test
    fun testCanSubmitBarcodeAcceptsValidDataWhenNotVerifying() {
        assertTrue(TicketScanDefaults.canSubmitBarcode("GA410", isVerifying = false))
        assertTrue(TicketScanDefaults.canSubmitBarcode(TicketScanDefaults.SAMPLE_DEMO_BARCODE, isVerifying = false))
    }

    @Test
    fun testSampleDemoBarcodeFormat() {
        val sample = TicketScanDefaults.SAMPLE_DEMO_BARCODE
        assertTrue(sample.isNotBlank())
        assertTrue(sample.contains("GA 00410"), "Sample barcode must contain Garuda GA 00410 flight number")
    }
}
