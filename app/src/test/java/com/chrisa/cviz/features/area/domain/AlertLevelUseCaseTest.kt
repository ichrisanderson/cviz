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

package com.chrisa.cviz.features.area.domain

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.features.area.domain.models.AlertLevelModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AlertLevelUseCaseTest {

    private val sut = AlertLevelUseCase()

    @Test
    fun `GIVEN alert level does not exists WHEN alertLevel called with english tier area THEN alert level data returned`() {
        val areaTypes = listOf(AreaType.LTLA, AreaType.UTLA)

        areaTypes.forEach { areaType ->
            val data = sut.alertLevel("E", areaType)

            assertThat(data).isEqualTo(
                AlertLevelModel(
                    alertLevelUrl = AlertLevelUseCase.ENGLAND_RESTRICTIONS_URL
                )
            )
        }
    }

    @Test
    fun `GIVEN alert level does not exists WHEN alertLevel called with scottish tier area THEN alert level data returned`() {
        val areaTypes = listOf(AreaType.LTLA, AreaType.UTLA)

        areaTypes.forEach { areaType ->
            val data = sut.alertLevel("S", areaType)

            assertThat(data).isEqualTo(
                AlertLevelModel(
                    alertLevelUrl = AlertLevelUseCase.SCOTLAND_RESTRICTIONS_URL
                )
            )
        }
    }

    @Test
    fun `GIVEN alert level does not exists WHEN alertLevel called with welsh tier area THEN alert level data returned`() {
        val areaTypes = listOf(AreaType.LTLA, AreaType.UTLA)

        areaTypes.forEach { areaType ->
            val data = sut.alertLevel("W", areaType)

            assertThat(data).isEqualTo(
                AlertLevelModel(
                    alertLevelUrl = AlertLevelUseCase.WALES_RESTRICTIONS_URL
                )
            )
        }
    }

    @Test
    fun `GIVEN alert level does not exists WHEN alertLevel called with ni tier area THEN alert level data returned`() {
        val areaTypes = listOf(AreaType.LTLA, AreaType.UTLA)

        areaTypes.forEach { areaType ->
            val data = sut.alertLevel("N", areaType)

            assertThat(data).isEqualTo(
                AlertLevelModel(
                    alertLevelUrl = AlertLevelUseCase.NORTHERN_IRELAND_RESTRICTIONS_URL
                )
            )
        }
    }

    @Test
    fun `GIVEN alert level does not exists WHEN alertLevel called with unsupported tier area THEN alert level data is null`() {
        val areaTypes = listOf(AreaType.LTLA, AreaType.UTLA)

        areaTypes.forEach { areaType ->
            val data = sut.alertLevel("K", areaType)

            assertThat(data).isNull()
        }
    }

    @Test
    fun `GIVEN alert level does not exists WHEN alertLevel called with non tier area THEN alert level data is null`() {
        val displayedAlertLevelAreaTypes = setOf(AreaType.LTLA, AreaType.UTLA)
        val areaTypes = AreaType.values().filter { !displayedAlertLevelAreaTypes.contains(it) }
        val areaCodes = listOf("E", "S", "W", "N")
        areaCodes.forEach { areaCode ->

            areaTypes.forEach { areaType ->
                val data = sut.alertLevel(areaCode, areaType)

                assertThat(data).isNull()
            }
        }
    }
}
