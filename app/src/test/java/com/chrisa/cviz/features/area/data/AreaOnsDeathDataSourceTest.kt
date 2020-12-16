package com.chrisa.cviz.features.area.data

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaDataDao
import com.chrisa.cviz.core.data.db.AreaDeathData
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class AreaOnsDeathDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()
    private val areaDataDao = mockk<AreaDataDao>()
    private val sut = AreaOnsDeathsDataSource(appDatabase)

    @Before
    fun setup() {
        every { appDatabase.areaDataDao() } returns areaDataDao
    }

    @Test
    fun `GIVEN area does not have ons deaths WHEN onsDeathsByRegistrationDate called THEN empty list emitted`() {
        every { areaDataDao.allAreaDeathsByAreaCode("") } returns listOf(areaData)

        val onsDeathsByRegistrationDate = sut.deaths("")

        assertThat(onsDeathsByRegistrationDate).isEmpty()
    }

    @Test
    fun `GIVEN area does has ons deaths WHEN onsDeathsByRegistrationDate called THEN deaths are emitted`() {
        every { areaDataDao.allAreaDeathsByAreaCode("") } returns listOf(areaDataWithOnsDeaths)

        val onsDeathsByRegistrationDate = sut.deaths("")

        assertThat(onsDeathsByRegistrationDate).isEqualTo(
            listOf(
                DailyData(
                    date = areaDataWithOnsDeaths.date,
                    newValue = areaDataWithOnsDeaths.newOnsDeathsByRegistrationDate!!,
                    cumulativeValue = areaDataWithOnsDeaths.cumulativeOnsDeathsByRegistrationDate!!,
                    rate = areaDataWithOnsDeaths.cumulativeOnsDeathsByRegistrationDateRate!!
                )
            )
        )
    }

    companion object {
        private val syncDate = LocalDateTime.of(2020, 1, 1, 0, 0)

        private val areaData = AreaDeathData(
            date = syncDate.toLocalDate(),
            newDeathsByPublishedDate = null,
            cumulativeDeathsByPublishedDate = null,
            cumulativeDeathsByPublishedDateRate = null,
            newDeathsByDeathDate = null,
            cumulativeDeathsByDeathDate = null,
            cumulativeDeathsByDeathDateRate = null,
            newOnsDeathsByRegistrationDate = null,
            cumulativeOnsDeathsByRegistrationDate = null,
            cumulativeOnsDeathsByRegistrationDateRate = null
        )

        private val areaDataWithOnsDeaths = areaData.copy(
            newOnsDeathsByRegistrationDate = 10,
            cumulativeOnsDeathsByRegistrationDate = 100,
            cumulativeOnsDeathsByRegistrationDateRate = 20.0
        )
    }
}
