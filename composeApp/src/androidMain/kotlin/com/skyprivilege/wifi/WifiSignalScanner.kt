package com.skyprivilege.wifi

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import com.skyprivilege.domain.model.WifiContext

class WifiSignalScanner(private val context: Context) {
    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    @SuppressLint("MissingPermission")
    fun getCurrentWifiContext(): WifiContext? {
        val info = wifiManager.connectionInfo ?: return null
        val bssid = info.bssid ?: return null
        val ssid = info.ssid?.trim('"', ' ') ?: ""
        val rssi = info.rssi

        return WifiContext(
            bssid = bssid,
            ssid = ssid,
            rssiDbm = rssi
        )
    }
}
