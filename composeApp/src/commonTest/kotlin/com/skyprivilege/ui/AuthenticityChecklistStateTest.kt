package com.skyprivilege.ui

import com.skyprivilege.domain.model.TicketGuideline
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthenticityChecklistStateTest {

    private val sampleGuidelines = listOf(
        TicketGuideline(id = 1L, title = "Ciri 1", description = "Desc 1", category = "physical", displayOrder = 1, active = true),
        TicketGuideline(id = 2L, title = "Ciri 2", description = "Desc 2", category = "physical", displayOrder = 2, active = true),
        TicketGuideline(id = 3L, title = "Ciri 3", description = "Desc 3", category = "digital", displayOrder = 3, active = true)
    )

    @Test
    fun testInitialStateMustBeFalse() {
        val initialCheckedMap = sampleGuidelines.associate { it.id to false }
        assertFalse(AuthenticityChecklistValidator.isAllChecked(sampleGuidelines, initialCheckedMap))
    }

    @Test
    fun testPartialChecklistDisablesConfirmation() {
        val partialMap = mapOf(1L to true, 2L to true, 3L to false)
        assertFalse(AuthenticityChecklistValidator.isAllChecked(sampleGuidelines, partialMap))
    }

    @Test
    fun testAllCheckedEnablesConfirmation() {
        val fullMap = mapOf(1L to true, 2L to true, 3L to true)
        assertTrue(AuthenticityChecklistValidator.isAllChecked(sampleGuidelines, fullMap))
    }

    @Test
    fun testEmptyGuidelinesDisablesConfirmation() {
        assertFalse(AuthenticityChecklistValidator.isAllChecked(emptyList<TicketGuideline>(), emptyMap<Long, Boolean>()))
    }
}
