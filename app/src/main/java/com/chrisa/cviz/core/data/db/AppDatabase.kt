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

package com.chrisa.cviz.core.data.db

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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
        SavedAreaEntity::class,
        HealthcareEntity::class,
        AreaLookupEntity::class,
        HealthcareLookupEntity::class,
        AlertLevelEntity::class,
        SoaDataEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(
    AreTypeConverter::class,
    LocalDateConverter::class,
    LocalDateTimeConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun areaDao(): AreaDao
    abstract fun areaDataDao(): AreaDataDao
    abstract fun areaSummaryDao(): AreaSummaryDao
    abstract fun metadataDao(): MetadataDao
    abstract fun savedAreaDao(): SavedAreaDao
    abstract fun areaLookupDao(): AreaLookupDao
    abstract fun healthcareDao(): HealthcareDao
    abstract fun healthcareLookupDao(): HealthcareLookupDao
    abstract fun alertLevelDao(): AlertLevelDao
    abstract fun soaDataDao(): SoaDataDao

    companion object {
        private const val databaseName = "cviz-db"

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `alertLevel` (`areaCode` TEXT NOT NULL, `areaName` TEXT NOT NULL, `areaType` TEXT NOT NULL, `date` INTEGER NOT NULL, `alertLevel` INTEGER NOT NULL, `alertLevelName` TEXT NOT NULL, `alertLevelUrl` TEXT NOT NULL, `alertLevelValue` INTEGER NOT NULL, PRIMARY KEY(`areaCode`))")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `soaData` (`areaCode` TEXT NOT NULL, `areaName` TEXT NOT NULL, `areaType` TEXT NOT NULL, `date` INTEGER NOT NULL, `rollingSum` INTEGER NOT NULL, `rollingRate` REAL NOT NULL, `change` INTEGER NOT NULL, `changePercentage` REAL NOT NULL, PRIMARY KEY(`areaCode`, `date`))")
                database.execSQL("DELETE FROM `alertLevel`")
                database.execSQL("ALTER TABLE `alertLevel` ADD COLUMN `trimmedPostcode` TEXT NOT NULL")
                database.execSQL("ALTER TABLE `alertLevel` ADD COLUMN `postcode` TEXT NOT NULL")
            }
        }

        fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
    NHS_REGION("nhsRegion"),
    NHS_TRUST("nhsTrust"),
    UTLA("utla"),
    LTLA("ltla"),
    MSOA("msoa");

    companion object {
        fun from(type: String): AreaType? {
            return when (type) {
                OVERVIEW.value -> OVERVIEW
                NATION.value -> NATION
                REGION.value -> REGION
                NHS_REGION.value -> NHS_REGION
                NHS_TRUST.value -> NHS_TRUST
                UTLA.value -> UTLA
                LTLA.value -> LTLA
                MSOA.value -> MSOA
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

    @Query("SELECT * FROM area WHERE areaCode = :areaCode LIMIT 1")
    fun byAreaCode(areaCode: String): AreaEntity?

    @Query("SELECT * FROM area WHERE areaName LIKE '%' || :areaName || '%' ORDER BY areaName ASC")
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
    val date: LocalDate,
    @ColumnInfo(name = "newDeathsByPublishedDate")
    val newDeathsByPublishedDate: Int?,
    @ColumnInfo(name = "cumulativeDeathsByPublishedDate")
    val cumulativeDeathsByPublishedDate: Int?,
    @ColumnInfo(name = "cumulativeDeathsByPublishedDateRate")
    val cumulativeDeathsByPublishedDateRate: Double?,
    @ColumnInfo(name = "newDeathsByDeathDate")
    val newDeathsByDeathDate: Int?,
    @ColumnInfo(name = "cumulativeDeathsByDeathDate")
    val cumulativeDeathsByDeathDate: Int?,
    @ColumnInfo(name = "cumulativeDeathsByDeathDateRate")
    val cumulativeDeathsByDeathDateRate: Double?,
    @ColumnInfo(name = "newOnsDeathsByRegistrationDate")
    val newOnsDeathsByRegistrationDate: Int?,
    @ColumnInfo(name = "cumulativeOnsDeathsByRegistrationDate")
    val cumulativeOnsDeathsByRegistrationDate: Int?,
    @ColumnInfo(name = "cumulativeOnsDeathsByRegistrationDateRate")
    val cumulativeOnsDeathsByRegistrationDateRate: Double?
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

data class AreaCaseData(
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "newCases")
    val newCases: Int,
    @ColumnInfo(name = "infectionRate")
    val infectionRate: Double,
    @ColumnInfo(name = "cumulativeCases")
    val cumulativeCases: Int
)

data class AreaDeathData(
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "newDeathsByPublishedDate")
    val newDeathsByPublishedDate: Int?,
    @ColumnInfo(name = "cumulativeDeathsByPublishedDate")
    val cumulativeDeathsByPublishedDate: Int?,
    @ColumnInfo(name = "cumulativeDeathsByPublishedDateRate")
    val cumulativeDeathsByPublishedDateRate: Double?,
    @ColumnInfo(name = "newDeathsByDeathDate")
    val newDeathsByDeathDate: Int?,
    @ColumnInfo(name = "cumulativeDeathsByDeathDate")
    val cumulativeDeathsByDeathDate: Int?,
    @ColumnInfo(name = "cumulativeDeathsByDeathDateRate")
    val cumulativeDeathsByDeathDateRate: Double?,
    @ColumnInfo(name = "newOnsDeathsByRegistrationDate")
    val newOnsDeathsByRegistrationDate: Int?,
    @ColumnInfo(name = "cumulativeOnsDeathsByRegistrationDate")
    val cumulativeOnsDeathsByRegistrationDate: Int?,
    @ColumnInfo(name = "cumulativeOnsDeathsByRegistrationDateRate")
    val cumulativeOnsDeathsByRegistrationDateRate: Double?
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

    @Query("SELECT * FROM areaData WHERE :areaCode = areaCode ORDER BY date ASC")
    fun allAreaCasesByAreaCode(areaCode: String): List<AreaCaseData>

    @Query("SELECT * FROM areaData WHERE :areaCode = areaCode ORDER BY date ASC")
    fun allAreaDeathsByAreaCode(areaCode: String): List<AreaDeathData>

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

    @Query("SELECT COUNT(areaCode) FROM savedArea")
    fun countAll(): Int

    @Query("SELECT * FROM savedArea")
    fun all(): List<SavedAreaEntity>

    @Delete
    fun delete(savedAreaEntity: SavedAreaEntity): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(savedAreaEntity: SavedAreaEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(savedAreaEntities: List<SavedAreaEntity>)

    @Query("SELECT COUNT(areaCode) > 0 FROM savedArea WHERE areaCode = :areaCode")
    fun isSaved(areaCode: String): Flow<Boolean>
}

object Constants {
    const val UK_AREA_CODE = "K02000001"
    const val ENGLAND_AREA_CODE = "E92000001"
    const val NORTHERN_IRELAND_AREA_CODE = "N92000002"
    const val SCOTLAND_AREA_CODE = "S92000003"
    const val WALES_AREA_CODE = "W92000004"

    const val ENGLAND_AREA_NAME = "England"
    const val SCOTLAND_AREA_NAME = "Scotland"
    const val WALES_AREA_NAME = "Wales"
    const val NORTHERN_IRELAND_AREA_NAME = "Northern Ireland"
    const val UK_AREA_NAME = "United Kingdom"
}

object MetaDataIds {
    fun areaSummaryId(): String = "AREA_SUMMARY_METADATA"
    fun areaCodeId(areaCode: String) = "AREA_${areaCode}_METADATA"
    fun healthcareId(areaCode: String) = "HEALTHCARE_${areaCode}_METADATA"
    fun alertLevelId(areaCode: String) = "ALERT_LEVEL_${areaCode}_METADATA"
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
interface AreaSummaryDao {

    @Query("SELECT COUNT(areaCode) FROM areaSummary")
    fun countAll(): Int

    @Query("DELETE FROM areaSummary")
    fun deleteAll()

    @Query("SELECT * FROM areaSummary WHERE areaCode = :areaCode")
    fun byAreaCode(areaCode: String): AreaSummaryEntity

    @Query("SELECT * FROM areaSummary")
    fun allAsFlow(): Flow<List<AreaSummaryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(areaSummaries: List<AreaSummaryEntity>)
}

@Entity(
    tableName = "areaLookup",
    primaryKeys = ["lsoaCode"]
)
data class AreaLookupEntity(
    @ColumnInfo(name = "postcode")
    val postcode: String,
    @ColumnInfo(name = "trimmedPostcode")
    val trimmedPostcode: String,
    @ColumnInfo(name = "lsoaCode")
    val lsoaCode: String,
    @ColumnInfo(name = "lsoaName")
    val lsoaName: String?,
    @ColumnInfo(name = "msoaCode")
    val msoaCode: String,
    @ColumnInfo(name = "msoaName")
    val msoaName: String?,
    @ColumnInfo(name = "ltlaCode")
    val ltlaCode: String,
    @ColumnInfo(name = "ltlaName")
    val ltlaName: String,
    @ColumnInfo(name = "utlaCode")
    val utlaCode: String,
    @ColumnInfo(name = "utlaName")
    val utlaName: String,
    @ColumnInfo(name = "nhsTrustCode")
    val nhsTrustCode: String?,
    @ColumnInfo(name = "nhsTrustName")
    val nhsTrustName: String?,
    @ColumnInfo(name = "nhsRegionCode")
    val nhsRegionCode: String?,
    @ColumnInfo(name = "nhsRegionName")
    val nhsRegionName: String?,
    @ColumnInfo(name = "regionCode")
    val regionCode: String?,
    @ColumnInfo(name = "regionName")
    val regionName: String?,
    @ColumnInfo(name = "nationCode")
    val nationCode: String,
    @ColumnInfo(name = "nationName")
    val nationName: String
)

@Dao
interface AreaLookupDao {

    @Query("SELECT COUNT(msoaCode) FROM areaLookup")
    fun countAll(): Int

    @Query("SELECT * FROM areaLookup WHERE msoaCode = :code LIMIT 1")
    fun byMsoa(code: String): AreaLookupEntity?

    @Query("SELECT * FROM areaLookup WHERE ltlaCode = :code LIMIT 1")
    fun byLtla(code: String): AreaLookupEntity?

    @Query("SELECT * FROM areaLookup WHERE utlaCode = :code LIMIT 1")
    fun byUtla(code: String): AreaLookupEntity?

    @Query("SELECT * FROM areaLookup WHERE regionCode = :code LIMIT 1")
    fun byRegion(code: String): AreaLookupEntity?

    @Query("SELECT * FROM areaLookup WHERE nhsRegionCode = :code LIMIT 1")
    fun byNhsRegion(code: String): AreaLookupEntity?

    @Query("SELECT * FROM areaLookup WHERE nhsTrustCode = :code LIMIT 1")
    fun byNhsTrustCode(code: String): AreaLookupEntity?

    @Query("SELECT * FROM areaLookup WHERE trimmedPostcode = :code LIMIT 1")
    fun byTrimmedPostcode(code: String): AreaLookupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(areaLookupEntities: List<AreaLookupEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(areaLookupEntity: AreaLookupEntity)
}

@Entity(
    tableName = "healthcare",
    primaryKeys = ["areaCode", "date"]
)
data class HealthcareEntity(
    @ColumnInfo(name = "areaCode")
    val areaCode: String,
    @ColumnInfo(name = "areaName")
    val areaName: String,
    @ColumnInfo(name = "areaType")
    val areaType: AreaType,
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "newAdmissions")
    val newAdmissions: Int?,
    @ColumnInfo(name = "cumulativeAdmissions")
    val cumulativeAdmissions: Int?,
    @ColumnInfo(name = "occupiedBeds")
    val occupiedBeds: Int?,
    @ColumnInfo(name = "transmissionRateMin")
    val transmissionRateMin: Double?,
    @ColumnInfo(name = "transmissionRateMax")
    val transmissionRateMax: Double?,
    @ColumnInfo(name = "transmissionRateGrowthRateMin")
    val transmissionRateGrowthRateMin: Double?,
    @ColumnInfo(name = "transmissionRateGrowthRateMax")
    val transmissionRateGrowthRateMax: Double?
)

@Dao
interface HealthcareDao {

    @Query("DELETE FROM healthcare WHERE :areaCode = areaCode")
    fun deleteAllByAreaCode(areaCode: String)

    @Query("SELECT * FROM healthcare WHERE areaCode = :code")
    fun byAreaCode(code: String): List<HealthcareEntity>

    @Query("SELECT * FROM healthcare WHERE areaCode IN (:areaCodes)")
    fun byAreaCodes(areaCodes: List<String>): List<HealthcareEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(healthcareData: List<HealthcareEntity>)
}

@Entity(
    tableName = "healthcareLookup",
    primaryKeys = ["areaCode", "nhsTrustCode"]
)
data class HealthcareLookupEntity(
    @ColumnInfo(name = "areaCode")
    val areaCode: String,
    @ColumnInfo(name = "nhsTrustCode")
    val nhsTrustCode: String
)

@Dao
interface HealthcareLookupDao {

    @Query("SELECT COUNT(nhsTrustCode) FROM healthcareLookup")
    fun countAll(): Int

    @Query("SELECT * FROM healthcareLookup WHERE areaCode = :code")
    fun byAreaCode(code: String): List<HealthcareLookupEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: HealthcareLookupEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<HealthcareLookupEntity>)
}

@Entity(
    tableName = "alertLevel",
    primaryKeys = ["areaCode"]
)
data class AlertLevelEntity(
    @ColumnInfo(name = "areaCode")
    val areaCode: String,
    @ColumnInfo(name = "areaName")
    val areaName: String,
    @ColumnInfo(name = "areaType")
    val areaType: AreaType,
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "alertLevel")
    val alertLevel: Int,
    @ColumnInfo(name = "alertLevelName")
    val alertLevelName: String,
    @ColumnInfo(name = "alertLevelUrl")
    val alertLevelUrl: String,
    @ColumnInfo(name = "alertLevelValue")
    val alertLevelValue: Int
)

@Dao
interface AlertLevelDao {

    @Query("SELECT * FROM alertLevel WHERE areaCode = :areaCode")
    fun byAreaCode(areaCode: String): AlertLevelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: AlertLevelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<AlertLevelEntity>)
}

@Entity(
    tableName = "soaData",
    primaryKeys = ["areaCode", "date"]
)
data class SoaDataEntity(
    @ColumnInfo(name = "areaCode")
    val areaCode: String,
    @ColumnInfo(name = "areaName")
    val areaName: String,
    @ColumnInfo(name = "areaType")
    val areaType: AreaType,
    @ColumnInfo(name = "date")
    val date: LocalDate,
    @ColumnInfo(name = "rollingSum")
    val rollingSum: Int,
    @ColumnInfo(name = "rollingRate")
    val rollingRate: Double,
    @ColumnInfo(name = "change")
    val change: Int,
    @ColumnInfo(name = "changePercentage")
    val changePercentage: Double
)

@Dao
interface SoaDataDao {

    @Query("SELECT * FROM soaData WHERE areaCode = :areaCode ORDER BY date ASC")
    fun byAreaCode(areaCode: String): List<SoaDataEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<SoaDataEntity>)

    @Query("DELETE FROM soaData WHERE :areaCode = areaCode")
    fun deleteAllByAreaCode(areaCode: String)
}
