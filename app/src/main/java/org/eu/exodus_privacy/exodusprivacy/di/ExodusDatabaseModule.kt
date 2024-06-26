package org.eu.exodus_privacy.exodusprivacy.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.eu.exodus_privacy.exodusprivacy.data.database.ExodusDatabase
import org.eu.exodus_privacy.exodusprivacy.data.database.ExodusDatabaseConverters
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExodusDatabaseModule {

    private const val DATABASE_NAME = "exodus_database"
    private val exodusTypeConverter = ExodusDatabaseConverters()

    @Singleton
    @Provides
    fun provideExodusDatabaseInstance(@ApplicationContext context: Context): ExodusDatabase {
        return Room.databaseBuilder(context, ExodusDatabase::class.java, DATABASE_NAME)
            .addTypeConverter(exodusTypeConverter).build()
    }
}
