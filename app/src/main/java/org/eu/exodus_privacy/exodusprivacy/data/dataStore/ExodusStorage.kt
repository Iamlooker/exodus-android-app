package org.eu.exodus_privacy.exodusprivacy.data.dataStore

import kotlinx.coroutines.flow.Flow

interface ExodusStorage<T> {
    suspend fun insert(data: Map<String, T>)

    fun get(key: String): Flow<T>

    fun getAll(): Flow<Map<String, T>>

    fun clearAll(): Flow<Int>
}
