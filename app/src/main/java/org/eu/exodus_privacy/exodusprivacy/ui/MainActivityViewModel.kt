package org.eu.exodus_privacy.exodusprivacy.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.eu.exodus_privacy.exodusprivacy.core.network.NetworkManager
import org.eu.exodus_privacy.exodusprivacy.data.dataStore.ExodusConfig
import org.eu.exodus_privacy.exodusprivacy.data.dataStore.ExodusDataStoreRepository
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val configStorage: ExodusDataStoreRepository<ExodusConfig>,
    networkManager: NetworkManager,
) : ViewModel() {

    val config = configStorage.getAll().asLiveData()
    private val TAG = MainActivityViewModel::class.java.simpleName

    fun saveNotificationPermissionRequested(status: Boolean) {
        Log.d(TAG, "Got status for notification permission: $status.")
        val newConfig = config.value as MutableMap
        newConfig["notification_perm"] = ExodusConfig("notification_requested", status)
        viewModelScope.launch {
            Log.d(TAG, "Saving new config: $newConfig.")
            configStorage.insert(newConfig)
        }
    }

    fun savePolicyAgreement(status: Boolean) {
        Log.d(TAG, "Got status for policy agreement: $status.")
        val newConfig = config.value as MutableMap
        newConfig["privacy_policy"] = ExodusConfig("privacy_policy_consent", status)
        viewModelScope.launch {
            Log.d(TAG, "Saving new config: $newConfig.")
            configStorage.insert(newConfig)
        }
    }

    val networkConnection: StateFlow<Boolean> = networkManager.isOnline.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = true,
    )
}
