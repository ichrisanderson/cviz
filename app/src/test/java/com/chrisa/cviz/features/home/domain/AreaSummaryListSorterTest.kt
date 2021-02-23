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

package com.chrisa.cviz.features.home.domain

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.features.home.data.dtos.AreaSummaryDto
import com.chrisa.cviz.features.home.domain.models.SortOption
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AreaSummaryListSorterTest {

    private val sut = AreaSummaryListSorter()

    @Test
    fun `WHEN sort with RisingCases THEN list is ordered by rising cases`() {
        val items = listOf(
            areaSummary.copy(changeInCases = 1),
            areaSummary.copy(changeInCases = 100),
            areaSummary.copy(changeInCases = 50)
        )

        val sortedItems = sut.sort(items, SortOption.RisingCases)

        assertThat(sortedItems).isEqualTo(
            listOf(
                areaSummary.copy(changeInCases = 100),
                areaSummary.copy(changeInCases = 50),
                areaSummary.copy(changeInCases = 1)
            )
        )
    }

    @Test
    fun `WHEN sort with NewCases THEN list is ordered by current new cases`() {
        val items = listOf(
            areaSummary.copy(currentNewCases = 1),
            areaSummary.copy(currentNewCases = 100),
            areaSummary.copy(currentNewCases = 50)
        )

        val sortedItems = sut.sort(items, SortOption.NewCases)

        assertThat(sortedItems).isEqualTo(
            listOf(
                areaSummary.copy(currentNewCases = 100),
                areaSummary.copy(currentNewCases = 50),
                areaSummary.copy(currentNewCases = 1)
            )
        )
    }

    @Test
    fun `WHEN sort with RisingInfectionRate THEN list is ordered by change in infection rate`() {
        val items = listOf(
            areaSummary.copy(changeInInfectionRate = 1.0),
            areaSummary.copy(changeInInfectionRate = 100.0),
            areaSummary.copy(changeInInfectionRate = 50.0)
        )

        val sortedItems = sut.sort(items, SortOption.RisingInfectionRate)

        assertThat(sortedItems).isEqualTo(
            listOf(
                areaSummary.copy(changeInInfectionRate = 100.0),
                areaSummary.copy(changeInInfectionRate = 50.0),
                areaSummary.copy(changeInInfectionRate = 1.0)
            )
        )
    }

    @Test
    fun `WHEN sort with RisingInfectionRate THEN list is ordered by current infection rate`() {
        val items = listOf(
            areaSummary.copy(currentInfectionRate = 1.0),
            areaSummary.copy(currentInfectionRate = 100.0),
            areaSummary.copy(currentInfectionRate = 50.0)
        )

        val sortedItems = sut.sort(items, SortOption.InfectionRate)

        assertThat(sortedItems).isEqualTo(
            listOf(
                areaSummary.copy(currentInfectionRate = 100.0),
                areaSummary.copy(currentInfectionRate = 50.0),
                areaSummary.copy(currentInfectionRate = 1.0)
            )
        )
    }

    companion object {
        val areaSummary = AreaSummaryDto(
            areaCode = "E1",
            areaName = Constants.ENGLAND_AREA_NAME,
            areaType = AreaType.OVERVIEW.value,
            changeInCases = 0,
            currentNewCases = 0,
            changeInInfectionRate = 0.0,
            currentInfectionRate = 0.0
        )
    }
}
