package org.eu.exodus_privacy.exodusprivacy.data.remote

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.eu.exodus_privacy.exodusprivacy.core.network.NetworkManager
import org.eu.exodus_privacy.exodusprivacy.data.remote.model.AppDetails
import org.eu.exodus_privacy.exodusprivacy.data.remote.model.Trackers
import org.eu.exodus_privacy.exodusprivacy.utils.IoDispatcher
import javax.inject.Inject

class RetrofitExodusService @Inject constructor(
    private val exodusAPIInterface: ExodusAPIInterface,
    private val networkManager: NetworkManager,
    @IoDispatcher val ioDispatcher: CoroutineDispatcher,
) : ExodusService {

    override suspend fun getAllTrackers(): Trackers {
        return withContext(ioDispatcher) {
            if (!networkManager.isExodusReachable()) {
                Log.w(TAG, "Could not reach exodus api. Returning empty Trackers object.")
                return@withContext Trackers()
            }
            Log.d(TAG, "Attempting download of tracker data.")
            val result = exodusAPIInterface.getAllTrackers()
            if (!result.isSuccessful || result.body() == null) {
                Log.w(
                    TAG,
                    "Failed to get trackers, response code: ${result.code()}. Returning empty Trackers object.",
                )
                return@withContext Trackers()
            }
            Log.d(TAG, "Success!")
            result.body()!!
        }
    }

    override suspend fun getAppDetails(packageName: String): List<AppDetails> {
        return withContext(ioDispatcher) {
            if (!networkManager.isExodusReachable()) {
                Log.w(TAG, "Could not reach exodus api. Returning emptyList.")
                return@withContext emptyList()
            }
            Log.d(TAG, "Attempting download of app details on $packageName.")
            val result = exodusAPIInterface.getAppDetails(packageName)
            if (!result.isSuccessful || result.body() == null) {
                Log.w(
                    TAG,
                    "Failed to get app details, response code: ${result.code()}. Returning emptyList.",
                )
                return@withContext emptyList()
            }
            Log.d(TAG, "Success!")
            result.body()!!
        }
    }

    private companion object {
        const val TAG = "RetrofitExodusService"
    }
}
