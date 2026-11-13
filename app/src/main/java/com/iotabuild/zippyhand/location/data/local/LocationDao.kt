package com.iotabuild.zippyhand.location.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)

    @Query("SELECT * FROM locations LIMIT 1")
    fun getLocation(): Flow<LocationEntity?>

    @Query("DELETE FROM locations")
    suspend fun clearLocation()
}
