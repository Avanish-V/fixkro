package com.iotabuild.zippyhand.feature.home.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHome(home: HomeEntity)

    @Query("SELECT * FROM home LIMIT 1")
    fun getHome(): Flow<HomeEntity?>

    @Query("DELETE FROM home")
    suspend fun clearHome()
}
