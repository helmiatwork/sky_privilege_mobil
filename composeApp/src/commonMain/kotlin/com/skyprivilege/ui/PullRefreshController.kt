package com.skyprivilege.ui

/**
 * Controller to manage pull-to-refresh state and prevent concurrent duplicate refreshes.
 */
class PullRefreshController {
    var isRefreshing: Boolean = false
        private set

    fun setRefreshing(value: Boolean) {
        isRefreshing = value
    }

    suspend fun refresh(block: suspend () -> Unit): Boolean {
        if (isRefreshing) return false
        isRefreshing = true
        return try {
            block()
            true
        } finally {
            isRefreshing = false
        }
    }
}
