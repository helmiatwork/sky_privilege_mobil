package com.skyprivilege.domain.model

enum class BaggageSize(
    val code: String,
    val label: String,
    val priceRupiah: Long,
    val priceCents: Long
) {
    S("S", "S", 60_000L, 6_000_000L),
    M("M", "M", 75_000L, 7_500_000L),
    L("L", "L", 90_000L, 9_000_000L),
    XL("XL", "XL", 120_000L, 12_000_000L);

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
    PREMIUM("premium", "Premium", 15_000L, 1_500_000L),
    BUBBLE("bubble", "Bubble Wrap", 10_000L, 1_000_000L);

    companion object {
        fun fromCode(code: String): WrapType =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: STANDARD
    }
}

object BaggagePricingCalculator {
    fun calculateGrossCents(bagSize: BaggageSize, wrapType: WrapType, bagCount: Int = 1): Long {
        return (bagSize.priceCents + wrapType.extraPriceCents) * bagCount
    }

    fun calculateGrossRupiah(bagSize: BaggageSize, wrapType: WrapType, bagCount: Int = 1): Long {
        return (bagSize.priceRupiah + wrapType.extraPriceRupiah) * bagCount
    }

    fun calculateNetCents(grossCents: Long, discountCents: Long): Long {
        return maxOf(0L, grossCents - discountCents)
    }

    fun calculateNetRupiah(grossRupiah: Long, discountRupiah: Long): Long {
        return maxOf(0L, grossRupiah - discountRupiah)
    }
}

object OrderIdGenerator {
    fun generateOrderId(): String {
        return "ORD-${kotlin.random.Random.nextLong(100_000_000L, 1_000_000_000L)}"
    }
}
