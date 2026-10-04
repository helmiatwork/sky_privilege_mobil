package com.skyprivilege.domain.model

import com.skyprivilege.data.remote.dto.ClaimDiscountRequest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BaggageWrapPricingTest {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @Test
    fun testBaggageSizePricing() {
        assertEquals(60_000L, BaggageSize.S.priceRupiah)
        assertEquals(6_000_000L, BaggageSize.S.priceCents)

        assertEquals(75_000L, BaggageSize.M.priceRupiah)
        assertEquals(7_500_000L, BaggageSize.M.priceCents)

        assertEquals(90_000L, BaggageSize.L.priceRupiah)
        assertEquals(9_000_000L, BaggageSize.L.priceCents)

        assertEquals(120_000L, BaggageSize.XL.priceRupiah)
        assertEquals(12_000_000L, BaggageSize.XL.priceCents)
    }

    @Test
    fun testWrapTypePricing() {
        assertEquals(0L, WrapType.STANDARD.extraPriceRupiah)
        assertEquals(0L, WrapType.STANDARD.extraPriceCents)

        assertEquals(15_000L, WrapType.PREMIUM.extraPriceRupiah)
        assertEquals(1_500_000L, WrapType.PREMIUM.extraPriceCents)

        assertEquals(10_000L, WrapType.BUBBLE.extraPriceRupiah)
        assertEquals(1_000_000L, WrapType.BUBBLE.extraPriceCents)
    }

    @Test
    fun testBaggagePricingCalculatorDefaultMStandard() {
        val grossCents = BaggagePricingCalculator.calculateGrossCents(BaggageSize.M, WrapType.STANDARD)
        val netCents = BaggagePricingCalculator.calculateNetCents(grossCents, 2_500_000L)

        assertEquals(7_500_000L, grossCents)
        assertEquals(5_000_000L, netCents)
    }

    @Test
    fun testBaggagePricingCalculatorSlide21ScenarioLargeStandard() {
        // Slide 21 Pitch Deck: "Order Baggage Wrapping Rp 90.000, Promo discount -Rp 25.000, Total bayar Rp 65.000"
        val grossRupiah = BaggagePricingCalculator.calculateGrossRupiah(BaggageSize.L, WrapType.STANDARD)
        val grossCents = BaggagePricingCalculator.calculateGrossCents(BaggageSize.L, WrapType.STANDARD)
        val netRupiah = BaggagePricingCalculator.calculateNetRupiah(grossRupiah, 25_000L)
        val netCents = BaggagePricingCalculator.calculateNetCents(grossCents, 2_500_000L)

        assertEquals(90_000L, grossRupiah)
        assertEquals(9_000_000L, grossCents)
        assertEquals(65_000L, netRupiah)
        assertEquals(6_500_000L, netCents)
    }

    @Test
    fun testBaggagePricingCalculatorMediumBubbleWrap() {
        // Medium (75.000) + Bubble Wrap (10.000) = 85.000 gross, Net after 25.000 discount = 60.000
        val grossCents = BaggagePricingCalculator.calculateGrossCents(BaggageSize.M, WrapType.BUBBLE)
        val netCents = BaggagePricingCalculator.calculateNetCents(grossCents, 2_500_000L)

        assertEquals(8_500_000L, grossCents)
        assertEquals(6_000_000L, netCents)
    }

    @Test
    fun testBaggagePricingCalculatorExtraLargeBubbleWrap() {
        // XL (120.000) + Bubble (10.000) = 130.000 gross, Net after 25.000 discount = 105.000
        val grossCents = BaggagePricingCalculator.calculateGrossCents(BaggageSize.XL, WrapType.BUBBLE)
        val netCents = BaggagePricingCalculator.calculateNetCents(grossCents, 2_500_000L)

        assertEquals(13_000_000L, grossCents)
        assertEquals(10_500_000L, netCents)
    }

    @Test
    fun testBaggagePricingCalculatorSmallStandard() {
        // S (60.000) + Standard (0) = 60.000 gross, Net after 25.000 discount = 35.000
        val grossCents = BaggagePricingCalculator.calculateGrossCents(BaggageSize.S, WrapType.STANDARD)
        val netCents = BaggagePricingCalculator.calculateNetCents(grossCents, 2_500_000L)

        assertEquals(6_000_000L, grossCents)
        assertEquals(3_500_000L, netCents)
    }

    @Test
    fun testClaimDiscountRequestDefaultBaggageWrapSerialization() {
        val request = ClaimDiscountRequest(
            orderId = "ORD-001",
            pnrHash = "pnr_123",
            outletId = 1L,
            cashierId = 2L,
            amountCents = 2_500_000L,
            signals = LocationContext()
        )

        assertEquals("M", request.bagSize)
        assertEquals("standard", request.wrapType)
        assertEquals("qris", request.paymentMethod)
        assertEquals(7_500_000L, request.grossAmountCents)
        assertEquals(5_000_000L, request.netAmountCents)

        val encoded = json.encodeToString(request)
        assertTrue(encoded.contains("\"bag_size\":\"M\""))
        assertTrue(encoded.contains("\"wrap_type\":\"standard\""))
        assertTrue(encoded.contains("\"payment_method\":\"qris\""))
        assertTrue(encoded.contains("\"gross_amount_cents\":7500000"))
        assertTrue(encoded.contains("\"net_amount_cents\":5000000"))

        val decoded = json.decodeFromString<ClaimDiscountRequest>(encoded)
        assertEquals("M", decoded.bagSize)
        assertEquals("standard", decoded.wrapType)
        assertEquals(7_500_000L, decoded.grossAmountCents)
        assertEquals(5_000_000L, decoded.netAmountCents)
    }

    @Test
    fun testRedemptionClaimDefaultAndCustomBaggageWrapValues() {
        val defaultClaim = RedemptionClaim(
            ticket = Ticket(
                passengerName = "DOE/JOHN MR",
                pnr = "XYZ123",
                fromAirport = "CGK",
                toAirport = "DPS",
                operatingCarrier = "GA",
                flightNumber = "GA402",
                flightDate = "2026-09-19",
                compartmentCode = "Y",
                seatNumber = "12A",
                checkInSequence = "001"
            ),
            orderId = "ORD-002",
            outletId = 1L,
            cashierId = 2L,
            amountCents = 2_500_000L
        )

        assertEquals("M", defaultClaim.bagSize)
        assertEquals("standard", defaultClaim.wrapType)
        assertEquals("qris", defaultClaim.paymentMethod)
        assertEquals(7_500_000L, defaultClaim.grossAmountCents)
        assertEquals(5_000_000L, defaultClaim.netAmountCents)

        val customClaim = defaultClaim.copy(
            bagSize = "L",
            wrapType = "bubble",
            paymentMethod = "cash",
            grossAmountCents = 10_000_000L,
            netAmountCents = 7_500_000L
        )

        assertEquals("L", customClaim.bagSize)
        assertEquals("bubble", customClaim.wrapType)
        assertEquals("cash", customClaim.paymentMethod)
        assertEquals(10_000_000L, customClaim.grossAmountCents)
        assertEquals(7_500_000L, customClaim.netAmountCents)

        val encoded = json.encodeToString(customClaim)
        val decoded = json.decodeFromString<RedemptionClaim>(encoded)
        assertEquals("L", decoded.bagSize)
        assertEquals("bubble", decoded.wrapType)
        assertEquals("cash", decoded.paymentMethod)
        assertEquals(10_000_000L, decoded.grossAmountCents)
        assertEquals(7_500_000L, decoded.netAmountCents)
    }
}
