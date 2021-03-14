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
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

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
        SoaDataEntity::class,
        AreaAssociation::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(
    AreTypeConverter::class,
    LocalDateConverter::class,
    LocalDateTimeConverter::class,
    AreaAssociationTypeConverter::class
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
    abstract fun areaAssociationDao(): AreaAssociationDao

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
                database.execSQL("ALTER TABLE `areaLookup` ADD COLUMN `trimmedPostcode` TEXT DEFAULT '' NOT NULL")
                database.execSQL("ALTER TABLE `areaLookup` ADD COLUMN `postcode` TEXT DEFAULT '' NOT NULL ")
                database.execSQL("DELETE FROM `areaLookup`")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `areaData_tmp` (`areaCode` TEXT NOT NULL, `metadataId` TEXT NOT NULL, `newCases` INTEGER NOT NULL, `infectionRate` REAL NOT NULL, `cumulativeCases` INTEGER NOT NULL, `date` INTEGER NOT NULL, `newDeathsByPublishedDate` INTEGER, `cumulativeDeathsByPublishedDate` INTEGER, `cumulativeDeathsByPublishedDateRate` REAL, `newDeathsByDeathDate` INTEGER, `cumulativeDeathsByDeathDate` INTEGER, `cumulativeDeathsByDeathDateRate` REAL, `newOnsDeathsByRegistrationDate` INTEGER, `cumulativeOnsDeathsByRegistrationDate` INTEGER, `cumulativeOnsDeathsByRegistrationDateRate` REAL, PRIMARY KEY(`areaCode`, `date`), FOREIGN KEY(`areaCode`) REFERENCES `area`(`areaCode`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`metadataId`) REFERENCES `metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("DROP TABLE `areaData`")
                database.execSQL("ALTER TABLE `areaData_tmp` RENAME TO `areaData`")

                database.execSQL("CREATE TABLE IF NOT EXISTS `areaSummary_tmp` (`areaCode` TEXT NOT NULL, `date` INTEGER NOT NULL, `baseInfectionRate` REAL NOT NULL, `cumulativeCasesWeek1` INTEGER NOT NULL, `cumulativeCaseInfectionRateWeek1` REAL NOT NULL, `newCasesWeek1` INTEGER NOT NULL, `newCaseInfectionRateWeek1` REAL NOT NULL, `cumulativeCasesWeek2` INTEGER NOT NULL, `cumulativeCaseInfectionRateWeek2` REAL NOT NULL, `newCasesWeek2` INTEGER NOT NULL, `newCaseInfectionRateWeek2` REAL NOT NULL, `cumulativeCasesWeek3` INTEGER NOT NULL, `cumulativeCaseInfectionRateWeek3` REAL NOT NULL, `newCasesWeek3` INTEGER NOT NULL, `newCaseInfectionRateWeek3` REAL NOT NULL, `cumulativeCasesWeek4` INTEGER NOT NULL, `cumulativeCaseInfectionRateWeek4` REAL NOT NULL, PRIMARY KEY(`areaCode`), FOREIGN KEY(`areaCode`) REFERENCES `area`(`areaCode`) ON UPDATE NO ACTION ON DELETE NO ACTION )")
                database.execSQL("DROP TABLE `areaSummary`")
                database.execSQL("ALTER TABLE `areaSummary_tmp` RENAME TO `areaSummary`")

                database.execSQL("CREATE TABLE IF NOT EXISTS `savedArea_tmp` (`areaCode` TEXT NOT NULL, PRIMARY KEY(`areaCode`), FOREIGN KEY(`areaCode`) REFERENCES `area`(`areaCode`) ON UPDATE NO ACTION ON DELETE NO ACTION )")
                database.execSQL("INSERT INTO `savedArea_tmp` (`areaCode`) SELECT `areaCode` FROM `savedArea`")
                database.execSQL("DROP TABLE `savedArea`")
                database.execSQL("ALTER TABLE `savedArea_tmp` RENAME TO `savedArea`")

                database.execSQL("CREATE TABLE IF NOT EXISTS `healthcare_tmp` (`areaCode` TEXT NOT NULL, `metadataId` TEXT NOT NULL, `date` INTEGER NOT NULL, `newAdmissions` INTEGER, `cumulativeAdmissions` INTEGER, `occupiedBeds` INTEGER, `transmissionRateMin` REAL, `transmissionRateMax` REAL, `transmissionRateGrowthRateMin` REAL, `transmissionRateGrowthRateMax` REAL, PRIMARY KEY(`areaCode`, `date`), FOREIGN KEY(`areaCode`) REFERENCES `area`(`areaCode`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`metadataId`) REFERENCES `metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("DROP TABLE `healthcare`")
                database.execSQL("ALTER TABLE `healthcare_tmp` RENAME TO `healthcare`")

                database.execSQL("CREATE TABLE IF NOT EXISTS `alertLevel_tmp` (`areaCode` TEXT NOT NULL, `metadataId` TEXT NOT NULL, `date` INTEGER NOT NULL, `alertLevel` INTEGER NOT NULL, `alertLevelName` TEXT NOT NULL, `alertLevelUrl` TEXT NOT NULL, `alertLevelValue` INTEGER NOT NULL, PRIMARY KEY(`areaCode`), FOREIGN KEY(`areaCode`) REFERENCES `area`(`areaCode`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`metadataId`) REFERENCES `metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("DROP TABLE `alertLevel`")
                database.execSQL("ALTER TABLE `alertLevel_tmp` RENAME TO `alertLevel`")

                database.execSQL("CREATE TABLE `soaData_tmp` (`areaCode` TEXT NOT NULL, `metadataId` TEXT NOT NULL, `date` INTEGER NOT NULL, `rollingSum` INTEGER NOT NULL, `rollingRate` REAL NOT NULL, `change` INTEGER NOT NULL, `changePercentage` REAL NOT NULL, PRIMARY KEY(`areaCode`, `date`), FOREIGN KEY(`areaCode`) REFERENCES `area`(`areaCode`) ON UPDATE NO ACTION ON DELETE NO ACTION , FOREIGN KEY(`metadataId`) REFERENCES `metadata`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("DROP TABLE `soaData`")
                database.execSQL("ALTER TABLE `soaData_tmp` RENAME TO `soaData`")
            }
        }

        fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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
    val areaCode: String,
    val areaName: String,
    val areaType: AreaType
)

@Dao
interface AreaDao {

    @Query("SELECT COUNT(areaCode) FROM area")
    fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(area: AreaEntity)

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
    primaryKeys = ["areaCode", "date"],
    foreignKeys = [
        ForeignKey(
            entity = AreaEntity::class,
            parentColumns = ["areaCode"],
            childColumns = ["areaCode"]
        ),
        ForeignKey(
            entity = MetadataEntity::class,
            parentColumns = ["id"],
            childColumns = ["metadataId"],
            onDelete = CASCADE
        )
    ]
)
data class AreaDataEntity(
    val areaCode: String,
    val metadataId: String,
    val newCases: Int,
    val infectionRate: Double,
    val cumulativeCases: Int,
    val date: LocalDate,
    val newDeathsByPublishedDate: Int?,
    val cumulativeDeathsByPublishedDate: Int?,
    val cumulativeDeathsByPublishedDateRate: Double?,
    val newDeathsByDeathDate: Int?,
    val cumulativeDeathsByDeathDate: Int?,
    val cumulativeDeathsByDeathDateRate: Double?,
    val newOnsDeathsByRegistrationDate: Int?,
    val cumulativeOnsDeathsByRegistrationDate: Int?,
    val cumulativeOnsDeathsByRegistrationDateRate: Double?
)

data class AreaDataMetadataTuple(
    val lastUpdatedAt: LocalDateTime,
    val areaCode: String,
    val areaName: String,
    val areaType: AreaType,
    val newCases: Int,
    val infectionRate: Double,
    val cumulativeCases: Int,
    val date: LocalDate
)

data class AreaCaseData(
    val date: LocalDate,
    val newCases: Int,
    val infectionRate: Double,
    val cumulativeCases: Int
)

data class AreaDeathData(
    val date: LocalDate,
    val newDeathsByPublishedDate: Int?,
    val cumulativeDeathsByPublishedDate: Int?,
    val cumulativeDeathsByPublishedDateRate: Double?,
    val newDeathsByDeathDate: Int?,
    val cumulativeDeathsByDeathDate: Int?,
    val cumulativeDeathsByDeathDateRate: Double?,
    val newOnsDeathsByRegistrationDate: Int?,
    val cumulativeOnsDeathsByRegistrationDate: Int?,
    val cumulativeOnsDeathsByRegistrationDateRate: Double?
)

data class AreaDataWithArea(
    val areaName: String,
    val areaType: AreaType,
    @Embedded
    val areaData: AreaDataEntity
)

@Dao
interface AreaDataDao {

    @Query("SELECT DISTINCT areaCode FROM areaData")
    fun distinctAreaCodes(): List<String>

    @Query("SELECT * FROM areaData")
    fun all(): List<AreaDataEntity>

    @Query("SELECT * FROM areaData WHERE areaCode IN (:areaCodes)")
    fun allInAreaCode(areaCodes: Collection<String>): AreaDataEntity

    @Query("DELETE FROM areaData WHERE :areaCode = areaCode")
    fun deleteAllByAreaCode(areaCode: String)

    @Query("DELETE FROM areaData WHERE areaCode IN (:areaCodes)")
    fun deleteAllNotInAreaCode(areaCodes: Collection<String>)

    @Query("DELETE FROM areaData WHERE areaCode IN (:areaCodes)")
    fun deleteAllInAreaCode(areaCodes: Collection<String>)

    @Query("SELECT COUNT(areaCode) FROM areaData")
    fun countAll(): Int

    @Query("SELECT * FROM areaData WHERE :areaCode = areaCode ORDER BY date ASC")
    fun allByAreaCodeAsFlow(areaCode: String): Flow<List<AreaDataEntity>>

    @Query("SELECT * FROM areaData WHERE :areaCode = areaCode ORDER BY date ASC")
    fun allByAreaCode(areaCode: String): List<AreaDataEntity>

    @Query("SELECT * FROM areaData WHERE :areaCode = areaCode ORDER BY date ASC")
    fun allAreaCasesByAreaCode(areaCode: String): List<AreaCaseData>

    @Query("SELECT * FROM areaData WHERE :areaCode = areaCode ORDER BY date ASC")
    fun allAreaDeathsByAreaCode(areaCode: String): List<AreaDeathData>

    @Query("SELECT lastUpdatedAt, area.areaCode AS areaCode, areaName, areaType, newCases, infectionRate, cumulativeCases, date  FROM areaData INNER JOIN area on areaData.areaCode = area.areaCode INNER JOIN metadata on areaData.metadataId = metadata.id WHERE area.areaCode IN (:areaCodes) ORDER BY date DESC LIMIT :limit")
    fun latestWithMetadataByAreaCodeAsFlow(
        areaCodes: List<String>,
        limit: Int = areaCodes.size
    ): Flow<List<AreaDataMetadataTuple>>

    @Query("SELECT * FROM areaData INNER JOIN savedArea ON areaData.areaCode = savedArea.areaCode INNER JOIN area ON areaData.areaCode = area.areaCode ORDER BY date ASC")
    fun allSavedAreaDataAsFlow(): Flow<List<AreaDataWithArea>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(areaData: List<AreaDataEntity>)
}

@Entity(
    tableName = "metadata",
    primaryKeys = ["id"]
)
data class MetadataEntity(
    val id: String,
    val lastUpdatedAt: LocalDateTime,
    val lastSyncTime: LocalDateTime
)

@Dao
interface MetadataDao {

    @Query("SELECT * FROM metadata")
    fun all(): List<MetadataEntity>

    @Query("DELETE FROM metadata WHERE id IN (:id)")
    fun deleteAllInId(id: Collection<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(metadata: MetadataEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(metadata: List<MetadataEntity>)

    @Query("SELECT * FROM metadata WHERE id = :id  LIMIT 1")
    fun metadata(id: String): MetadataEntity?

    @Query("SELECT * FROM metadata WHERE id = :id LIMIT 1")
    fun metadataAsFlow(id: String): Flow<MetadataEntity?>
}

@Entity(
    tableName = "savedArea",
    primaryKeys = ["areaCode"],
    foreignKeys = [
        ForeignKey(
            entity = AreaEntity::class,
            parentColumns = ["areaCode"],
            childColumns = ["areaCode"]
        )
    ]
)
data class SavedAreaEntity(
    @ColumnInfo(name = "areaCode")
    val areaCode: String
)

@Dao
interface SavedAreaDao {

    @Query("SELECT DISTINCT areaCode FROM savedArea")
    fun distinctAreaCodes(): List<String>

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

object MetadataIds {
    fun areaSummaryId(): String = "AREA_SUMMARY_METADATA"
    fun areaCodeId(areaCode: String) = "AREA_${areaCode}_METADATA"
    fun healthcareId(areaCode: String) = "HEALTHCARE_${areaCode}_METADATA"
    fun alertLevelId(areaCode: String) = "ALERT_LEVEL_${areaCode}_METADATA"
}

@Entity(
    tableName = "areaSummary",
    primaryKeys = ["areaCode"],
    foreignKeys = [
        ForeignKey(
            entity = AreaEntity::class,
            parentColumns = ["areaCode"],
            childColumns = ["areaCode"]
        )
    ]
)
data class AreaSummaryEntity(
    val areaCode: String,
    val date: LocalDate,
    val baseInfectionRate: Double,
    val cumulativeCasesWeek1: Int,
    val cumulativeCaseInfectionRateWeek1: Double,
    val newCasesWeek1: Int,
    val newCaseInfectionRateWeek1: Double,
    val cumulativeCasesWeek2: Int,
    val cumulativeCaseInfectionRateWeek2: Double,
    val newCasesWeek2: Int,
    val newCaseInfectionRateWeek2: Double,
    val cumulativeCasesWeek3: Int,
    val cumulativeCaseInfectionRateWeek3: Double,
    val newCasesWeek3: Int,
    val newCaseInfectionRateWeek3: Double,
    val cumulativeCasesWeek4: Int,
    val cumulativeCaseInfectionRateWeek4: Double
)

data class AreaSummaryWithArea(
    val areaName: String,
    val areaType: AreaType,
    @Embedded
    val areaSummary: AreaSummaryEntity
)

@Dao
interface AreaSummaryDao {

    @Query("SELECT COUNT(areaCode) FROM areaSummary")
    fun countAll(): Int

    @Query("DELETE FROM areaSummary")
    fun deleteAll()

    @Query("SELECT * FROM areaSummary WHERE areaCode = :areaCode")
    fun byAreaCode(areaCode: String): AreaSummaryEntity

    @Query("SELECT * FROM areaSummary INNER JOIN area ON areaSummary.areaCode = area.areaCode")
    fun allWithAreaAsFlow(): Flow<List<AreaSummaryWithArea>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(areaSummaries: List<AreaSummaryEntity>)
}

@Entity(
    tableName = "areaLookup",
    primaryKeys = ["lsoaCode"]
)
data class AreaLookupEntity(
    val postcode: String,
    val trimmedPostcode: String,
    val lsoaCode: String,
    val lsoaName: String?,
    val msoaCode: String,
    val msoaName: String?,
    val ltlaCode: String,
    val ltlaName: String,
    val utlaCode: String,
    val utlaName: String,
    val nhsTrustCode: String?,
    val nhsTrustName: String?,
    val nhsRegionCode: String?,
    val nhsRegionName: String?,
    val regionCode: String?,
    val regionName: String?,
    val nationCode: String,
    val nationName: String
)

@Dao
interface AreaLookupDao {

    @Query("SELECT * FROM areaLookup WHERE lsoaCode IN (:lsoaCodes)")
    fun allInLsoaCode(lsoaCodes: Collection<String>): List<AreaLookupEntity>

    @Query("DELETE FROM areaLookup WHERE lsoaCode NOT IN (:lsoaCodes)")
    fun deleteAllNotInLsoaCode(lsoaCodes: Collection<String>): Int

    @Query("SELECT COUNT(msoaCode) FROM areaLookup")
    fun countAll(): Int

    @Query("SELECT * FROM areaLookup")
    fun all(): List<AreaLookupEntity>

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
    primaryKeys = ["areaCode", "date"],
    foreignKeys = [
        ForeignKey(
            entity = AreaEntity::class,
            parentColumns = ["areaCode"],
            childColumns = ["areaCode"]
        ),
        ForeignKey(
            entity = MetadataEntity::class,
            parentColumns = ["id"],
            childColumns = ["metadataId"],
            onDelete = CASCADE
        )
    ]
)
data class HealthcareEntity(
    val areaCode: String,
    val metadataId: String,
    val date: LocalDate,
    val newAdmissions: Int?,
    val cumulativeAdmissions: Int?,
    val occupiedBeds: Int?,
    val transmissionRateMin: Double?,
    val transmissionRateMax: Double?,
    val transmissionRateGrowthRateMin: Double?,
    val transmissionRateGrowthRateMax: Double?
)

data class HealthcareWithArea(
    val areaName: String,
    val areaType: AreaType,
    @Embedded
    val healthcare: HealthcareEntity
)

@Dao
interface HealthcareDao {

    @Query("SELECT DISTINCT areaCode FROM healthcare")
    fun distinctAreaCodes(): List<String>

    @Query("SELECT * FROM healthcare")
    fun all(): List<HealthcareEntity>

    @Query("SELECT * FROM healthcare WHERE areaCode IN(:areaCode)")
    fun allInAreaCode(areaCode: Collection<String>): List<HealthcareEntity>

    @Query("DELETE FROM healthcare WHERE areaCode NOT IN(:areaCode)")
    fun deleteAllNotInAreaCode(areaCode: Collection<String>)

    @Query("DELETE FROM healthcare WHERE areaCode IN(:areaCode)")
    fun deleteAllInAreaCode(areaCode: Collection<String>)

    @Query("DELETE FROM healthcare WHERE :areaCode = areaCode")
    fun deleteAllByAreaCode(areaCode: String)

    @Query("SELECT * FROM healthcare WHERE areaCode = :code")
    fun byAreaCode(code: String): List<HealthcareEntity>

    @Query("SELECT * FROM healthcare INNER JOIN area ON healthcare.areaCode = area.areaCode WHERE healthcare.areaCode IN (:areaCodes)")
    fun withAreaByAreaCodes(areaCodes: List<String>): List<HealthcareWithArea>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(healthcareData: List<HealthcareEntity>)
}

@Entity(
    tableName = "healthcareLookup",
    primaryKeys = ["areaCode", "nhsTrustCode"]
)
data class HealthcareLookupEntity(
    val areaCode: String,
    val nhsTrustCode: String
)

@Dao
interface HealthcareLookupDao {
    @Query("SELECT * FROM healthcareLookup")
    fun all(): List<HealthcareLookupEntity>

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
    primaryKeys = ["areaCode"],
    foreignKeys = [
        ForeignKey(
            entity = AreaEntity::class,
            parentColumns = ["areaCode"],
            childColumns = ["areaCode"]
        ),
        ForeignKey(
            entity = MetadataEntity::class,
            parentColumns = ["id"],
            childColumns = ["metadataId"],
            onDelete = CASCADE
        )
    ]
)
data class AlertLevelEntity(
    val areaCode: String,
    val metadataId: String,
    val date: LocalDate,
    val alertLevel: Int,
    val alertLevelName: String,
    val alertLevelUrl: String,
    val alertLevelValue: Int
)

@Dao
interface AlertLevelDao {

    @Query("SELECT DISTINCT areaCode FROM alertLevel")
    fun distinctAreaCodes(): List<String>

    @Query("DELETE FROM alertLevel WHERE areaCode IN (:areaCodes)")
    fun deleteAllInAreaCode(areaCodes: Collection<String>)

    @Query("SELECT * FROM alertLevel")
    fun all(): List<AlertLevelEntity>

    @Query("SELECT * FROM alertLevel WHERE areaCode = :areaCode")
    fun byAreaCode(areaCode: String): AlertLevelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: AlertLevelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<AlertLevelEntity>)
}

@Entity(
    tableName = "soaData",
    primaryKeys = ["areaCode", "date"],
    foreignKeys = [
        ForeignKey(
            entity = AreaEntity::class,
            parentColumns = ["areaCode"],
            childColumns = ["areaCode"]
        ),
        ForeignKey(
            entity = MetadataEntity::class,
            parentColumns = ["id"],
            childColumns = ["metadataId"],
            onDelete = CASCADE
        )
    ]
)
data class SoaDataEntity(
    val areaCode: String,
    val metadataId: String,
    val date: LocalDate,
    val rollingSum: Int,
    val rollingRate: Double,
    val change: Int,
    val changePercentage: Double
)

data class SoaDataWithArea(
    val areaName: String,
    val areaType: AreaType,
    @Embedded
    val soaData: SoaDataEntity
)

@Dao
interface SoaDataDao {

    @Query("SELECT DISTINCT areaCode FROM soaData")
    fun distinctAreaCodes(): List<String>

    @Query("SELECT * FROM soaData WHERE areaCode IN (:areaCodes)")
    fun allInAreaCode(areaCodes: Collection<String>): List<SoaDataEntity>

    @Query("DELETE FROM soaData WHERE areaCode NOT IN (:areaCodes)")
    fun deleteAllNotInAreaCode(areaCodes: Collection<String>)

    @Query("DELETE FROM soaData WHERE areaCode IN (:areaCodes)")
    fun deleteAllInAreaCode(areaCodes: Collection<String>)

    @Query("DELETE FROM soaData WHERE areaCode = :areaCode")
    fun deleteAllByAreaCode(areaCode: String)

    @Query("SELECT * FROM soaData INNER JOIN area ON soaData.areaCode = area.areaCode ORDER BY date ASC")
    fun allWithAreaAsFlow(): Flow<List<SoaDataWithArea>>

    @Query("SELECT * FROM soaData INNER JOIN savedArea ON soaData.areaCode = savedArea.areaCode INNER JOIN area ON soaData.areaCode = area.areaCode ORDER BY date ASC")
    fun allSavedAreaWithAreaAsFlow(): Flow<List<SoaDataWithArea>>

    @Query("SELECT * FROM soaData ORDER BY date ASC")
    fun all(): List<SoaDataEntity>

    @Query("SELECT * FROM soaData INNER JOIN area ON soaData.areaCode = area.areaCode WHERE soaData.areaCode = :areaCode ORDER BY date ASC")
    fun withAreaByAreaCode(areaCode: String): List<SoaDataWithArea>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<SoaDataEntity>)
}

enum class AreaAssociationType(val value: String) {
    AREA_LOOKUP("area_lookup"),
    AREA_DATA("area_data"),
    HEALTHCARE_DATA("healthcare_data");

    companion object {
        fun from(type: String): AreaAssociationType? {
            return when (type) {
                AREA_LOOKUP.value -> AREA_LOOKUP
                AREA_DATA.value -> AREA_DATA
                HEALTHCARE_DATA.value -> HEALTHCARE_DATA
                else -> null
            }
        }
    }
}

class AreaAssociationTypeConverter {
    @TypeConverter
    fun areaAssociationTypeFromString(value: String?): AreaAssociationType? {
        return value?.let { AreaAssociationType.from(value) }
    }

    @TypeConverter
    fun areaAssociationTypeToString(areaType: AreaAssociationType?): String? {
        return areaType?.value
    }
}

@Entity(
    tableName = "areaAssociation",
    primaryKeys = ["areaCode", "associatedAreaCode", "associatedAreaType"]
)
data class AreaAssociation(
    val areaCode: String,
    val associatedAreaCode: String,
    val associatedAreaType: AreaAssociationType
)

@Dao
interface AreaAssociationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: AreaAssociation)

    @Query("DELETE FROM areaAssociation WHERE areaCode = :areaCode")
    fun deleteAllByAreaCode(areaCode: String)

    @Query("SELECT * FROM areaAssociation WHERE areaCode = :code")
    fun byAreaCode(code: String): List<AreaAssociation>

    @Query("SELECT * FROM areaAssociation WHERE areaCode IN(:areaCode)")
    fun inAreaCode(areaCode: List<String>): List<AreaAssociation>
}
