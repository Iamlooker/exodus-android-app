package org.eu.exodus_privacy.exodusprivacy.di

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.eu.exodus_privacy.exodusprivacy.data.dataStore.ExodusConfig

data class DataStoreName(val name: String)

@Module
@InstallIn(SingletonComponent::class)
object ExodusDataStoreModule {

    @Provides
    fun providesGson(): Gson {
        return Gson()
    }

    @Provides
    fun providesPreferencesKey(): Preferences.Key<String> {
        return stringPreferencesKey("ExodusSettings")
    }

    @Provides
    fun providesTypeToken(): TypeToken<Map<String, ExodusConfig>> {
        return object : TypeToken<Map<String, ExodusConfig>>() {}
    }

    @Provides
    fun providesDataStoreName(): DataStoreName {
        return DataStoreName("exodusPreferences")
    }
}
