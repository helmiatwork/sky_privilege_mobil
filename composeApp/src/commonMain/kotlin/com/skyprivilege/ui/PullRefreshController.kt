package com.skyprivilege.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.sync.Mutex

/**
 * Controller to manage pull-to-refresh state and prevent concurrent duplicate refreshes.
 */
class PullRefreshController {
    private val mutex = Mutex()
    var isRefreshing: Boolean by mutableStateOf(false)
        private set

    suspend fun execute(action: suspend () -> Unit) {
        if (!mutex.tryLock()) return
        try {
            isRefreshing = true
            action()
        } finally {
            isRefreshing = false
            mutex.unlock()
        }
    }
}
