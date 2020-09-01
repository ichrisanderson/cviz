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
        AreaSummaryEntity::class,
        MetadataEntity::class,
        SavedAreaEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(
    AreTypeConverter::class,
    LocalDateConverter::class,
    LocalDateTimeConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun areaDao(): AreaDao
    abstract fun areaDataDao(): AreaDataDao
    abstract fun areaSummaryEntityDao(): AreaSummaryEntityDao
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

class AreTypeConverter {
    @TypeConverter
    fun areaTypeFromString(value: String?): AreaType? {
        return value?.let { AreaType.from(value) }
    }

    @TypeConverter
    fun areaTypeToString(areaType: AreaType?): String? {
        return areaType?.value
    }
}

enum class AreaType(val value: String) {
    OVERVIEW("overview"),
    NATION("nation"),
    REGION("region"),
    UTLA("utla"),
    LTLA("ltla");

    companion object {
        fun from(type: String): AreaType? {
            return when (type) {
                OVERVIEW.value -> OVERVIEW
                NATION.value -> NATION
                REGION.value -> REGION
                UTLA.value -> UTLA
                LTLA.value -> LTLA
                else -> null
            }
        }
    }
}

@Entity(
    tableName = "area",
    primaryKeys = ["areaCode"]
)
data class AreaEntity(
    @ColumnInfo(name = "areaCode")
    val areaCode: String,
    @ColumnInfo(name = "areaName")
    val areaName: String,
    @ColumnInfo(name = "areaType")
    val areaType: AreaType
)

@Dao
interface AreaDao {

    @Query("SELECT COUNT(areaCode) FROM area")
    fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(area: List<AreaEntity>)

    @Query("SELECT * FROM area WHERE areaName LIKE :areaName ORDER BY areaName ASC")
    fun search(areaName: String): List<AreaEntity>

    @Query("SELECT * FROM area INNER JOIN savedArea ON area.areaCode = savedArea.areaCode ORDER BY areaName ASC")
    fun allSavedAreas(): List<AreaEntity>
}

@Entity(
    tableName = "areaData",
    primaryKeys = ["areaCode", "date"]
)
data class AreaDataEntity(
    @ColumnInfo(name = "metadataId")
    val metadataId: String,
    @ColumnInfo(name = "areaCode")
    val areaCode: String,
    @ColumnInfo(name = "areaName")
    val areaName: String,
    @ColumnInfo(name = "areaType")
    val areaType: AreaType,
    @ColumnInfo(name = "newCases")
    val newCases: Int,
    @ColumnInfo(name = "infectionRate")
    val infectionRate: Double,
    @ColumnInfo(name = "cumulativeCases")
    val cumulativeCases: Int,
    @ColumnInfo(name = "date")
    val date: LocalDate
)

data class AreaDataMetadataTuple(
    @ColumnInfo(name = "lastUpdatedAt")
    val lastUpdatedAt: LocalDateTime,
    @ColumnInfo(name = "areaCode")
    val areaCode: String,
    @ColumnInfo(name = "areaName")
    val areaName: String,
    @ColumnInfo(name = "areaType")
    val areaType: AreaType,
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

    @Query("DELETE FROM areaData WHERE :areaCode = areaCode")
    fun deleteAllByAreaCode(areaCode: String)

    @Query("DELETE FROM areaData WHERE areaCode NOT IN (:areaCodes)")
    fun deleteAllNotInAreaCodes(areaCodes: List<String>)

    @Query("SELECT COUNT(areaCode) FROM areaData")
    fun countAll(): Int

    @Query("SELECT COUNT(areaCode) FROM areaData WHERE :areaType = areaType")
    fun countAllByAreaType(areaType: AreaType): Int

    @Query("SELECT * FROM areaData WHERE :areaCode = areaCode ORDER BY date ASC")
    fun allByAreaCodeAsFlow(areaCode: String): Flow<List<AreaDataEntity>>

    @Query("SELECT * FROM areaData WHERE :areaCode = areaCode ORDER BY date ASC")
    fun allByAreaCode(areaCode: String): List<AreaDataEntity>

    @Query("SELECT * FROM areaData INNER JOIN metadata on areaData.metadataId = metadata.id WHERE areaCode IN (:areaCodes) ORDER BY date DESC LIMIT :limit")
    fun latestWithMetadataByAreaCodeAsFlow(
        areaCodes: List<String>,
        limit: Int = areaCodes.size
    ): Flow<List<AreaDataMetadataTuple>>

    @Query("SELECT * FROM areaData INNER JOIN savedArea ON areaData.areaCode = savedArea.areaCode ORDER BY date ASC")
    fun allSavedAreaDataAsFlow(): Flow<List<AreaDataEntity>>

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

    @Query("DELETE FROM metadata WHERE id NOT IN (:id)")
    fun deleteAllNotInIds(id: List<String>)

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

    @Query("SELECT * FROM savedArea")
    fun all(): List<SavedAreaEntity>

    @Delete
    fun delete(savedAreaEntity: SavedAreaEntity): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(savedAreaEntity: SavedAreaEntity)

    @Query("SELECT COUNT(areaCode) > 0 FROM savedArea WHERE areaCode = :areaCode")
    fun isSaved(areaCode: String): Flow<Boolean>
}

object Constants {
    const val UK_AREA_CODE = "K02000001"
    const val ENGLAND_AREA_CODE = "E92000001"
    const val NORTHERN_IRELAND_AREA_CODE = "N92000002"
    const val SCOTLAND_AREA_CODE = "S92000003"
    const val WALES_AREA_CODE = "W92000004"
}

object MetaDataIds {
    fun areaListId(): String = "AREA_LIST_METADATA"
    fun areaSummaryId(): String = "AREA_SUMMARY_METADATA"
    fun areaCodeId(areaCode: String) = "AREA_${areaCode}_METADATA"
}

@Entity(
    tableName = "areaSummary",
    primaryKeys = ["areaCode"]
)
data class AreaSummaryEntity(
    @ColumnInfo(name = "areaCode")
    val areaCode: String,
    @ColumnInfo(name = "areaName")
    val areaName: String,
    @ColumnInfo(name = "areaType")
    val areaType: AreaType,
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "baseInfectionRate")
    val baseInfectionRate: Double,
    @ColumnInfo(name = "cumulativeCasesWeek1")
    val cumulativeCasesWeek1: Int,
    @ColumnInfo(name = "cumulativeCaseInfectionRateWeek1")
    val cumulativeCaseInfectionRateWeek1: Double,
    @ColumnInfo(name = "newCasesWeek1")
    val newCasesWeek1: Int,
    @ColumnInfo(name = "newCaseInfectionRateWeek1")
    val newCaseInfectionRateWeek1: Double,
    @ColumnInfo(name = "cumulativeCasesWeek2")
    val cumulativeCasesWeek2: Int,
    @ColumnInfo(name = "cumulativeCaseInfectionRateWeek2")
    val cumulativeCaseInfectionRateWeek2: Double,
    @ColumnInfo(name = "newCasesWeek2")
    val newCasesWeek2: Int,
    @ColumnInfo(name = "newCaseInfectionRateWeek2")
    val newCaseInfectionRateWeek2: Double,
    @ColumnInfo(name = "cumulativeCasesWeek3")
    val cumulativeCasesWeek3: Int,
    @ColumnInfo(name = "cumulativeCaseInfectionRateWeek3")
    val cumulativeCaseInfectionRateWeek3: Double,
    @ColumnInfo(name = "newCasesWeek3")
    val newCasesWeek3: Int,
    @ColumnInfo(name = "newCaseInfectionRateWeek¬3")
    val newCaseInfectionRateWeek3: Double,
    @ColumnInfo(name = "cumulativeCasesWeek4")
    val cumulativeCasesWeek4: Int,
    @ColumnInfo(name = "cumulativeCaseInfectionRateWeek4")
    val cumulativeCaseInfectionRateWeek4: Double
)

@Dao
interface AreaSummaryEntityDao {

    @Query("SELECT COUNT(areaCode) FROM areaSummary")
    fun countAll(): Int

    @Query("DELETE FROM areaSummary")
    fun deleteAll()

    @Query("SELECT * FROM areaSummary WHERE areaCode = :areaCode")
    fun byAreaCode(areaCode: String): AreaSummaryEntity

    @Query("SELECT * FROM areaSummary ORDER BY newCaseInfectionRateWeek1 DESC LIMIT 10")
    fun topAreasByLatestCaseInfectionRateAsFlow(): Flow<List<AreaSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(areaSummaries: List<AreaSummaryEntity>)
}
