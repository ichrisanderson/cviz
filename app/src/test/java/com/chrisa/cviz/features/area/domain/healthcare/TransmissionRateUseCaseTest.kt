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

package com.chrisa.cviz.features.area.domain.healthcare

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.features.area.data.TransmissionRateDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.chrisa.cviz.features.area.data.dtos.MetadataDto
import com.chrisa.cviz.features.area.data.dtos.TransmissionRateDto
import com.chrisa.cviz.features.area.domain.models.AreaTransmissionRateModel
import com.chrisa.cviz.features.area.domain.models.TransmissionRateModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import org.junit.Test

class TransmissionRateUseCaseTest {

    private val transmissionRateDataSource: TransmissionRateDataSource = mockk()
    private val sut = TransmissionRateUseCase(transmissionRateDataSource)

    @Test
    fun `GIVEN no transmission data WHEN transmissionRate called THEN null is returned`() {
        every { transmissionRateDataSource.transmissionRate(area.code) } returns null

        val transmissionRate = sut.transmissionRate(area)

        assertThat(transmissionRate).isNull()
    }

    @Test
    fun `GIVEN transmission data WHEN transmissionRate called THEN transmission rates returned`() {
        every { transmissionRateDataSource.transmissionRate(area.code) } returns transmissionRate
        every { transmissionRateDataSource.healthcareMetaData(area.code) } returns metadata

        val areaTransmissionRate = sut.transmissionRate(area)

        assertThat(areaTransmissionRate).isEqualTo(
            AreaTransmissionRateModel(
                areaName = area.name,
                lastUpdatedDate = metadata.lastUpdatedAt,
                transmissionRate = TransmissionRateModel(
                    date = transmissionRate.date,
                    transmissionRateMin = transmissionRate.transmissionRateMin,
                    transmissionRateMax = transmissionRate.transmissionRateMax,
                    transmissionRateGrowthRateMin = transmissionRate.transmissionRateGrowthRateMin,
                    transmissionRateGrowthRateMax = transmissionRate.transmissionRateGrowthRateMax
                )
            )
        )
    }

    companion object {
        private val area = AreaDto(
            "1234",
            "St Barts",
            AreaType.NHS_REGION
        )
        private val syncDate = LocalDateTime.of(2020, 1, 1, 0, 0)

        private val metadata = MetadataDto(
            syncDate,
            syncDate
        )

        private val transmissionRate = TransmissionRateDto(
            date = syncDate.toLocalDate(),
            transmissionRateMin = 0.8,
            transmissionRateMax = 1.1,
            transmissionRateGrowthRateMin = 0.7,
            transmissionRateGrowthRateMax = 1.2
        )
    }
}
