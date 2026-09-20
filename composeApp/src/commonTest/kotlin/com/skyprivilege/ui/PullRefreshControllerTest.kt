package com.skyprivilege.ui

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import java.util.concurrent.atomic.AtomicInteger
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
    fun testExecuteRunsActionAndResets() = runTest {
        val controller = PullRefreshController()
        var executionCount = 0

        controller.execute {
            executionCount++
            assertTrue(controller.isRefreshing)
        }

        assertEquals(1, executionCount)
        assertFalse(controller.isRefreshing)
    }

    @Test
    fun testConcurrentExecutionRejectedWhileActionInProgress() = runTest {
        val controller = PullRefreshController()
        val actionStarted = CompletableDeferred<Unit>()
        val releaseAction = CompletableDeferred<Unit>()
        var primaryCompleted = false
        var concurrentRan = false

        val job = launch(Dispatchers.Default) {
            controller.execute {
                actionStarted.complete(Unit)
                releaseAction.await()
                primaryCompleted = true
            }
        }

        actionStarted.await()
        assertTrue(controller.isRefreshing, "Should be refreshing while primary action running")

        // Try second call while first is still running
        controller.execute {
            concurrentRan = true
        }

        assertFalse(concurrentRan, "Concurrent call should not have executed")

        // Release first action and finish
        releaseAction.complete(Unit)
        job.join()

        assertTrue(primaryCompleted)
        assertFalse(controller.isRefreshing)
    }

    @Test
    fun testParallelCoroutinesThreadSafety() = runTest {
        val controller = PullRefreshController()
        val executedActions = AtomicInteger(0)
        val barrier = CompletableDeferred<Unit>()

        // Spawn 20 parallel coroutines competing for the mutex simultaneously
        val deferreds = (1..20).map {
            async(Dispatchers.Default) {
                barrier.await()
                controller.execute {
                    executedActions.incrementAndGet()
                    delay(30)
                }
            }
        }

        barrier.complete(Unit)
        deferreds.awaitAll()

        // Because they run concurrently and delay inside execute, exactly 1 succeeds
        // while all others get dropped by !mutex.tryLock()
        assertEquals(1, executedActions.get(), "Only one concurrent invocation should acquire mutex")
        assertFalse(controller.isRefreshing, "isRefreshing must be false after completion")
    }

    @Test
    fun testFailureStillResetsRefreshingState() = runTest {
        val controller = PullRefreshController()

        try {
            controller.execute {
                throw RuntimeException("Network error simulation")
            }
        } catch (_: Exception) {
            // expected
        }

        assertFalse(controller.isRefreshing, "Should reset refreshing to false even on failure")
    }
}
