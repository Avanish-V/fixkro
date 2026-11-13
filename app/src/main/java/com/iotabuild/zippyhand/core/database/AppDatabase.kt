package com.iotabuild.zippyhand.location.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iotabuild.zippyhand.feature.home.data.local.HomeDao
import com.iotabuild.zippyhand.feature.home.data.local.HomeEntity
import com.iotabuild.zippyhand.feature.home.data.local.converter.Converter
import com.iotabuild.zippyhand.feature.profile.data.local.UserDao
import com.iotabuild.zippyhand.feature.profile.data.local.UserEntity

@Database(entities = [LocationEntity::class, HomeEntity::class, UserEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun homeDao(): HomeDao
    abstract fun userDao(): UserDao
}