package com.skyprivilege.domain.model

enum class BaggageSize(
    val code: String,
    val label: String,
    val priceRupiah: Long,
    val priceCents: Long
) {
    S("S", "S", 50_000L, 5_000_000L),
    M("M", "M", 65_000L, 6_500_000L),
    L("L", "L", 80_000L, 8_000_000L),
    XL("XL", "XL", 100_000L, 10_000_000L);

    companion object {
        fun fromCode(code: String): BaggageSize =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: M
    }
}

enum class WrapType(
    val code: String,
    val label: String,
    val extraPriceRupiah: Long,
    val extraPriceCents: Long
) {
    STANDARD("standard", "Standard", 0L, 0L),
    BUBBLE("bubble", "Bubble Wrap", 15_000L, 1_500_000L);

    companion object {
        fun fromCode(code: String): WrapType =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: STANDARD
    }
}

object BaggagePricingCalculator {
    fun calculateGrossCents(bagSize: BaggageSize, wrapType: WrapType): Long {
        return bagSize.priceCents + wrapType.extraPriceCents
    }

    fun calculateGrossRupiah(bagSize: BaggageSize, wrapType: WrapType): Long {
        return bagSize.priceRupiah + wrapType.extraPriceRupiah
    }

    fun calculateNetCents(grossCents: Long, discountCents: Long): Long {
        return maxOf(0L, grossCents - discountCents)
    }

    fun calculateNetRupiah(grossRupiah: Long, discountRupiah: Long): Long {
        return maxOf(0L, grossRupiah - discountRupiah)
    }
}
