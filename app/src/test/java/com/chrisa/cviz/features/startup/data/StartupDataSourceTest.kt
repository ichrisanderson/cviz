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

package com.chrisa.cviz.features.startup.data

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaDataDao
import com.chrisa.cviz.core.data.db.AreaSummaryDao
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class StartupDataSourceTest {

    private val appDatabase: AppDatabase = mockk()
    private val areaDataDao: AreaDataDao = mockk(relaxed = true)
    private val areaSummaryDao: AreaSummaryDao = mockk(relaxed = true)
    private val sut = StartupDataSource(appDatabase)

    @Before
    fun setup() {
        every { appDatabase.areaDataDao() } returns areaDataDao
        every { appDatabase.areaSummaryDao() } returns areaSummaryDao
    }

    @Test
    fun `WHEN dataCount called THEN area data and summary counts returned`() {
        val areaDataDaoCount = 100
        val areaSummaryDaoCount = 1009
        every { areaDataDao.countAll() } returns areaDataDaoCount
        every { areaSummaryDao.countAll() } returns areaSummaryDaoCount

        val count = sut.dataCount()

        assertThat(count).isEqualTo(
            AreaData(
                areaData = areaDataDaoCount,
                areaSummaryEntities = areaSummaryDaoCount
            )
        )
    }
}
