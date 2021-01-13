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
import com.chrisa.cviz.core.data.synchronisation.SynchronisationTestData
import com.chrisa.cviz.features.area.data.dtos.AreaDailyDataDto
import com.google.common.truth.Truth.assertThat
import java.time.LocalDate
import org.junit.Test

class AdmissionsFilterTest {

    private val admissionsFilter = AdmissionsFilter()

    @Test
    fun `GIVEN filter set is empty WHEN filterHospitalData called THEN all data returned`() {
        val filteredData = admissionsFilter.filterHospitalData(hospitalAdmissions, emptySet())

        assertThat(filteredData).isEqualTo(
            listOf(
                DailyData(
                    newValue = 3,
                    cumulativeValue = 3,
                    date = LocalDate.ofEpochDay(1L),
                    rate = 0.0
                ),
                DailyData(
                    newValue = 6,
                    cumulativeValue = 9,
                    date = LocalDate.ofEpochDay(2L),
                    rate = 0.0
                ),
                DailyData(
                    newValue = 9,
                    cumulativeValue = 18,
                    date = LocalDate.ofEpochDay(3L),
                    rate = 0.0
                )
            )
        )
    }

    @Test
    fun `GIVEN all values in filtered empty WHEN filterHospitalData called THEN all data returned`() {
        val filter = hospitalAdmissions.map { it.name }.toSet()
        val filteredData = admissionsFilter.filterHospitalData(hospitalAdmissions, filter)

        assertThat(filteredData).isEqualTo(
            listOf(
                DailyData(
                    newValue = 3,
                    cumulativeValue = 3,
                    date = LocalDate.ofEpochDay(1L),
                    rate = 0.0
                ),
                DailyData(
                    newValue = 6,
                    cumulativeValue = 9,
                    date = LocalDate.ofEpochDay(2L),
                    rate = 0.0
                ),
                DailyData(
                    newValue = 9,
                    cumulativeValue = 18,
                    date = LocalDate.ofEpochDay(3L),
                    rate = 0.0
                )
            )
        )
    }

    @Test
    fun `GIVEN selected values filtered empty WHEN filterHospitalData called THEN selected data returned`() {
        val filter =
            hospitalAdmissions
                .filterIndexed { index, _ -> index == 1 }
                .map { it.name }.toSet()

        val filteredData = admissionsFilter.filterHospitalData(hospitalAdmissions, filter)

        assertThat(filteredData).isEqualTo(
            listOf(
                DailyData(
                    newValue = 1,
                    cumulativeValue = 1,
                    date = LocalDate.ofEpochDay(1L),
                    rate = 0.0
                ),
                DailyData(
                    newValue = 2,
                    cumulativeValue = 3,
                    date = LocalDate.ofEpochDay(2L),
                    rate = 0.0
                ),
                DailyData(
                    newValue = 3,
                    cumulativeValue = 6,
                    date = LocalDate.ofEpochDay(3L),
                    rate = 0.0
                )
            )
        )
    }

    @Test
    fun `GIVEN all values filtered out WHEN filterHospitalData called THEN no data returned`() {
        val filteredData = admissionsFilter.filterHospitalData(hospitalAdmissions, setOf("*"))

        assertThat(filteredData).isEmpty()
    }

    companion object {

        val admissions = SynchronisationTestData.dailyData(1, 3)
        val hospitalAdmissions = listOf(
            AreaDailyDataDto("T1", admissions),
            AreaDailyDataDto("T2", admissions),
            AreaDailyDataDto("T3", admissions)
        )
    }
}
