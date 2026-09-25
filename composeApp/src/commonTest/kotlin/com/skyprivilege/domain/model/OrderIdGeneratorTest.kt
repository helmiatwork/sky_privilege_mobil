package com.skyprivilege.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrderIdGeneratorTest {

    @Test
    fun testOrderIdFormat() {
        val orderId = OrderIdGenerator.generateOrderId()
        assertTrue(orderId.startsWith("ORD-"), "Order ID should start with ORD- prefix")
        val numberPart = orderId.removePrefix("ORD-").toLongOrNull()
        assertTrue(numberPart != null, "Order ID suffix should be a valid number")
        assertTrue(numberPart in 100_000_000L..999_999_999L, "Suffix should be between 100_000_000 and 999_999_999")
    }

    @Test
    fun testOrderIdUniqueness1000IterationsNoCollision() {
        val generatedIds = mutableSetOf<String>()
        val iterations = 1000

        for (i in 0 until iterations) {
            val id = OrderIdGenerator.generateOrderId()
            assertTrue(generatedIds.add(id), "Duplicate order ID detected at iteration $i: $id")
        }

        assertEquals(iterations, generatedIds.size, "All 1000 generated order IDs must be unique")
    }
}
