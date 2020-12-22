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
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.db.HealthcareDao
import com.chrisa.cviz.core.data.db.HealthcareEntity
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import org.junit.Before
import org.junit.Test

class HealthcareDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()
    private val healthcareDao = mockk<HealthcareDao>()
    private val sut = HealthcareDataSource(appDatabase)

    @Before
    fun setup() {
        every { appDatabase.healthcareDao() } returns healthcareDao
    }

    @Test
    fun `GIVEN area does not have healthcare deaths WHEN healthcareData called THEN empty list emitted`() {
        every { healthcareDao.byAreaCode("") } returns emptyList()

        val healthcareData = sut.healthcareData("")

        assertThat(healthcareData).isEmpty()
    }

    @Test
    fun `GIVEN area has healthcare deaths WHEN healthcareData called THEN healthcare data emitted`() {
        every { healthcareDao.byAreaCode("") } returns listOf(areaData)

        val healthcareData = sut.healthcareData("")

        assertThat(healthcareData).isEqualTo(
            listOf(
                DailyData(
                    date = syncDate.toLocalDate(),
                    newValue = areaData.newAdmissions!!,
                    cumulativeValue = areaData.cumulativeAdmissions!!,
                    rate = 0.0
                )
            )
        )
    }

    companion object {
        private val syncDate = LocalDateTime.of(2020, 1, 1, 0, 0)

        private val areaData = HealthcareEntity(
            date = syncDate.toLocalDate(),
            areaCode = Constants.ENGLAND_AREA_CODE,
            areaName = "England",
            areaType = AreaType.NATION,
            newAdmissions = 10,
            cumulativeAdmissions = 100,
            occupiedBeds = 70,
            transmissionRateMin = 0.8,
            transmissionRateMax = 1.1,
            transmissionRateGrowthRateMin = 0.7,
            transmissionRateGrowthRateMax = 1.2
        )
    }
}
