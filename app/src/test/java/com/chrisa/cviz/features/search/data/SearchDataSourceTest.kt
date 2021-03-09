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

package com.chrisa.cviz.features.search.data

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaEntity
import com.chrisa.cviz.core.data.db.AreaLookupEntity
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.synchronisation.PostcodeLookupDataSynchroniser
import com.chrisa.cviz.features.search.data.dtos.AreaDTO
import com.google.common.truth.Truth.assertThat
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class SearchDataSourceTest {

    private val appDatabase = mockk<AppDatabase> {
        every { areaLookupDao().byTrimmedPostcode(any()) } returns null
    }
    private val postcodeLookupDataSynchroniser = mockk<PostcodeLookupDataSynchroniser> {
        coEvery { performSync(any()) } just Runs
    }
    private val queryTransformer = SearchQueryTransformer()
    private val sut =
        SearchDataSource(appDatabase, postcodeLookupDataSynchroniser, queryTransformer)

    @Test
    fun `WHEN searchAreas called THEN area dao queried`() {
        val area = AreaEntity(
            areaCode = "1234",
            areaName = "London",
            areaType = AreaType.UTLA
        )
        val areaNameAsQuery = queryTransformer.transformQuery(area.areaName)
        val expectedResults = listOf(area)
        every { appDatabase.areaDao().search(areaNameAsQuery) } returns expectedResults

        val results = sut.searchAreas(area.areaName)

        assertThat(results).isEqualTo(expectedResults.map {
            AreaDTO(
                it.areaCode,
                it.areaName,
                it.areaType.value
            )
        })
    }

    @Test
    fun `WHEN searchPostcode called THEN postcode synchronised`() = runBlocking {
        val postcode = "AA11AA"
        sut.searchPostcode(postcode)

        coVerify { postcodeLookupDataSynchroniser.performSync(postcode) }
    }

    @Test
    fun `GIVEN postcode data not present WHEN searchPostcode called THEN postcode synchronised`() =
        runBlocking {
            val postcode = "AA11AA"

            val result = sut.searchPostcode(postcode)

            assertThat(result).isNull()
        }

    @Test
    fun `GIVEN postcode data is present without msoa WHEN searchPostcode called THEN postcode synchronised`() =
        runBlocking {
            val postcode = "AA11AA"
            every { appDatabase.areaLookupDao().byTrimmedPostcode(postcode) } returns lookupEntity

            val result = sut.searchPostcode(postcode)

            assertThat(result).isNull()
        }

    @Test
    fun `GIVEN postcode data is present with msoa WHEN searchPostcode called THEN postcode synchronised`() =
        runBlocking {
            val postcode = "AA11AA"
            val lookup = lookupEntity.copy(msoaCode = "msoa1", msoaName = "msoa area")
            every { appDatabase.areaLookupDao().byTrimmedPostcode(postcode) } returns lookup

            val result = sut.searchPostcode(postcode)

            assertThat(result).isEqualTo(
                AreaDTO(
                    lookup.msoaCode,
                    lookup.msoaName!!,
                    AreaType.MSOA.value
                )
            )
        }

    companion object {
        val lookupEntity = AreaLookupEntity(
            postcode = "",
            trimmedPostcode = "",
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
