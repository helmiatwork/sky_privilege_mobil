package com.skyprivilege.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

enum class NetworkType {
    NONE,
    WIFI,
    CELLULAR,
    OTHER
}

data class NetworkTransitionEvent(
    val previousType: NetworkType,
    val currentType: NetworkType,
    val isFailover: Boolean,
    val timestampMs: Long
)

/**
 * Pure function to resolve the active network type based on transport flags.
 * Hierarchy: Wi-Fi > Cellular > Ethernet (Other) > None
 */
fun resolveNetworkType(hasWifi: Boolean, hasCellular: Boolean, hasEthernet: Boolean): NetworkType {
    return when {
        hasWifi -> NetworkType.WIFI
        hasCellular -> NetworkType.CELLULAR
        hasEthernet -> NetworkType.OTHER
        else -> NetworkType.NONE
    }
}

/**
 * Pure function to compute transition event between two network types.
 * Returns null if network type has not changed (idempotent).
 */
fun computeTransition(
    oldType: NetworkType,
    newType: NetworkType,
    timestampMs: Long = System.currentTimeMillis()
): NetworkTransitionEvent? {
    if (oldType == newType) return null
    val isFailover = (oldType == NetworkType.WIFI && newType == NetworkType.CELLULAR)
    return NetworkTransitionEvent(
        previousType = oldType,
        currentType = newType,
        isFailover = isFailover,
        timestampMs = timestampMs
    )
}

class NetworkConnectivityManager(context: Context) {

    // Store only applicationContext to prevent Activity memory leaks
    private val appContext = context.applicationContext
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _currentNetworkType = MutableStateFlow(NetworkType.NONE)
    val currentNetworkType: StateFlow<NetworkType> = _currentNetworkType.asStateFlow()

    private val _transitionEvents = MutableSharedFlow<NetworkTransitionEvent>(replay = 1, extraBufferCapacity = 64)
    val transitionEvents: SharedFlow<NetworkTransitionEvent> = _transitionEvents.asSharedFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var isMonitoring = false

    @Synchronized
    fun startMonitoring() {
        if (isMonitoring) return

        updateInitialNetworkState()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network onAvailable: $network")
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                val newType = resolveNetworkTypeFromCapabilities(networkCapabilities)
                val oldType = _currentNetworkType.value

                val event = computeTransition(oldType, newType)
                if (event != null) {
                    if (event.isFailover) {
                        Log.w(TAG, "DUAL-SIM FAILOVER DETECTED: Transisi Wi-Fi -> Seluler 4G/LTE!")
                    } else {
                        Log.i(TAG, "Network transition: $oldType -> $newType")
                    }

                    _currentNetworkType.value = newType
                    _transitionEvents.tryEmit(event)
                }
            }

            override fun onLost(network: Network) {
                Log.w(TAG, "Network lost: $network")
                val activeNetwork = connectivityManager.activeNetwork
                if (activeNetwork == null) {
                    val oldType = _currentNetworkType.value
                    val event = computeTransition(oldType, NetworkType.NONE)
                    if (event != null) {
                        _currentNetworkType.value = NetworkType.NONE
                        _transitionEvents.tryEmit(event)
                    }
                }
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(callback)
            } else {
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                connectivityManager.registerNetworkCallback(request, callback)
            }
            networkCallback = callback
            isMonitoring = true
            Log.i(TAG, "Network monitoring started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback: ${e.message}", e)
        }
    }

    @Synchronized
    fun stopMonitoring() {
        if (!isMonitoring) return
        networkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
                Log.i(TAG, "Network monitoring stopped")
            } catch (e: Exception) {
                Log.w(TAG, "Error unregistering network callback: ${e.message}")
            }
        }
        networkCallback = null
        isMonitoring = false
    }

    fun isWifiConnected(): Boolean = _currentNetworkType.value == NetworkType.WIFI

    fun isCellularConnected(): Boolean = _currentNetworkType.value == NetworkType.CELLULAR

    fun isConnected(): Boolean = _currentNetworkType.value != NetworkType.NONE

    private fun updateInitialNetworkState() {
        try {
            val activeNetwork = connectivityManager.activeNetwork ?: return
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return
            _currentNetworkType.value = resolveNetworkTypeFromCapabilities(capabilities)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to evaluate initial network state: ${e.message}")
        }
    }

    private fun resolveNetworkTypeFromCapabilities(capabilities: NetworkCapabilities): NetworkType {
        val hasWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val hasCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        val hasEthernet = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        return resolveNetworkType(hasWifi = hasWifi, hasCellular = hasCellular, hasEthernet = hasEthernet)
    }

    companion object {
        private const val TAG = "NetworkConnectivityMgr"
    }
}
