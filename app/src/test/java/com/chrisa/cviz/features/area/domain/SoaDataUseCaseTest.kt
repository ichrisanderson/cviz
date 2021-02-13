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
import com.chrisa.cviz.core.data.synchronisation.SoaDataSynchroniser
import com.chrisa.cviz.features.area.data.SoaDataSource
import com.chrisa.cviz.features.area.data.dtos.SoaDataDto
import com.chrisa.cviz.features.area.domain.models.SoaData
import com.chrisa.cviz.features.area.domain.models.SoaDataModel
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.io.IOException
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.Test
import timber.log.Timber

class SoaDataUseCaseTest {

    private val soaDataSource = mockk<SoaDataSource>()
    private val soaDataSynchroniser = mockk<SoaDataSynchroniser>()
    private val sut = SoaDataUseCase(soaDataSource, soaDataSynchroniser)

    @Test
    fun `GIVEN msoa area WHEN syncSoaData called THEN soa data synced`() =
        runBlocking {
            coEvery { soaDataSynchroniser.performSync("1") } just Runs

            sut.syncSoaData("1", AreaType.MSOA)

            coVerify(exactly = 1) { soaDataSynchroniser.performSync("1") }
        }

    @Test
    fun `GIVEN sync fails WHEN syncSoaData called THEN exception is suppressed`() =
        runBlocking {
            mockkStatic(Timber::class)
            val error = IOException()
            coEvery { soaDataSynchroniser.performSync("1") } throws error

            sut.syncSoaData("1", AreaType.MSOA)

            coVerify(exactly = 1) { soaDataSynchroniser.performSync("1") }
            verify { Timber.e(error) }
        }

    @Test
    fun `GIVEN non-msoa area WHEN syncSoaData called THEN soa data is not synced`() =
        runBlocking {
            val msoaAreas = setOf(AreaType.MSOA)
            val areaTypes = AreaType.values().filterNot { msoaAreas.contains(it) }
            areaTypes.forEach { areaType ->
                val areaCode = "$areaType"

                sut.syncSoaData(areaCode, areaType)

                coVerify(exactly = 0) { soaDataSynchroniser.performSync(areaCode) }
            }
        }

    @Test
    fun `GIVEN non-msoa area WHEN byAreaCode called THEN no soa data returned`() {
        val msoaAreas = setOf(AreaType.MSOA)
        val areaTypes = AreaType.values().filterNot { msoaAreas.contains(it) }
        areaTypes.forEach { areaType ->
            val areaCode = "$areaType"

            val soaData = sut.byAreaCode(areaCode, areaType)

            assertThat(soaData).isNull()
        }
    }

    @Test
    fun `GIVEN msoa area WHEN byAreaCode called THEN soa data for area returned`() {
        val msoaAreas = setOf(AreaType.MSOA)
        val areaTypes = AreaType.values().filter { msoaAreas.contains(it) }
        areaTypes.forEach { areaType ->
            val areaCode = "$areaType"
            every { soaDataSource.byAreaCode(areaCode) } returns listOf(soaDataDto)

            val soaData = sut.byAreaCode(areaCode, areaType)

            assertThat(soaData).isEqualTo(
                SoaDataModel(
                    soaDataDto.areaCode,
                    soaDataDto.areaName,
                    soaDataDto.areaType,
                    listOf(
                        SoaData(
                            soaDataDto.date,
                            soaDataDto.rollingSum,
                            soaDataDto.rollingRate
                        )
                    )
                )
            )
        }
    }

    companion object {
        val soaDataDto = SoaDataDto(
            areaCode = "1234",
            areaName = "London",
            areaType = AreaType.REGION,
            date = LocalDate.of(2020, 1, 1),
            rollingSum = 11,
            rollingRate = 12.0,
            change = 12,
            changePercentage = 12.0
        )
    }
}
