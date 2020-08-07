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

import com.chrisa.covid19.core.data.network.AreaModel
import com.chrisa.covid19.core.data.network.CasesModel
import com.chrisa.covid19.core.util.coroutines.TestCoroutineDispatchersImpl
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class AssetBootstrapperTest {

    private val assetDataSource = mockk<AssetDataSource>(relaxed = true)
    private val offlineDataSource = mockk<OfflineDataSource>(relaxed = true)
    private val testDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testDispatcher)
    private val casesModel = mockk<CasesModel>(relaxed = true)

    private lateinit var sut: AssetBootstrapper

    @Before
    fun setup() {

        coEvery { assetDataSource.getCases() } returns casesModel

        sut = AssetBootstrapper(
            assetDataSource,
            offlineDataSource,
            TestCoroutineDispatchersImpl(testDispatcher)
        )
    }

    @Test
    fun `GIVEN offline cases WHEN bootstrap data THEN case data is not updated`() =
        testCoroutineScope.runBlockingTest {

            every { offlineDataSource.areaCount() } returns 1

            sut.bootstrapData()

            verify(exactly = 0) {
                offlineDataSource.insertAreas(any())
            }
        }

    @Test
    fun `GIVEN no offline areas WHEN bootstrap data THEN area data is updated`() =
        testCoroutineScope.runBlockingTest {

            val area = AreaModel(
                areaCode = "1234",
                areaName = "UK",
                areaType = "overview"
            )

            val areas = listOf(area)

            coEvery { assetDataSource.getAreas() } returns areas
            every { offlineDataSource.areaCount() } returns 0

            sut.bootstrapData()

            verify { offlineDataSource.insertAreas(areas) }
        }

//    @Test
//    fun `GIVEN no offline cases WHEN bootstrap data THEN case data is updated`() =
//        testCoroutineScope.runBlockingTest {
//
//            every { offlineDataSource.casesCount() } returns 0
//
//            sut.bootstrapData()
//
//            val allCases = casesModel.countries.union(casesModel.ltlas).union(casesModel.utlas)
//                .union(casesModel.regions)
//
//            verify { offlineDataSource.insertCaseMetadata(casesModel.metadata) }
//            verify {
//                offlineDataSource.insertDailyRecord(
//                    casesModel.dailyRecords,
//                    casesModel.metadata.lastUpdatedAt.toLocalDate()
//                )
//            }
//            verify { offlineDataSource.insertCases(allCases) }
//        }
//
//    @Test
//    fun `GIVEN offline cases WHEN bootstrap data THEN case data is not updated`() =
//        testCoroutineScope.runBlockingTest {
//
//            every { offlineDataSource.casesCount() } returns 1
//
//            sut.bootstrapData()
//
//            verify(exactly = 0) {
//                offlineDataSource.insertCaseMetadata(any())
//            }
//
//            verify(exactly = 0) {
//                offlineDataSource.insertDailyRecord(any(), any())
//            }
//            verify(exactly = 0) {
//                offlineDataSource.insertCases(any())
//            }
//        }
}
