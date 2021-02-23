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

package com.chrisa.cviz.features.area.data

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.db.HealthcareDao
import com.chrisa.cviz.core.data.db.HealthcareEntity
import com.chrisa.cviz.core.data.db.MetaDataIds
import com.chrisa.cviz.core.data.db.MetadataDao
import com.chrisa.cviz.core.data.db.MetadataEntity
import com.chrisa.cviz.features.area.data.dtos.MetadataDto
import com.chrisa.cviz.features.area.data.dtos.TransmissionRateDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import org.junit.Before
import org.junit.Test

class TransmissionRateDataSourceTest {

    private val appDatabase: AppDatabase = mockk()
    private val healthcareDao: HealthcareDao = mockk()
    private val metadataDao: MetadataDao = mockk()
    private val sut = TransmissionRateDataSource(appDatabase)

    @Before
    fun setup() {
        every { appDatabase.metadataDao() } returns metadataDao
        every { appDatabase.healthcareDao() } returns healthcareDao
    }

    @Test
    fun `WHEN healthcareMetaData called THEN metadata dao data returned`() {
        every { metadataDao.metadata(MetaDataIds.healthcareId("areaCode")) } returns metadata

        val healthcareMetaData = sut.healthcareMetaData("areaCode")

        assertThat(healthcareMetaData).isEqualTo(
            MetadataDto(
                metadata.lastUpdatedAt,
                metadata.lastSyncTime
            )
        )
    }

    @Test
    fun `GIVEN no healthcare data WHEN transmissionRate called THEN transmission rate is null`() {
        val areaCode = "E1"
        every { healthcareDao.byAreaCode(areaCode) } returns emptyList()

        val transmissionRate = sut.transmissionRate(areaCode)

        assertThat(transmissionRate).isNull()
    }

    @Test
    fun `WHEN transmissionRate called THEN metadata dto returned`() {
        val areaCode = "E1"
        every { healthcareDao.byAreaCode(areaCode) } returns listOf(areaData)

        val transmissionRate = sut.transmissionRate(areaCode)

        assertThat(transmissionRate).isEqualTo(
            TransmissionRateDto(
                areaData.date,
                areaData.transmissionRateMin!!,
                areaData.transmissionRateMax!!,
                areaData.transmissionRateGrowthRateMin!!,
                areaData.transmissionRateGrowthRateMax!!
            )
        )
    }

    companion object {
        private val syncDate = LocalDateTime.of(2020, 1, 1, 0, 0)

        private val metadata = MetadataEntity(
            "",
            syncDate,
            syncDate
        )

        private val areaData = HealthcareEntity(
            date = syncDate.toLocalDate(),
            areaCode = Constants.ENGLAND_AREA_CODE,
            newAdmissions = 10,
            cumulativeAdmissions = 100,
            occupiedBeds = 70,
            transmissionRateMin = 0.8,
            transmissionRateMax = 1.1,
            transmissionRateGrowthRateMin = 0.7,
            transmissionRateGrowthRateMax = 1.2
        )
    }
}
