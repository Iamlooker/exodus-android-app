package org.eu.exodus_privacy.exodusprivacy.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.eu.exodus_privacy.exodusprivacy.data.database.model.TrackerData

@Dao
interface TrackerDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackerData(trackerData: TrackerData)

    @Query("SELECT * FROM trackerdata WHERE id=:id")
    suspend fun queryTrackerById(id: Int): List<TrackerData>

    @Query("SELECT * FROM trackerdata")
    fun queryAllTrackers(): LiveData<List<TrackerData>>

    @Query("SELECT * FROM trackerdata WHERE presentOnDevice = 1")
    fun queryActiveTrackers(): LiveData<List<TrackerData>>

    @Query("SELECT * FROM trackerdata WHERE id IN (:listOfID) ORDER BY name COLLATE NOCASE")
    suspend fun queryTrackersByIdList(listOfID: List<Int>): List<TrackerData>

    @Delete
    suspend fun deleteTrackerData(trackerData: TrackerData)
}
