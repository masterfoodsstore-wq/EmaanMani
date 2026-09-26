package com.example.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class NetworkConnectivityObserver(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    // Allows manual override for testing offline scenario inside emulators or devices
    private val _simulatedOffline = MutableStateFlow(false)
    val simulatedOffline: StateFlow<Boolean> = _simulatedOffline.asStateFlow()

    private val _isOnline = MutableStateFlow(checkRealNetworkStatus())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    init {
        startNetworkMonitoring()
    }

    fun setSimulatedOffline(offline: Boolean) {
        _simulatedOffline.value = offline
        recomputeStatus()
    }

    @Suppress("DEPRECATION")
    private fun checkRealNetworkStatus(): Boolean {
        val cm = connectivityManager ?: return true
        val activeNetwork = cm.activeNetwork
        if (activeNetwork != null) {
            val caps = cm.getNetworkCapabilities(activeNetwork)
            if (caps != null) {
                if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                ) {
                    return true
                }
            }
        }
        val info = cm.activeNetworkInfo
        return info != null && info.isConnected
    }

    fun refreshNetworkStatus(): Boolean {
        recomputeStatus()
        return _isOnline.value
    }

    private fun recomputeStatus() {
        if (_simulatedOffline.value) {
            _isOnline.value = false
            return
        }
        _isOnline.value = checkRealNetworkStatus()
    }

    private fun startNetworkMonitoring() {
        val cm = connectivityManager ?: run {
            _isOnline.value = false
            return
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (!_simulatedOffline.value) {
                    _isOnline.value = true
                }
            }

            override fun onLost(network: Network) {
                if (!_simulatedOffline.value) {
                    _isOnline.value = checkRealNetworkStatus()
                }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                if (!_simulatedOffline.value) {
                    val hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    _isOnline.value = hasInternet
                }
            }
        }

        try {
            cm.registerNetworkCallback(request, callback)
        } catch (_: Exception) {
            // Fallback to active query
            recomputeStatus()
        }
    }
}
