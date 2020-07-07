package com.chrisa.covid19.core.data.db

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
import java.util.Date

@Database(
    entities = [
        CaseEntity::class,
        DeathEntity::class,
        MetadataEntity::class,
        DailyRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun casesDao(): CaseDao
    abstract fun deathsDao(): DeathDao
    abstract fun dailyRecordsDao(): DailyRecordDao
    abstract fun metadataDao(): MetadataDao

    companion object {
        private const val databaseName = "covid19-uk-db"
        fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
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
    val date: Date
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
    fun searchAllAreas(areaName: String): List<AreaTupleEntity>

    @Query("SELECT * FROM cases WHERE areaCode = :areaCode ORDER BY date ASC")
    fun searchAllCases(areaCode: String): List<CaseEntity>
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
    val date: Date,
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
    fun searchAllDeathsOrderedByDateDesc(areaCode: String): List<DeathEntity>
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
    val date: Date
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
    val lastUpdatedAt: Date
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
    fun searchMetadata(id: String): List<MetadataEntity>
}
