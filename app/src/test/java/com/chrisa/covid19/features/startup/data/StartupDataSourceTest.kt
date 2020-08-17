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

package com.chrisa.covid19.features.startup.data

import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.AreaDataDao
import com.chrisa.covid19.core.data.db.Constants
import com.chrisa.covid19.core.data.db.MetaDataIds
import com.chrisa.covid19.core.data.db.MetadataDao
import com.chrisa.covid19.core.data.db.SavedAreaDao
import com.chrisa.covid19.core.data.db.SavedAreaEntity
import com.chrisa.covid19.core.util.mockTransaction
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test

class StartupDataSourceTest {

    private val appDatabase = mockk<AppDatabase>()
    private val areaDataDao = mockk<AreaDataDao>(relaxed = true)
    private val metadataDao = mockk<MetadataDao>(relaxed = true)
    private val savedAreaDao = mockk<SavedAreaDao>(relaxed = true)
    private val testDispatcher = TestCoroutineDispatcher()

    private val sut = StartupDataSource(appDatabase)

    @Before
    fun setup() {
        every { appDatabase.metadataDao() } returns metadataDao
        every { appDatabase.areaDataDao() } returns areaDataDao
        every { appDatabase.savedAreaDao() } returns savedAreaDao
        appDatabase.mockTransaction()
    }

    @Test
    fun `WHEN execute called THEN data source is called`() = testDispatcher.runBlockingTest {

        val ukOverviewCode = listOf(Constants.UK_AREA_CODE)
        val nonAreaMetadataIds = listOf(MetaDataIds.areaListId(), MetaDataIds.ukOverviewId())

        val savedAreaEntity = SavedAreaEntity(
            areaCode = "1234"
        )
        val savedAreas = listOf(savedAreaEntity)
        every { savedAreaDao.all() } returns savedAreas

        sut.clearNonSavedAreaDataCache()

        verify(exactly = 1) {
            metadataDao.deleteAllNotInIds(nonAreaMetadataIds + savedAreas.map {
                MetaDataIds.areaCodeId(
                    it.areaCode
                )
            })
        }
        verify(exactly = 1) {
            areaDataDao.deleteAllNotInAreaCodes(ukOverviewCode + savedAreas.map {
                it.areaCode
            })
        }
    }
}
