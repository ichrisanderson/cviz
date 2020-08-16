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

package com.chrisa.covid19.core.data.db

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
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
        AreaEntity::class,
        AreaDataEntity::class,
        MetadataEntity::class,
        SavedAreaEntity::class
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
    abstract fun areaDataDao(): AreaDataDao
    abstract fun metadataDao(): MetadataDao
    abstract fun savedAreaDao(): SavedAreaDao

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

    @Query("SELECT COUNT(areaCode) FROM areas")
    fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(areas: List<AreaEntity>)

    @Query("SELECT * FROM areas ORDER BY areaName ASC")
    fun all(): List<AreaEntity>

    @Query("SELECT * FROM areas WHERE areaName LIKE :areaName ORDER BY areaName ASC")
    fun search(areaName: String): List<AreaEntity>
}

@Entity(
    tableName = "areaData",
    primaryKeys = ["areaCode", "date"]
)
data class AreaDataEntity(
    @ColumnInfo(name = "areaCode")
    val areaCode: String,
    @ColumnInfo(name = "areaName")
    val areaName: String,
    @ColumnInfo(name = "areaType")
    val areaType: String,
    @ColumnInfo(name = "newCases")
    val newCases: Int,
    @ColumnInfo(name = "infectionRate")
    val infectionRate: Double,
    @ColumnInfo(name = "cumulativeCases")
    val cumulativeCases: Int,
    @ColumnInfo(name = "date")
    val date: LocalDate
)

@Dao
interface AreaDataDao {
    @Query("DELETE FROM areaData")
    fun deleteAll()

    @Query("SELECT COUNT(areaCode) FROM areaData")
    fun countAll(): Int

    @Query("SELECT COUNT(areaCode) FROM areaData WHERE :areaType = areaType")
    fun countAllByAreaType(areaType: String): Int

    @Query("DELETE FROM areaData WHERE :areaType = areaType")
    fun deleteAllByAreaType(areaType: String)

    @Query("DELETE FROM areaData WHERE :areaCode = areaCode")
    fun deleteAllByAreaCode(areaCode: String)

    @Query("SELECT * FROM areaData WHERE :areaCode = areaCode ORDER BY date ASC")
    fun allByAreaCodeFlow(areaCode: String): Flow<List<AreaDataEntity>>

    @Query("SELECT * FROM areaData WHERE :areaCode = areaCode ORDER BY date ASC")
    fun allByAreaCode(areaCode: String): List<AreaDataEntity>

    @Query("SELECT * FROM areaData INNER JOIN savedArea ON areaData.areaCode = savedArea.areaCode ORDER BY date ASC")
    fun allSavedAreaData(): Flow<List<AreaDataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(areaData: List<AreaDataEntity>)
}

@Entity(
    tableName = "metadata",
    primaryKeys = ["id"]
)
data class MetadataEntity(
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "lastUpdatedAt")
    val lastUpdatedAt: LocalDateTime,
    @ColumnInfo(name = "lastSyncTime")
    val lastSyncTime: LocalDateTime
)

@Dao
interface MetadataDao {

    @Query("DELETE FROM metadata")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(metadata: MetadataEntity)

    @Query("SELECT * FROM metadata WHERE id = :id  LIMIT 1")
    fun metadata(id: String): MetadataEntity?

    @Query("SELECT * FROM metadata WHERE id = :id LIMIT 1")
    fun metadataAsFlow(id: String): Flow<MetadataEntity?>
}

@Entity(
    tableName = "savedArea",
    primaryKeys = ["areaCode"]
)
data class SavedAreaEntity(
    @ColumnInfo(name = "areaCode")
    val areaCode: String
)

@Dao
interface SavedAreaDao {

    @Query("DELETE FROM savedArea")
    fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(savedAreaEntity: SavedAreaEntity)

    @Query("SELECT COUNT(areaCode) > 0 FROM savedArea WHERE areaCode = :areaCode")
    fun isSaved(areaCode: String): Flow<Boolean>

    @Delete
    fun delete(savedAreaEntity: SavedAreaEntity): Int
}

object Constants {
    const val UK_AREA_CODE = "K02000001"
}
object MetaDataHelper {
    fun areaListKey(): String = "AREA_METADATA"
    fun ukOverviewKey(): String = "UK_OVERVIEW_CODE"
    fun areaKey(areaCode: String, areaType: String) = "areaCode=$areaCode;areaType=$areaType"
}
