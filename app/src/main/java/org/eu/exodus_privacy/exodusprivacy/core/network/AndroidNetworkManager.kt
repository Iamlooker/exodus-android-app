package org.eu.exodus_privacy.exodusprivacy.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.withContext
import org.eu.exodus_privacy.exodusprivacy.data.remote.ExodusAPIInterface
import org.eu.exodus_privacy.exodusprivacy.utils.IoDispatcher
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidNetworkManager @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : NetworkManager {

    override val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.d(TAG, "Network available.")
                channel.trySend(true)
            }

            override fun onLost(network: Network) {
                Log.w(TAG, "Network not available.")
                channel.trySend(false)
            }
        }

        val connectivityManager = ContextCompat
            .getSystemService(context, ConnectivityManager::class.java)

        connectivityManager?.registerNetworkCallback(
            NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build(),
            callback,
        )

        channel.trySend(connectivityManager.isCurrentlyConnected())

        awaitClose {
            connectivityManager?.unregisterNetworkCallback(callback)
        }
    }.conflate()

    override suspend fun isExodusReachable(): Boolean {
        return withContext(ioDispatcher) {
            try {
                URL(ExodusAPIInterface.BASE_URL)
                    .openConnection()
                    .connect()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Could Not Reach Exodus API URL.", e)
                false
            }
        }
    }

    private fun ConnectivityManager?.isCurrentlyConnected() = when (this) {
        null -> false
        else ->
            activeNetwork
                ?.let(::getNetworkCapabilities)
                ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                ?: false
    }

    private companion object {
        const val TAG = "AndroidNetworkManager"
    }
}
