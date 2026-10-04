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
        assertEquals("J*** M* D**", PiiMasker.maskName("DOE/JOHN MR"))
    }

    @Test
    fun maskName_fullIataName_masksCorrectly() {
        assertEquals("P**** M* P****", PiiMasker.maskName("PAX_A/PAX_B MR"))
    }

    @Test
    fun maskName_surnameFirst_formatsCorrectly() {
        assertEquals("F**** S******", PiiMasker.maskName("SURNAME/FIRST"))
    }

    @Test
    fun maskName_standardSpaceSeparated_masksWords() {
        assertEquals("J*** D**", PiiMasker.maskName("JOHN DOE"))
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
