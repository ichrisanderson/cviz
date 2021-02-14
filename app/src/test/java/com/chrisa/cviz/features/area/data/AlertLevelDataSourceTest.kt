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

import com.chrisa.cviz.core.data.db.AlertLevelDao
import com.chrisa.cviz.core.data.db.AlertLevelEntity
import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.features.area.data.dtos.AlertLevelDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.junit.Before
import org.junit.Test

class AlertLevelDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()
    private val alertLevelDao = mockk<AlertLevelDao>()
    private val sut = AlertLevelDataSource(appDatabase)

    @Before
    fun setup() {
        every { appDatabase.alertLevelDao() } returns alertLevelDao
    }

    @Test
    fun `WHEN alertLevel called THEN alert level by area code returned`() {
        val alertLevelEntity = AlertLevelEntity(
            areaCode = "1234",
            areaName = "London",
            areaType = AreaType.REGION,
            date = LocalDate.of(2020, 1, 1),
            alertLevel = 2,
            alertLevelName = "Stay Alert",
            alertLevelUrl = "http://acme.com",
            alertLevelValue = 2
        )
        every { alertLevelDao.byAreaCode("") } returns alertLevelEntity

        val alertLevel = sut.alertLevel("")

        assertThat(alertLevel).isEqualTo(
            AlertLevelDto(
                alertLevelEntity.areaCode,
                alertLevelEntity.areaName,
                alertLevelEntity.areaType,
                alertLevelEntity.date,
                alertLevelEntity.alertLevel,
                alertLevelEntity.alertLevelName,
                alertLevelEntity.alertLevelUrl,
                alertLevelEntity.alertLevelValue
            )
        )
    }
}
