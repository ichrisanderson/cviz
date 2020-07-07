package com.chrisa.covid19.core.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27])
class DailyRecordDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Test
    fun `GIVEN no daily record exists WHEN insertDailyRecord called THEN dailyRecordsEntity is inserted`() {

        val newDailyRecordsEntity = DailyRecordEntity(
            areaName = "UK",
            date = Date(0),
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33
        )

        db.dailyRecordsDao().insertAll(listOf(newDailyRecordsEntity))

        val dailyRecords = db.dailyRecordsDao().searchDailyRecords(newDailyRecordsEntity.areaName)

        assertThat(dailyRecords.size).isEqualTo(1)
        assertThat(dailyRecords.first()).isEqualTo(newDailyRecordsEntity)
    }

    @Test
    fun `GIVEN daily record exists WHEN insertDailyRecord called with same date and area name THEN old dailyRecordsEntity is updated`() {

        val oldDailyRecordsEntity = DailyRecordEntity(
            areaName = "UK",
            date = Date(0),
            dailyLabConfirmedCases = 0,
            totalLabConfirmedCases = 0
        )

        db.dailyRecordsDao().insertAll(listOf(oldDailyRecordsEntity))

        val newDailyRecordsEntity = DailyRecordEntity(
            areaName = "UK",
            date = Date(0),
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33
        )

        db.dailyRecordsDao().insertAll(listOf(newDailyRecordsEntity))

        val dailyRecords = db.dailyRecordsDao().searchDailyRecords(newDailyRecordsEntity.areaName)

        assertThat(dailyRecords.size).isEqualTo(1)
        assertThat(dailyRecords.first()).isEqualTo(newDailyRecordsEntity)
    }

    @Test
    fun `GIVEN daily record exists WHEN insertDailyRecord called with same area name and different date THEN new dailyRecordsEntity is inserted`() {

        val oldDailyRecordsEntity = DailyRecordEntity(
            areaName = "UK",
            date = Date(0),
            dailyLabConfirmedCases = 0,
            totalLabConfirmedCases = 0
        )

        db.dailyRecordsDao().insertAll(listOf(oldDailyRecordsEntity))

        val newDailyRecordsEntity = DailyRecordEntity(
            areaName = "UK",
            date = Date(1),
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33
        )

        db.dailyRecordsDao().insertAll(listOf(newDailyRecordsEntity))

        val dailyRecords = db.dailyRecordsDao().searchDailyRecords(newDailyRecordsEntity.areaName)

        assertThat(dailyRecords.size).isEqualTo(2)
        assertThat(dailyRecords[0]).isEqualTo(oldDailyRecordsEntity)
        assertThat(dailyRecords[1]).isEqualTo(newDailyRecordsEntity)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}


