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

package com.chrisa.covid19.features.home.domain

import com.chrisa.covid19.features.home.domain.models.WeeklyCaseBreakdownModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WeeklyCaseDifferenceHelperTest {

    private val sut = WeeklyCaseDifferenceHelper()

    @Test
    fun `WHEN weeklyCaseDifference THEN rate and weekly lab case differences are calculated`() {

        val weekOne = WeeklyCaseBreakdownModel(
            100,
            100,
            10.0
        )
        val weekTwo = WeeklyCaseBreakdownModel(
            220,
            320,
            22.0
        )

        val difference = sut.weeklyCaseDifference(weekOne, weekTwo)

        assertThat(difference.changeInTotalLabConfirmedCasesRate).isEqualTo(12.0)
        assertThat(difference.changeInWeeklyLabConfirmedCases).isEqualTo(120)
    }
}
