package com.skyprivilege.ui

import com.skyprivilege.domain.model.BaggagePricingCalculator
import com.skyprivilege.domain.model.BaggageSize
import com.skyprivilege.domain.model.WrapType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TicketValidBaggageWrapStateTest {

    @Test
    fun testDefaultStateValues() {
        val defaultSize = BaggageSize.M
        val defaultWrap = WrapType.STANDARD
        val discountAmountCents = 2_500_000L

        val grossCents = BaggagePricingCalculator.calculateGrossCents(defaultSize, defaultWrap)
        val netCents = BaggagePricingCalculator.calculateNetCents(grossCents, discountAmountCents)

        assertEquals("M", defaultSize.code)
        assertEquals(75_000L, defaultSize.priceRupiah)
        assertEquals(7_500_000L, grossCents)
        assertEquals(5_000_000L, netCents)
        assertEquals("50.000", formatRupiah(netCents / 100))
    }

    @Test
    fun testSlide21PitchDeckPriceCalculation() {
        // Slide 21 Pitch Deck:
        // "MOKA POS · Order Baggage Wrapping Rp 80.000, Promo discount -Rp 25.000, Total bayar Rp 55.000"
        val sizeL = BaggageSize.L
        val wrapStandard = WrapType.STANDARD
        val discountCents = 2_500_000L
        val discountRupiah = 25_000L

        val grossRupiah = BaggagePricingCalculator.calculateGrossRupiah(sizeL, wrapStandard)
        val grossCents = BaggagePricingCalculator.calculateGrossCents(sizeL, wrapStandard)
        val netRupiah = BaggagePricingCalculator.calculateNetRupiah(grossRupiah, discountRupiah)
        val netCents = BaggagePricingCalculator.calculateNetCents(grossCents, discountCents)

        assertEquals(90_000L, grossRupiah)
        assertEquals(9_000_000L, grossCents)
        assertEquals(65_000L, netRupiah)
        assertEquals(6_500_000L, netCents)
        assertEquals("90.000", formatRupiah(grossRupiah))
        assertEquals("65.000", formatRupiah(netRupiah))
    }

    @Test
    fun testBubbleWrapAdditionalPricing() {
        val sizeM = BaggageSize.M
        val wrapBubble = WrapType.BUBBLE
        val discountCents = 2_500_000L

        val grossCents = BaggagePricingCalculator.calculateGrossCents(sizeM, wrapBubble)
        val netCents = BaggagePricingCalculator.calculateNetCents(grossCents, discountCents)

        assertEquals(8_500_000L, grossCents)
        assertEquals(6_000_000L, netCents)
    }

    @Test
    fun testAllBaggageSizesAvailable() {
        val sizes = BaggageSize.entries
        assertEquals(4, sizes.size)
        assertEquals(listOf("S", "M", "L", "XL"), sizes.map { it.code })
        assertEquals(listOf(60_000L, 75_000L, 90_000L, 120_000L), sizes.map { it.priceRupiah })
    }

    @Test
    fun testAllWrapTypesAvailable() {
        val types = WrapType.entries
        assertEquals(3, types.size)
        assertEquals(listOf("standard", "premium", "bubble"), types.map { it.code })
        assertEquals(listOf(0L, 15_000L, 10_000L), types.map { it.extraPriceRupiah })
    }

    @Test
    fun testFormatRupiahOutput() {
        assertEquals("50.000", formatRupiah(50_000L))
        assertEquals("65.000", formatRupiah(65_000L))
        assertEquals("80.000", formatRupiah(80_000L))
        assertEquals("100.000", formatRupiah(100_000L))
        assertEquals("115.000", formatRupiah(115_000L))
        assertEquals("25.000", formatRupiah(25_000L))
        assertEquals("55.000", formatRupiah(55_000L))
    }
}
