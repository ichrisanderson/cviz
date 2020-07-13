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

package com.chrisa.covid19.core.data.mappers

import com.chrisa.covid19.core.data.db.DeathEntity
import com.chrisa.covid19.core.data.network.DeathModel
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class DeathModelMapperTest {

    val sut = DeathModelMapper()

    @Test
    fun `GIVEN no null fields WHEN mapToDeathsEntity called THEN deathsEntity returned`() {

        val deathModel = DeathModel(
            areaCode = "001",
            areaName = "UK",
            reportingDate = LocalDate.ofEpochDay(0),
            cumulativeDeaths = 110,
            dailyChangeInDeaths = 222
        )

        val entity = sut.mapToDeathsEntity(deathModel)

        assertThat(entity).isEqualTo(
            DeathEntity(
                areaCode = deathModel.areaCode,
                areaName = deathModel.areaName,
                date = deathModel.reportingDate,
                cumulativeDeaths = deathModel.cumulativeDeaths,
                dailyChangeInDeaths = deathModel.dailyChangeInDeaths!!
            )
        )
    }

    @Test
    fun `GIVEN dailyChangeInDeaths is null WHEN mapToDeathsEntity called THEN deathsEntity returned with 0 dailyChangeInDeaths`() {

        val deathModel = DeathModel(
            areaCode = "001",
            areaName = "UK",
            reportingDate = LocalDate.ofEpochDay(0),
            cumulativeDeaths = 110,
            dailyChangeInDeaths = null
        )

        val entity = sut.mapToDeathsEntity(deathModel)

        assertThat(entity).isEqualTo(
            DeathEntity(
                areaCode = deathModel.areaCode,
                areaName = deathModel.areaName,
                date = deathModel.reportingDate,
                cumulativeDeaths = deathModel.cumulativeDeaths,
                dailyChangeInDeaths = 0
            )
        )
    }
}
