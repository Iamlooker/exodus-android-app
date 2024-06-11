package org.eu.exodus_privacy.exodusprivacy.data.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.eu.exodus_privacy.exodusprivacy.data.database.model.ExodusApplication
import org.eu.exodus_privacy.exodusprivacy.data.database.dao.ExodusApplicationDao
import org.eu.exodus_privacy.exodusprivacy.data.database.model.TrackerData
import org.eu.exodus_privacy.exodusprivacy.data.database.dao.TrackerDataDao
import org.eu.exodus_privacy.exodusprivacy.data.model.Constants

@Database(
    entities = [TrackerData::class, ExodusApplication::class],
    version = Constants.currentDatabaseVersion,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(
            from = Constants.previousDatabaseVersion,
            to = Constants.currentDatabaseVersion,
        ),
    ],
)
@TypeConverters(ExodusDatabaseConverters::class)
abstract class ExodusDatabase : RoomDatabase() {

    abstract fun trackerDataDao(): TrackerDataDao

    abstract fun exodusApplicationDao(): ExodusApplicationDao
}
