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

package com.chrisa.cviz.features.area.presentation.mappers

import com.chrisa.cviz.core.data.synchronisation.DailyData
import com.chrisa.cviz.features.area.domain.models.SoaData
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class SoaDailyDataMapperTest {

    private val sut = SoaDailyDataMapper()

    @Test
    fun `WHEN mapToDailyData called THEN daily data returned in order`() {
        val soaData = listOf(
            SoaData(
                date = LocalDate.of(2020, 2, 1),
                rollingSum = 11,
                rollingRate = 33.0
            ),
            SoaData(
                date = LocalDate.of(2020, 1, 1),
                rollingSum = 11,
                rollingRate = 12.0
            )
        )

        val dailyData = sut.mapToDailyData(soaData)

        assertThat(dailyData).isEqualTo(
            listOf(
                DailyData(
                    newValue = soaData[1].rollingSum,
                    cumulativeValue = soaData[1].rollingSum,
                    soaData[1].rollingRate,
                    soaData[1].date
                ),
                DailyData(
                    newValue = soaData[0].rollingSum,
                    cumulativeValue = soaData[0].rollingSum + soaData[1].rollingSum,
                    soaData[0].rollingRate,
                    soaData[0].date
                )
            )
        )
    }
}
