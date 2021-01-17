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
import com.chrisa.cviz.core.data.db.AreaCaseData
import com.chrisa.cviz.core.data.db.AreaDao
import com.chrisa.cviz.core.data.db.AreaDataDao
import com.chrisa.cviz.core.data.db.AreaEntity
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import org.junit.Before
import org.junit.Test

class AreaCasesDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()
    private val areaDao = mockk<AreaDao>()
    private val areaDataDao = mockk<AreaDataDao>()
    private val sut = AreaCasesDataSource(appDatabase)

    @Before
    fun setup() {
        every { appDatabase.areaDataDao() } returns areaDataDao
        every { appDatabase.areaDao() } returns areaDao
    }

    @Test
    fun `GIVEN area has cases WHEN cases called THEN cases are emitted`() {
        every { areaDataDao.allAreaCasesByAreaCode("") } returns listOf(areaData)

        val cases = sut.cases("")

        assertThat(cases).isEqualTo(
            listOf(
                DailyData(
                    date = areaData.date,
                    newValue = areaData.newCases,
                    cumulativeValue = areaData.cumulativeCases,
                    rate = areaData.infectionRate
                )
            )
        )
    }

    @Test
    fun `WHEN areaName called THEN cases are emitted`() {
        every { areaDao.byAreaCode("") } returns areaEntity

        val areaName = sut.areaName("")

        assertThat(areaName).isEqualTo(Constants.ENGLAND_AREA_NAME)
    }

    companion object {
        private val syncDate = LocalDateTime.of(2020, 1, 1, 0, 0)

        private val areaEntity = AreaEntity(
            Constants.ENGLAND_AREA_CODE,
            Constants.ENGLAND_AREA_NAME,
            AreaType.NATION
        )
        private val areaData = AreaCaseData(
            date = syncDate.toLocalDate(),
            newCases = 10,
            infectionRate = 22.0,
            cumulativeCases = 123
        )
    }
}
