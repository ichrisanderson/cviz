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

package com.chrisa.cviz.features.area.data

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaLookupDao
import com.chrisa.cviz.core.data.db.AreaLookupEntity
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class AreaLookupDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()
    private val areaLookupDao = mockk<AreaLookupDao>()
    private val sut = AreaLookupDataSource(appDatabase)

    @Before
    fun setup() {
        every { appDatabase.areaLookupDao() } returns areaLookupDao
    }

    @Test
    fun `WHEN areaLookupByLtla called THEN ltla data returned`() {
        val area = lookupEntity.copy(
            ltlaCode = "code",
            ltlaName = "name"
        )
        every { areaLookupDao.byLtla("1") } returns area

        val areaLookup = sut.areaLookupByLtla("1")

        assertThat(areaLookup).isEqualTo(area.toAreaLookupDto())
    }

    @Test
    fun `WHEN areaLookupByUtla called THEN utla data returned`() {
        val area = lookupEntity.copy(
            utlaCode = "code",
            utlaName = "name"
        )
        every { areaLookupDao.byUtla("1") } returns area

        val areaLookup = sut.areaLookupByUtla("1")

        assertThat(areaLookup).isEqualTo(area.toAreaLookupDto())
    }

    @Test
    fun `WHEN areaLookupByRegion called THEN region data returned`() {
        val area = lookupEntity.copy(
            regionCode = "code",
            regionName = "name"
        )
        every { areaLookupDao.byRegion("1") } returns area

        val areaLookup = sut.areaLookupByRegion("1")

        assertThat(areaLookup).isEqualTo(area.toAreaLookupDto())
    }

    @Test
    fun `WHEN areaLookupByNhsRegion called THEN region data returned`() {
        val area = lookupEntity.copy(
            nhsRegionCode = "code",
            nhsRegionName = "name"
        )
        every { areaLookupDao.byNhsRegion("1") } returns area

        val areaLookup = sut.areaLookupByNhsRegion("1")

        assertThat(areaLookup).isEqualTo(area.toAreaLookupDto())
    }

    @Test
    fun `WHEN areaLookupByNhsTrust called THEN region data returned`() {
        val area = lookupEntity.copy(
            nhsTrustCode = "nhsTrustCode",
            nhsTrustName = "nhsTrustName    "
        )
        every { areaLookupDao.byNhsTrustCode("1") } returns area

        val areaLookup = sut.areaLookupByNhsTrust("1")

        assertThat(areaLookup).isEqualTo(area.toAreaLookupDto())
    }

    companion object {
        val lookupEntity = AreaLookupEntity(
            lsoaCode = "",
            lsoaName = null,
            msoaCode = "",
            msoaName = null,
            ltlaCode = "",
            ltlaName = "",
            utlaCode = "",
            utlaName = "",
            nhsTrustCode = null,
            nhsTrustName = null,
            nhsRegionCode = null,
            nhsRegionName = null,
            regionCode = "",
            regionName = null,
            nationCode = "",
            nationName = ""
        )
    }
}
