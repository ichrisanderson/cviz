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
import com.chrisa.cviz.core.data.db.HealthcareWithArea
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import org.junit.Before
import org.junit.Test

class AdmissionsDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()
    private val healthcareDao = mockk<HealthcareDao>()
    private val sut = AdmissionsDataSource(appDatabase)

    @Before
    fun setup() {
        every { appDatabase.healthcareDao() } returns healthcareDao
    }

    @Test
    fun `GIVEN area does not have admissions WHEN healthcareData called THEN empty list emitted`() {
        every { healthcareDao.withAreaByAreaCodes(listOf("")) } returns emptyList()

        val admissionsForArea = sut.admissionsForArea("")

        assertThat(admissionsForArea).isEmpty()
    }

    @Test
    fun `GIVEN area has admissions WHEN healthcareData called THEN  data emitted`() {
        every { healthcareDao.withAreaByAreaCodes(listOf("")) } returns listOf(areaData)

        val admissionsForArea = sut.admissionsForArea("")

        assertThat(admissionsForArea).isEqualTo(
            listOf(
                DailyData(
                    date = syncDate.toLocalDate(),
                    newValue = areaData.healthcare.newAdmissions!!,
                    cumulativeValue = areaData.healthcare.cumulativeAdmissions!!,
                    rate = 0.0
                )
            )
        )
    }

    @Test
    fun `GIVEN areas have admissions WHEN healthcareDataFoAreaCodes called THEN admission data emitted`() {
        val areaCodes = listOf("1", "2", "3")
        every { healthcareDao.withAreaByAreaCodes(areaCodes) } returns listOf(areaData)

        val admissionsForArea = sut.admissionsForAreaCodes(areaCodes)

        assertThat(admissionsForArea).isEqualTo(
            listOf(
                AreaDailyDataDto(
                    areaData.areaName,
                    listOf(
                        DailyData(
                            date = syncDate.toLocalDate(),
                            newValue = areaData.healthcare.newAdmissions!!,
                            cumulativeValue = areaData.healthcare.cumulativeAdmissions!!,
                            rate = 0.0
                        )
                    )
                )
            )
        )
    }

    companion object {
        private val syncDate = LocalDateTime.of(2020, 1, 1, 0, 0)

        private val areaData = HealthcareWithArea(
            areaName = Constants.ENGLAND_AREA_NAME,
            areaType = AreaType.NATION,
            healthcare = HealthcareEntity(
                areaCode = Constants.ENGLAND_AREA_CODE,
                date = syncDate.toLocalDate(),
                newAdmissions = 10,
                cumulativeAdmissions = 100,
                occupiedBeds = 70,
                transmissionRateMin = 0.8,
                transmissionRateMax = 1.1,
                transmissionRateGrowthRateMin = 0.7,
                transmissionRateGrowthRateMax = 1.2
            )
        )
    }
}
