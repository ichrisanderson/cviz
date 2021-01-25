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
import com.chrisa.cviz.core.data.synchronisation.AlertLevelSynchroniser
import com.chrisa.cviz.features.area.data.AlertLevelDataSource
import com.chrisa.cviz.features.area.data.dtos.AlertLevelDto
import com.chrisa.cviz.features.area.data.dtos.MetadataDto
import com.chrisa.cviz.features.area.domain.models.AlertLevelModel
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.junit.Test

class AlertLevelUseCaseTest {

    private val alertLevelDataSource = mockk<AlertLevelDataSource>()
    private val alertLevelSynchroniser = mockk<AlertLevelSynchroniser>()
    private val sut = AlertLevelUseCase(alertLevelDataSource, alertLevelSynchroniser)

    @Test
    fun `WHEN alertLevel called THEN alert level data returned`() {
        every { alertLevelDataSource.metadata("") } returns metadata
        coEvery { alertLevelDataSource.alertLevel("") } returns alertLevel

        val data = sut.alertLevel("")

        assertThat(data).isEqualTo(
            AlertLevelModel(
                areaName = alertLevel.areaName,
                date = alertLevel.date,
                lastUpdatedAt = metadata.lastUpdatedAt,
                alertLevelName = alertLevel.alertLevelName,
                alertLevelUrl = alertLevel.alertLevelUrl
            )
        )
    }

    @Test
    fun `GIVEN alert level area WHEN syncAlertLevel called THEN alert level synced`() =
        runBlocking {
            val areaTypes = listOf(AreaType.UTLA, AreaType.LTLA)
            coEvery { alertLevelSynchroniser.performSync(any(), any()) } just Runs
            areaTypes.forEach { areaType ->
                val areaCode = "${areaType}_1"
                sut.syncAlertLevel(areaCode, areaType)

                coVerify(exactly = 1) {
                    alertLevelSynchroniser.performSync(
                        areaCode,
                        AreaType.LTLA
                    )
                }
            }
        }

    @Test
    fun `GIVEN non-alert level area WHEN execute called THEN alert level synced`() =
        runBlocking {
            val alertLevelAreaTypes = setOf(AreaType.UTLA, AreaType.LTLA)
            val areaTypes = AreaType.values().filter { !alertLevelAreaTypes.contains(it) }
            coEvery { alertLevelSynchroniser.performSync(any(), any()) } just Runs
            areaTypes.forEach { areaType ->
                val areaCode = "${areaType}_1"
                sut.syncAlertLevel(areaCode, areaType)

                coVerify(exactly = 0) {
                    alertLevelSynchroniser.performSync(
                        areaCode,
                        AreaType.LTLA
                    )
                }
            }
        }

    companion object {
        private val syncDate = LocalDateTime.of(2020, 1, 1, 0, 0)
        private val metadata = MetadataDto(
            syncDate,
            syncDate
        )
        val alertLevel = AlertLevelDto(
            areaCode = "",
            areaName = "",
            areaType = AreaType.LTLA,
            date = syncDate.toLocalDate(),
            alertLevel = 1,
            alertLevelName = "",
            alertLevelUrl = "",
            alertLevelValue = 1
        )
    }
}
