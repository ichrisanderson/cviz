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

package com.chrisa.cviz.features.area.data

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaDataDao
import com.chrisa.cviz.core.data.db.AreaDeathData
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import org.junit.Before
import org.junit.Test

class AreaDeathsDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()
    private val areaDataDao = mockk<AreaDataDao>()
    private val sut = AreaDeathsDataSource(appDatabase)

    @Before
    fun setup() {
        every { appDatabase.areaDataDao() } returns areaDataDao
    }

    @Test
    fun `GIVEN area does not have published deaths WHEN deathsByPublishedDate called THEN empty list emitted`() {
        every { areaDataDao.allAreaDeathsByAreaCode("") } returns listOf(areaData)

        val deathsByPublishedDate = sut.deathsByPublishedDate("")

        assertThat(deathsByPublishedDate).isEmpty()
    }

    @Test
    fun `GIVEN area does has published deaths WHEN deathsByPublishedDate called THEN deaths are emitted`() {
        every { areaDataDao.allAreaDeathsByAreaCode("") } returns listOf(areaDataWithPublishedDeaths)

        val deathsByPublishedDate = sut.deathsByPublishedDate("")

        assertThat(deathsByPublishedDate).isEqualTo(
            listOf(
                DailyData(
                    date = areaDataWithPublishedDeaths.date,
                    newValue = areaDataWithPublishedDeaths.newDeathsByPublishedDate!!,
                    cumulativeValue = areaDataWithPublishedDeaths.cumulativeDeathsByPublishedDate!!,
                    rate = areaDataWithPublishedDeaths.cumulativeDeathsByPublishedDateRate!!
                )
            )
        )
    }

    @Test
    fun `GIVEN area does not have ons deaths WHEN onsDeathsByRegistrationDate called THEN empty list emitted`() {
        every { areaDataDao.allAreaDeathsByAreaCode("") } returns listOf(areaData)

        val onsDeathsByRegistrationDate = sut.onsDeathsByRegistrationDate("")

        assertThat(onsDeathsByRegistrationDate).isEmpty()
    }

    @Test
    fun `GIVEN area does has ons deaths WHEN onsDeathsByRegistrationDate called THEN deaths are emitted`() {
        every { areaDataDao.allAreaDeathsByAreaCode("") } returns listOf(areaDataWithOnsDeaths)

        val onsDeathsByRegistrationDate = sut.onsDeathsByRegistrationDate("")

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

        private val areaDataWithPublishedDeaths = areaData.copy(
            newDeathsByPublishedDate = 10,
            cumulativeDeathsByPublishedDate = 100,
            cumulativeDeathsByPublishedDateRate = 20.0
        )

        private val areaDataWithOnsDeaths = areaData.copy(
            newOnsDeathsByRegistrationDate = 10,
            cumulativeOnsDeathsByRegistrationDate = 100,
            cumulativeOnsDeathsByRegistrationDateRate = 20.0
        )
    }
}
