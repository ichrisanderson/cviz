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

package com.chrisa.cron19.core.data.synchronisation

import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.junit.Test

class DailyDataWithRollingAverageBuilderTest {

    private val rollingAverageHelper: RollingAverageHelper = mockk()
    private val sut = DailyDataWithRollingAverageBuilder(rollingAverageHelper)

    @Test
    fun foo() {
        every { rollingAverageHelper.average(any(), any()) } returns 1.0

        val data = sut.buildDailyDataWithRollingAverage(
            listOf(
                DailyData(
                    newValue = 1,
                    cumulativeValue = 100,
                    rate = 100.0,
                    date = syncDate
                ),
                DailyData(
                    newValue = 1,
                    cumulativeValue = 101,
                    rate = 100.0,
                    date = syncDate.plusDays(1)
                )
            )
        )

        assertThat(data).isEqualTo(
            listOf(
                DailyDataWithRollingAverage(
                    newValue = 1,
                    cumulativeValue = 100,
                    rollingAverage = 1.0,
                    rate = 100.0,
                    date = syncDate
                ),
                DailyDataWithRollingAverage(
                    newValue = 1,
                    cumulativeValue = 101,
                    rollingAverage = 1.0,
                    rate = 100.0,
                    date = syncDate.plusDays(1)
                )
            )
        )
    }

    companion object {
        private val syncDate = LocalDate.of(2020, 1, 1)
    }
}
