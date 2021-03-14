package com.chrisa.cviz.core.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.time.LocalDateTime


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27])
class UnusedDataCleanerTest {

    private lateinit var db: AppDatabase
    private lateinit var sut: UnusedDataCleaner

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        db.areaDao().insertAll(
            listOf(
                marlyboneArea,
                centralWestminsterArea,
                oxfordCentralArea
            ).plus(nations)
        )

        sut = UnusedDataCleaner(db)
    }

    @Test
    fun `GIVEN no soa-areas saved WHEN removeUnusedData called THEN soa data cleared`() {
        val currentTime = LocalDateTime.of(2020, 1, 1, 9, 0)
        val westminsterData =
            SoaDataEntity(
                areaCode = centralWestminsterArea.areaCode,
                change = -4,
                changePercentage = -17.6,
                date = LocalDate.of(2021, 2, 14),
                rollingRate = 55.9,
                rollingSum = 9
            )
        val soaData = listOf(
            westminsterData,
            westminsterData.copy(
                areaCode = marlyboneArea.areaCode
            ),
            westminsterData.copy(
                areaCode = oxfordCentralArea.areaCode
            )
        )
        val westminsterMetadata = MetadataEntity(
            id = MetaDataIds.areaCodeId(centralWestminsterArea.areaCode),
            lastUpdatedAt = currentTime.minusDays(1),
            lastSyncTime = currentTime.minusHours(1)
        )
        db.soaDataDao().insertAll(soaData)
        db.metadataDao().insertAll(
            listOf(
                westminsterMetadata,
                westminsterMetadata.copy(
                    id = MetaDataIds.areaCodeId(marlyboneArea.areaCode),
                    lastSyncTime = currentTime.minusDays(3)
                ),
                westminsterMetadata.copy(
                    id = MetaDataIds.areaCodeId(oxfordCentralArea.areaCode),
                    lastSyncTime = currentTime.minusDays(1)
                )
            )
        )

        runBlocking { sut.removeUnusedData() }

        assertThat(db.soaDataDao().all()).isEmpty()
    }

    companion object {
        val marlyboneArea = AreaEntity(
            areaCode = "E02000970",
            areaName = "Marylebone & Park Lane",
            areaType = AreaType.MSOA
        )
        val centralWestminsterArea = AreaEntity(
            areaCode = "E02000979",
            areaName = "Central Westminster",
            areaType = AreaType.MSOA
        )
        val oxfordCentralArea = AreaEntity(
            areaCode = "E02005947",
            areaName = "Oxford Central",
            areaType = AreaType.MSOA
        )
        val nations = listOf(
            AreaEntity(
                areaCode = Constants.UK_AREA_CODE,
                areaName = Constants.UK_AREA_NAME,
                areaType = AreaType.OVERVIEW
            ),
            AreaEntity(
                areaCode = Constants.ENGLAND_AREA_CODE,
                areaName = Constants.ENGLAND_AREA_NAME,
                areaType = AreaType.NATION
            ),
            AreaEntity(
                areaCode = Constants.NORTHERN_IRELAND_AREA_CODE,
                areaName = Constants.NORTHERN_IRELAND_AREA_NAME,
                areaType = AreaType.NATION
            ),
            AreaEntity(
                areaCode = Constants.SCOTLAND_AREA_CODE,
                areaName = Constants.SCOTLAND_AREA_NAME,
                areaType = AreaType.NATION
            ),
            AreaEntity(
                areaCode = Constants.WALES_AREA_CODE,
                areaName = Constants.WALES_AREA_NAME,
                areaType = AreaType.NATION
            )
        )
    }
}
