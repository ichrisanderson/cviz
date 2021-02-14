/*
 * Copyright 2021 Chris Anderson.
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
import com.chrisa.cviz.core.data.db.SoaDataDao
import com.chrisa.cviz.core.data.db.SoaDataEntity
import com.chrisa.cviz.features.area.data.dtos.SoaDataDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.junit.Before
import org.junit.Test

class SoaDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()
    private val soaDataDao = mockk<SoaDataDao>()
    private val sut = SoaDataSource(appDatabase)

    @Before
    fun setup() {
        every { appDatabase.soaDataDao() } returns soaDataDao
    }

    @Test
    fun `WHEN byAreaCode THEN soa data by area code returned`() {
        val soaDataEntity = SoaDataEntity(
            areaCode = "1234",
            areaName = "London",
            areaType = AreaType.REGION,
            date = LocalDate.of(2020, 1, 1),
            rollingSum = 11,
            rollingRate = 12.0,
            change = 12,
            changePercentage = 12.0
        )
        every { soaDataDao.byAreaCode("") } returns listOf(soaDataEntity)

        val soaData = sut.byAreaCode("")

        assertThat(soaData).isEqualTo(
            listOf(
                SoaDataDto(
                    soaDataEntity.areaCode,
                    soaDataEntity.areaName,
                    soaDataEntity.areaType,
                    soaDataEntity.date,
                    soaDataEntity.rollingSum,
                    soaDataEntity.rollingRate,
                    soaDataEntity.change,
                    soaDataEntity.changePercentage
                )
            )
        )
    }
}
