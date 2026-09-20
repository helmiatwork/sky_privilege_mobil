package com.skyprivilege.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PullRefreshControllerTest {

    @Test
    fun testInitialStateNotRefreshing() {
        val controller = PullRefreshController()
        assertFalse(controller.isRefreshing)
    }

    @Test
    fun testTriggerRefreshExecutesBlockAndResets() = runTest {
        val controller = PullRefreshController()
        var executionCount = 0

        controller.refresh {
            executionCount++
        }

        assertEquals(1, executionCount)
        assertFalse(controller.isRefreshing)
    }

    @Test
    fun testTriggerRefreshPreventsConcurrentExecution() = runTest {
        val controller = PullRefreshController()
        var executionCount = 0

        // Simulate ongoing refresh
        controller.setRefreshing(true)

        val result = controller.refresh {
            executionCount++
        }

        assertFalse(result, "Should reject refresh while already refreshing")
        assertEquals(0, executionCount, "Block should not execute when already refreshing")
    }

    @Test
    fun testRefreshFailureStillResetsRefreshingState() = runTest {
        val controller = PullRefreshController()

        try {
            controller.refresh {
                throw RuntimeException("Network error simulation")
            }
        } catch (_: Exception) {
            // expected
        }

        assertFalse(controller.isRefreshing, "Should reset refreshing to false even on failure")
    }
}
