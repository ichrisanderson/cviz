/*
 * Copyright 2020 Chris Anderson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chrisa.covid19.core.data.db.v2

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [
        AreaEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    LocalDateConverter::class,
    LocalDateTimeConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun areaDao(): AreaDao

    companion object {
        private const val databaseName = "covid19-uk-db"
        fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

class LocalDateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }
}

class LocalDateTimeConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.atZone(ZoneOffset.UTC)?.toInstant()?.toEpochMilli()
    }
}

@Entity(
    tableName = "areas",
    primaryKeys = ["areaCode"]
)
data class AreaEntity(
    @ColumnInfo(name = "areaCode")
    val areaCode: String,
    @ColumnInfo(name = "areaName")
    val areaName: String,
    @ColumnInfo(name = "areaType")
    val areaType: String
)

@Dao
interface AreaDao {
    @Query("DELETE FROM areas")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(areas: List<AreaEntity>)

    @Query("SELECT * FROM areas ORDER BY areaName ASC")
    fun allAreas(): List<AreaEntity>
}

@Entity(
    tableName = "metadata",
    primaryKeys = ["id"]
)
data class MetadataEntity(
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "lastUpdatedAt")
    val lastUpdatedAt: LocalDateTime
) {
    companion object {
        const val AREA_METADATA_ID = "AREA_METADATA"
        const val OVERVIEW_METADATA_ID = "OVERVIEW_METADATA"
    }
}

@Dao
interface MetadataDao {

    @Query("DELETE FROM metadata")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(metadata: List<MetadataEntity>)

    @Query("SELECT * FROM metadata WHERE id = :id")
    fun metadata(id: String): List<MetadataEntity>

    @Query("SELECT * FROM metadata WHERE id = :id LIMIT 1")
    fun metadataAsFlow(id: String): Flow<MetadataEntity>
}
