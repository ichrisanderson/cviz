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

import com.chrisa.covid19.core.data.network.AreaDataModel
import com.chrisa.covid19.core.data.network.AreaModel
import com.chrisa.covid19.core.data.network.MetadataModel
import com.chrisa.covid19.core.util.coroutines.TestCoroutineDispatchersImpl
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class AssetBootstrapperTest {

    private val assetDataSource = mockk<AssetDataSource>(relaxed = true)
    private val offlineDataSource = mockk<OfflineDataSource>(relaxed = true)
    private val testDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testDispatcher)

    private lateinit var sut: AssetBootstrapper

    @Before
    fun setup() {
        sut = AssetBootstrapper(
            assetDataSource,
            offlineDataSource,
            TestCoroutineDispatchersImpl(testDispatcher)
        )
    }

    @Test
    fun `GIVEN offline areas WHEN bootstrap data THEN area data is not updated`() =
        testCoroutineScope.runBlockingTest {

            every { offlineDataSource.areaCount() } returns 1

            sut.bootstrapData()

            verify(exactly = 0) {
                assetDataSource.getAreas()
            }
        }

    @Test
    fun `GIVEN no offline areas WHEN bootstrap data THEN area data is updated`() =
        testCoroutineScope.runBlockingTest {
            mockkStatic(LocalDateTime::class)

            val area = AreaModel(
                areaCode = "1234",
                areaName = "UK",
                areaType = "overview"
            )

            val date = LocalDateTime.of(2020, 1, 1, 0, 0)
            val areas = listOf(area)

            coEvery { assetDataSource.getAreas() } returns areas
            every { offlineDataSource.areaCount() } returns 0
            every { LocalDateTime.now() } returns date

            sut.bootstrapData()

            verify {
                offlineDataSource.insertAreaMetadata(
                    MetadataModel(
                        lastUpdatedAt = date.minusDays(
                            1
                        )
                    )
                )
            }
            verify { offlineDataSource.insertAreas(areas) }
        }

    @Test
    fun `GIVEN offline area data overview WHEN bootstrap data THEN area data overview is not updated`() =
        testCoroutineScope.runBlockingTest {

            every { offlineDataSource.areaDataOverviewCount() } returns 1

            sut.bootstrapData()

            verify(exactly = 0) {
                assetDataSource.getOverviewAreaData()
            }
        }

    @Test
    fun `GIVEN no offline area data overview WHEN bootstrap data THEN area data overview is updated`() =
        testCoroutineScope.runBlockingTest {
            mockkStatic(LocalDateTime::class)

            val area = AreaDataModel(
                areaCode = "1234",
                areaName = "UK",
                areaType = "overview",
                cumulativeCases = 100,
                date = LocalDate.now(),
                infectionRate = 100.0,
                newCases = 10
            )

            val date = LocalDateTime.of(2020, 1, 1, 0, 0)
            val areas = listOf(area)

            coEvery { assetDataSource.getOverviewAreaData() } returns areas
            every { offlineDataSource.areaCount() } returns 0
            every { LocalDateTime.now() } returns date

            sut.bootstrapData()

            verify { offlineDataSource.insertAreaMetadata(MetadataModel(lastUpdatedAt = date.minusDays(1))) }
            verify { offlineDataSource.insertAreaData(areas) }
        }
}
