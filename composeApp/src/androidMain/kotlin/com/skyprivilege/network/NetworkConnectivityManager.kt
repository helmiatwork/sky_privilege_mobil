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

class NetworkConnectivityManager(private val context: Context) {

    private val connectivityManager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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
                val newType = resolveNetworkType(networkCapabilities)
                val oldType = _currentNetworkType.value

                if (oldType != newType) {
                    val isFailover = (oldType == NetworkType.WIFI && newType == NetworkType.CELLULAR)
                    val event = NetworkTransitionEvent(
                        previousType = oldType,
                        currentType = newType,
                        isFailover = isFailover,
                        timestampMs = System.currentTimeMillis()
                    )

                    if (isFailover) {
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
                    if (oldType != NetworkType.NONE) {
                        val event = NetworkTransitionEvent(
                            previousType = oldType,
                            currentType = NetworkType.NONE,
                            isFailover = false,
                            timestampMs = System.currentTimeMillis()
                        )
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
            _currentNetworkType.value = resolveNetworkType(capabilities)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to evaluate initial network state: ${e.message}")
        }
    }

    private fun resolveNetworkType(capabilities: NetworkCapabilities): NetworkType {
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.OTHER
            else -> NetworkType.OTHER
        }
    }

    companion object {
        private const val TAG = "NetworkConnectivityMgr"
    }
}
