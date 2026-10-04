package com.skyprivilege.pii

import kotlin.test.Test
import kotlin.test.assertEquals

class PiiMaskerTest {

    @Test
    fun maskName_null_returnsDash() = assertEquals("-", PiiMasker.maskName(null))

    @Test
    fun maskName_blank_returnsDash() = assertEquals("-", PiiMasker.maskName("   "))

    @Test
    fun maskName_dash_returnsDash() = assertEquals("-", PiiMasker.maskName("-"))

    @Test
    fun maskName_singleChar_returnsAsIs() = assertEquals("J", PiiMasker.maskName("J"))

    @Test
    fun maskName_multiWord_masksEachPart() {
        assertEquals("D******* M*", PiiMasker.maskName("DOE/JOHN MR"))
    }

    @Test
    fun maskName_fullIataName_masksCorrectly() {
        assertEquals("S*********** M*", PiiMasker.maskName("SANTOSO/BUDI MR"))
    }

    @Test
    fun maskPnr_null_returnsDash() = assertEquals("-", PiiMasker.maskPnr(null))

    @Test
    fun maskPnr_blank_returnsDash() = assertEquals("-", PiiMasker.maskPnr(""))

    @Test
    fun maskPnr_short_returnsAsIs() = assertEquals("AB", PiiMasker.maskPnr("AB"))

    @Test
    fun maskPnr_normal_masksMiddle() {
        assertEquals("AB***F", PiiMasker.maskPnr("ABCDEF"))
    }
}
