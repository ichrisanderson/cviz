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

package com.chrisa.covid19.core.data

import android.content.res.AssetManager
import com.chrisa.covid19.core.data.TestData.TEST_CASE_MODEL
import com.chrisa.covid19.core.data.network.AreaModel
import com.chrisa.covid19.core.data.network.CasesModel
import com.chrisa.covid19.core.data.network.Page
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class AssetDataSourceTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testDispatcher)
    private val moshi = Moshi.Builder()
        .add(LocalDateJsonAdapter())
        .add(LocalDateTimeJsonAdapter())
        .build()

    private val assetManager = mockk<AssetManager>(relaxed = true)

    private lateinit var sut: AssetDataSource

    @Before
    fun setup() {
        sut = AssetDataSource(assetManager, moshi)
    }

    @Test
    fun `GIVEN area model exists WHEN getAreas is called THEN area model is returned`() =
        testCoroutineScope.runBlockingTest {
            val areaModel = AreaModel(
                areaCode = "1234",
                areaName = "UK",
                areaType = "overview"
            )

            val page = Page(
                length = null,
                maxPageLimit = null,
                data = listOf(areaModel)
            )

            val type = Types.newParameterizedType(Page::class.java, AreaModel::class.java)
            val adapter = moshi.adapter<Page<AreaModel>>(type)

            val modelJson = adapter.toJson(page)
            every { assetManager.open("areas.json") } returns modelJson.byteInputStream()

            val result = sut.getAreas()
            assertThat(result).isEqualTo(page.data)
        }

    @Test
    fun `GIVEN case model exists WHEN getCases is called THEN case model is returned`() =
        testCoroutineScope.runBlockingTest {
            val caseModel = TEST_CASE_MODEL

            val casesModelAdapter = moshi.adapter(CasesModel::class.java)
            val testCaseModelJson = casesModelAdapter.toJson(caseModel)
            every { assetManager.open("coronavirus-cases_latest.json") } returns testCaseModelJson.byteInputStream()

            val result = sut.getCases()
            assertThat(result).isEqualTo(caseModel)
        }
}
