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

package com.chrisa.cviz.features.area.domain

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.core.data.synchronisation.AreaLookupDataSynchroniser
import com.chrisa.cviz.features.area.data.AreaLookupDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class AreaLookupUseCaseTest {

    private val areaLookupDataSynchroniser = mockk<AreaLookupDataSynchroniser>()
    private val areaLookupDataSource = mockk<AreaLookupDataSource>()
    private val testDispatcher = TestCoroutineDispatcher()
    private val sut = AreaLookupUseCase(areaLookupDataSynchroniser, areaLookupDataSource)

    @Test
    fun `WHEN areaLookup called with LTLA THEN ltla lookup returned`() {
        val areaCode = "E1"
        val lookup = lookupEntity.copy(ltlaCode = areaCode)
        every { areaLookupDataSource.areaLookupByLtla(areaCode) } returns lookup

        val areaLookupDto = sut.areaLookup(areaCode, AreaType.LTLA)

        assertThat(areaLookupDto).isEqualTo(lookup)
    }

    @Test
    fun `WHEN areaLookup called with UTLA THEN utla lookup returned`() {
        val areaCode = "E1"
        val lookup = lookupEntity.copy(utlaCode = areaCode)
        every { areaLookupDataSource.areaLookupByUtla(areaCode) } returns lookup

        val areaLookupDto = sut.areaLookup(areaCode, AreaType.UTLA)

        assertThat(areaLookupDto).isEqualTo(lookup)
    }

    @Test
    fun `WHEN areaLookup called with REGION THEN region lookup returned`() {
        val areaCode = "E1"
        val lookup = lookupEntity.copy(regionCode = areaCode)
        every { areaLookupDataSource.areaLookupByRegion(areaCode) } returns lookup

        val areaLookupDto = sut.areaLookup(areaCode, AreaType.REGION)

        assertThat(areaLookupDto).isEqualTo(lookup)
    }

    @Test
    fun `WHEN areaLookup called with NHS_REGION THEN nhs region lookup returned`() {
        val areaCode = "E1"
        val lookup = lookupEntity.copy(nhsRegionCode = areaCode)
        every { areaLookupDataSource.areaLookupByNhsRegion(areaCode) } returns lookup

        val areaLookupDto = sut.areaLookup(areaCode, AreaType.NHS_REGION)

        assertThat(areaLookupDto).isEqualTo(lookup)
    }

    @Test
    fun `WHEN areaLookup called with NHS_TRUST THEN nhs trust lookup returned`() {
        val areaCode = "E1"
        val lookup = lookupEntity.copy(nhsRegionCode = areaCode)
        every { areaLookupDataSource.areaLookupByNhsTrust(areaCode) } returns lookup

        val areaLookupDto = sut.areaLookup(areaCode, AreaType.NHS_TRUST)

        assertThat(areaLookupDto).isEqualTo(lookup)
    }

    @Test
    fun `WHEN areaLookup called with NATION THEN area lookup is null`() {
        val areaCode = "E1"
        val lookup = lookupEntity.copy(nhsRegionCode = areaCode)
        every { areaLookupDataSource.areaLookupByNhsTrust(areaCode) } returns lookup

        val areaLookupDto = sut.areaLookup(areaCode, AreaType.NATION)

        assertThat(areaLookupDto).isEqualTo(null)
    }

    @Test
    fun `WHEN areaLookup called with OVERVIEW THEN area lookup is null`() {
        val areaCode = "E1"
        val lookup = lookupEntity.copy(nhsRegionCode = areaCode)
        every { areaLookupDataSource.areaLookupByNhsTrust(areaCode) } returns lookup

        val areaLookupDto = sut.areaLookup(areaCode, AreaType.OVERVIEW)

        assertThat(areaLookupDto).isEqualTo(null)
    }

    @Test
    fun `WHEN syncAreaLookup THEN data synchroniser is called`() =
        testDispatcher.runBlockingTest {
            val areaCode = "E1"
            val areaType = AreaType.OVERVIEW
            coEvery { areaLookupDataSynchroniser.performSync(areaCode, areaType) } just Runs

            sut.syncAreaLookup(areaCode, areaType)

            coVerify(exactly = 1) { areaLookupDataSynchroniser.performSync(areaCode, areaType) }
        }

    @Test
    fun `WHEN areaName called with LTLA THEN ltla name is returned`() {
        val areaName = "Mythos"
        val lookup = lookupEntity.copy(ltlaName = areaName)

        val lookupAreaName = sut.areaName(AreaType.LTLA, lookup)

        assertThat(lookupAreaName).isEqualTo(areaName)
    }

    @Test
    fun `WHEN areaName called with UTLA THEN utla name is returned`() {
        val areaName = "Mythos"
        val lookup = lookupEntity.copy(utlaName = areaName)

        val lookupAreaName = sut.areaName(AreaType.UTLA, lookup)

        assertThat(lookupAreaName).isEqualTo(areaName)
    }

    @Test
    fun `WHEN areaName called with REGION THEN region name is returned`() {
        val areaName = "Mythos"
        val lookup = lookupEntity.copy(regionName = areaName)

        val lookupAreaName = sut.areaName(AreaType.REGION, lookup)

        assertThat(lookupAreaName).isEqualTo(areaName)
    }

    @Test
    fun `WHEN areaName called with NHS_REGION THEN nhs region name is returned`() {
        val areaName = "Mythos"
        val lookup = lookupEntity.copy(nhsRegionName = areaName)

        val lookupAreaName = sut.areaName(AreaType.NHS_REGION, lookup)

        assertThat(lookupAreaName).isEqualTo(areaName)
    }

    @Test
    fun `WHEN areaName called with NHS_TRUST THEN nhs trust name is returned`() {
        val areaName = "Mythos"
        val lookup = lookupEntity.copy(nhsTrustName = areaName)

        val lookupAreaName = sut.areaName(AreaType.NHS_TRUST, lookup)

        assertThat(lookupAreaName).isEqualTo(areaName)
    }

    @Test
    fun `WHEN areaName called with NATION THEN nation name is returned`() {
        val areaName = "Mythos"
        val lookup = lookupEntity.copy(nationName = areaName)

        val lookupAreaName = sut.areaName(AreaType.NATION, lookup)

        assertThat(lookupAreaName).isEqualTo(areaName)
    }

    @Test
    fun `WHEN areaName called with OVERVIEW THEN overview name is returned`() {
        val lookupAreaName = sut.areaName(AreaType.OVERVIEW, lookupEntity)

        assertThat(lookupAreaName).isEqualTo("United Kingdom")
    }

    companion object {
        val lookupEntity = AreaLookupDto(
            lsoaCode = "",
            lsoaName = null,
            msoaCode = "",
            msoaName = null,
            ltlaCode = "",
            ltlaName = "",
            utlaCode = "",
            utlaName = "",
            nhsRegionCode = null,
            nhsRegionName = null,
            nhsTrustCode = null,
            nhsTrustName = null,
            regionCode = "",
            regionName = null,
            nationCode = "",
            nationName = ""
        )
        val lookupEntityWithoutNhsRegion = lookupEntity.copy(
            nhsRegionCode = null,
            nhsRegionName = null
        )
        val defaultArea = AreaDto(
            code = Constants.UK_AREA_CODE,
            name = "United Kingdom",
            regionType = AreaType.OVERVIEW
        )
    }
}
