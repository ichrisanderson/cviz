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
        CaseEntity::class,
        DeathEntity::class,
        MetadataEntity::class,
        DailyRecordEntity::class,
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

    abstract fun casesDao(): CaseDao
    abstract fun deathsDao(): DeathDao
    abstract fun dailyRecordsDao(): DailyRecordDao
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
    tableName = "cases",
    primaryKeys = ["areaCode", "date"]
)
data class CaseEntity(
    @ColumnInfo(name = "areaCode")
    val areaCode: String,
    @ColumnInfo(name = "areaName")
    val areaName: String,
    @ColumnInfo(name = "dailyLabConfirmedCases")
    val dailyLabConfirmedCases: Int,
    @ColumnInfo(name = "dailyTotalLabConfirmedCasesRate")
    val dailyTotalLabConfirmedCasesRate: Double,
    @ColumnInfo(name = "totalLabConfirmedCases")
    val totalLabConfirmedCases: Int,
    @ColumnInfo(name = "date")
    val date: LocalDate
)

data class AreaTupleEntity(
    @ColumnInfo(name = "areaCode") val areaCode: String,
    @ColumnInfo(name = "areaName") val areaName: String
)

@Dao
interface CaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(cases: List<CaseEntity>)

    @Query("SELECT COUNT(areaCode) FROM cases")
    fun casesCount(): Int

    @Query("SELECT DISTINCT areaCode, areaName FROM cases WHERE areaName LIKE :areaName ORDER BY areaName ASC")
    fun allAreas(areaName: String): List<AreaTupleEntity>

    @Query("SELECT * FROM cases WHERE areaCode = :areaCode ORDER BY date ASC")
    fun areaCases(areaCode: String): List<CaseEntity>

    @Query("SELECT * FROM cases INNER JOIN savedArea ON cases.areaCode = savedArea.areaCode ORDER BY date ASC")
    fun savedAreaCases(): Flow<List<CaseEntity>>
}

@Entity(
    tableName = "deaths",
    primaryKeys = ["areaCode", "date"]
)
data class DeathEntity(
    @ColumnInfo(name = "areaCode")
    val areaCode: String,
    @ColumnInfo(name = "areaName")
    val areaName: String,
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "cumulativeDeaths")
    val cumulativeDeaths: Int,
    @ColumnInfo(name = "dailyChangeInDeaths")
    val dailyChangeInDeaths: Int
)

@Dao
interface DeathDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(deaths: List<DeathEntity>)

    @Query("SELECT COUNT(areaCode) FROM deaths")
    fun deathsCount(): Int

    @Query("SELECT * FROM deaths WHERE areaCode = :areaCode ORDER BY date DESC")
    fun areaDeaths(areaCode: String): List<DeathEntity>
}

@Entity(
    tableName = "dailyRecords",
    primaryKeys = ["areaName", "date"]
)
data class DailyRecordEntity(
    @ColumnInfo(name = "areaName")
    val areaName: String,
    @ColumnInfo(name = "dailyLabConfirmedCases")
    val dailyLabConfirmedCases: Int,
    @ColumnInfo(name = "totalLabConfirmedCases")
    val totalLabConfirmedCases: Int,
    @ColumnInfo(name = "date")
    val date: LocalDate
)

@Dao
interface DailyRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(dailyRecordEntities: List<DailyRecordEntity>)

    @Query("SELECT COUNT(areaName) FROM dailyRecords")
    fun count(): Int

    @Query("SELECT * FROM dailyRecords WHERE areaName = :areaName")
    fun searchDailyRecords(areaName: String): List<DailyRecordEntity>
}

@Entity(
    tableName = "metadata",
    primaryKeys = ["id"]
)
data class MetadataEntity(
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "disclaimer")
    val disclaimer: String,
    @ColumnInfo(name = "lastUpdatedAt")
    val lastUpdatedAt: LocalDateTime
) {
    companion object {
        const val CASE_METADATA_ID = "UK-CASE-METADATA"
        const val DEATH_METADATA_ID = "UK-DEATH-METADATA"
    }
}

@Dao
interface MetadataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(metadata: List<MetadataEntity>)

    @Query("SELECT * FROM metadata WHERE id = :id")
    fun metadata(id: String): List<MetadataEntity>
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
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(savedAreaEntity: SavedAreaEntity)

    @Query("SELECT COUNT(areaCode) > 0 FROM savedArea WHERE areaCode = :areaCode")
    fun isSaved(areaCode: String): Flow<Boolean>

    @Delete
    fun delete(savedAreaEntity: SavedAreaEntity): Int
}
