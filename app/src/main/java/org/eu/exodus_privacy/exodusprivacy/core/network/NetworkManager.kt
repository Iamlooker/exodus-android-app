package org.eu.exodus_privacy.exodusprivacy.core.network

import kotlinx.coroutines.flow.Flow

interface NetworkManager {

    val isOnline: Flow<Boolean>

    suspend fun isExodusReachable(): Boolean

}