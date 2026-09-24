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
        assertEquals(50_000L, BaggageSize.S.priceRupiah)
        assertEquals(5_000_000L, BaggageSize.S.priceCents)

        assertEquals(65_000L, BaggageSize.M.priceRupiah)
        assertEquals(6_500_000L, BaggageSize.M.priceCents)

        assertEquals(80_000L, BaggageSize.L.priceRupiah)
        assertEquals(8_000_000L, BaggageSize.L.priceCents)

        assertEquals(100_000L, BaggageSize.XL.priceRupiah)
        assertEquals(10_000_000L, BaggageSize.XL.priceCents)
    }

    @Test
    fun testWrapTypePricing() {
        assertEquals(0L, WrapType.STANDARD.extraPriceRupiah)
        assertEquals(0L, WrapType.STANDARD.extraPriceCents)

        assertEquals(15_000L, WrapType.BUBBLE.extraPriceRupiah)
        assertEquals(1_500_000L, WrapType.BUBBLE.extraPriceCents)
    }

    @Test
    fun testBaggagePricingCalculatorDefaultMStandard() {
        val grossCents = BaggagePricingCalculator.calculateGrossCents(BaggageSize.M, WrapType.STANDARD)
        val netCents = BaggagePricingCalculator.calculateNetCents(grossCents, 2_500_000L)

        assertEquals(6_500_000L, grossCents)
        assertEquals(4_000_000L, netCents)
    }

    @Test
    fun testBaggagePricingCalculatorSlide21ScenarioLargeStandard() {
        // Slide 21 Pitch Deck: "Order Baggage Wrapping Rp 80.000, Promo discount -Rp 25.000, Total bayar Rp 55.000"
        val grossRupiah = BaggagePricingCalculator.calculateGrossRupiah(BaggageSize.L, WrapType.STANDARD)
        val grossCents = BaggagePricingCalculator.calculateGrossCents(BaggageSize.L, WrapType.STANDARD)
        val netRupiah = BaggagePricingCalculator.calculateNetRupiah(grossRupiah, 25_000L)
        val netCents = BaggagePricingCalculator.calculateNetCents(grossCents, 2_500_000L)

        assertEquals(80_000L, grossRupiah)
        assertEquals(8_000_000L, grossCents)
        assertEquals(55_000L, netRupiah)
        assertEquals(5_500_000L, netCents)
    }

    @Test
    fun testBaggagePricingCalculatorMediumBubbleWrap() {
        // Medium (65.000) + Bubble Wrap (15.000) = 80.000 gross, Net after 25.000 discount = 55.000
        val grossCents = BaggagePricingCalculator.calculateGrossCents(BaggageSize.M, WrapType.BUBBLE)
        val netCents = BaggagePricingCalculator.calculateNetCents(grossCents, 2_500_000L)

        assertEquals(8_000_000L, grossCents)
        assertEquals(5_500_000L, netCents)
    }

    @Test
    fun testBaggagePricingCalculatorExtraLargeBubbleWrap() {
        // XL (100.000) + Bubble (15.000) = 115.000 gross, Net after 25.000 discount = 90.000
        val grossCents = BaggagePricingCalculator.calculateGrossCents(BaggageSize.XL, WrapType.BUBBLE)
        val netCents = BaggagePricingCalculator.calculateNetCents(grossCents, 2_500_000L)

        assertEquals(11_500_000L, grossCents)
        assertEquals(9_000_000L, netCents)
    }

    @Test
    fun testBaggagePricingCalculatorSmallStandard() {
        // S (50.000) + Standard (0) = 50.000 gross, Net after 25.000 discount = 25.000
        val grossCents = BaggagePricingCalculator.calculateGrossCents(BaggageSize.S, WrapType.STANDARD)
        val netCents = BaggagePricingCalculator.calculateNetCents(grossCents, 2_500_000L)

        assertEquals(5_000_000L, grossCents)
        assertEquals(2_500_000L, netCents)
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
        assertEquals(6_500_000L, request.grossAmountCents)
        assertEquals(4_000_000L, request.netAmountCents)

        val encoded = json.encodeToString(request)
        assertTrue(encoded.contains("\"bag_size\":\"M\""))
        assertTrue(encoded.contains("\"wrap_type\":\"standard\""))
        assertTrue(encoded.contains("\"payment_method\":\"qris\""))
        assertTrue(encoded.contains("\"gross_amount_cents\":6500000"))
        assertTrue(encoded.contains("\"net_amount_cents\":4000000"))

        val decoded = json.decodeFromString<ClaimDiscountRequest>(encoded)
        assertEquals("M", decoded.bagSize)
        assertEquals("standard", decoded.wrapType)
        assertEquals(6_500_000L, decoded.grossAmountCents)
        assertEquals(4_000_000L, decoded.netAmountCents)
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
        assertEquals(6_500_000L, defaultClaim.grossAmountCents)
        assertEquals(4_000_000L, defaultClaim.netAmountCents)

        val customClaim = defaultClaim.copy(
            bagSize = "L",
            wrapType = "bubble",
            paymentMethod = "cash",
            grossAmountCents = 9_500_000L,
            netAmountCents = 7_000_000L
        )

        assertEquals("L", customClaim.bagSize)
        assertEquals("bubble", customClaim.wrapType)
        assertEquals("cash", customClaim.paymentMethod)
        assertEquals(9_500_000L, customClaim.grossAmountCents)
        assertEquals(7_000_000L, customClaim.netAmountCents)

        val encoded = json.encodeToString(customClaim)
        val decoded = json.decodeFromString<RedemptionClaim>(encoded)
        assertEquals("L", decoded.bagSize)
        assertEquals("bubble", decoded.wrapType)
        assertEquals("cash", decoded.paymentMethod)
        assertEquals(9_500_000L, decoded.grossAmountCents)
        assertEquals(7_000_000L, decoded.netAmountCents)
    }
}
