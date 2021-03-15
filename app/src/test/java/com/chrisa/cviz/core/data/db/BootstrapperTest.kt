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

package com.chrisa.cviz.core.data.db

import com.chrisa.cviz.core.data.db.hospitallookups.HospitalLookupHelper
import com.chrisa.cviz.core.data.db.legacy.LegacyAppDatabaseHelper
import com.chrisa.cviz.core.data.db.legacy.LegacySavedArea
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class BootstrapperTest {

    private val appDatabase: AppDatabase = mockk(relaxed = true)
    private val areaDao: AreaDao = mockk(relaxed = true)
    private val areaLookupDao: AreaLookupDao = mockk(relaxed = true)
    private val savedAreaDao: SavedAreaDao = mockk(relaxed = true)
    private val legacyAppDatabaseHelper: LegacyAppDatabaseHelper = mockk(relaxed = true)
    private val hospitalLookupHelper: HospitalLookupHelper = mockk(relaxed = true)
    private val unusedDataCleaner: UnusedDataCleaner = mockk(relaxed = true)
    private val expiredDataCleaner: ExpiredDataCleaner = mockk(relaxed = true)
    private val sut = Bootstrapper(
        appDatabase,
        legacyAppDatabaseHelper,
        hospitalLookupHelper,
        unusedDataCleaner,
        expiredDataCleaner
    )

    @Before
    fun setup() {
        every { legacyAppDatabaseHelper.hasDatabase() } returns false
        every { appDatabase.areaDao() } returns areaDao
        every { appDatabase.areaLookupDao() } returns areaLookupDao
        every { appDatabase.savedAreaDao() } returns savedAreaDao
    }

    @Test
    fun `GIVEN no saved areas WHEN legacy db exists THEN saved areas are copied and legacy database is deleted`() =
        runBlocking {
            every { legacyAppDatabaseHelper.hasDatabase() } returns true
            every { legacyAppDatabaseHelper.savedAreas() } returns savedAreas
            every { savedAreaDao.countAll() } returns 0

            sut.execute()

            verify(exactly = 1) { legacyAppDatabaseHelper.deleteDatabase() }
            verify(exactly = 1) { savedAreaDao.insertAll(savedAreaEntities) }
            verify(exactly = 1) { areaDao.insertAll(areaEntities) }
        }

    @Test
    fun `GIVEN saved areas WHEN legacy db exists THEN saved areas are not copied and legacy database is deleted`() =
        runBlocking {
            every { legacyAppDatabaseHelper.hasDatabase() } returns true
            every { legacyAppDatabaseHelper.savedAreas() } returns savedAreas
            every { savedAreaDao.countAll() } returns 1

            sut.execute()

            verify(exactly = 1) { legacyAppDatabaseHelper.deleteDatabase() }
            verify(exactly = 0) { savedAreaDao.insertAll(savedAreaEntities) }
            verify(exactly = 0) { areaDao.insertAll(areaEntities) }
        }

    @Test
    fun `WHEN area data present THEN data is not inserted`() =
        runBlocking {
            every { areaDao.count() } returns 1

            sut.execute()

            verify(exactly = 0) { areaDao.insertAll(BootstrapData.areaData()) }
        }

    @Test
    fun `WHEN area data is not present THEN data is inserted`() =
        runBlocking {
            every { areaDao.count() } returns 0

            sut.execute()

            verify(exactly = 1) { areaDao.insertAll(BootstrapData.areaData()) }
        }

    @Test
    fun `WHEN execute called THEN hospital lookups inserted`() =
        runBlocking {
            sut.execute()

            verify(exactly = 1) { hospitalLookupHelper.insertHospitalLookupData() }
        }

    @Test
    fun `WHEN execute called THEN expired data removed`() =
        runBlocking {
            sut.execute()

            coVerify(exactly = 1) { expiredDataCleaner.execute() }
        }

    @Test
    fun `WHEN execute called THEN unused data removed`() =
        runBlocking {
            sut.execute()

            coVerify(exactly = 1) { unusedDataCleaner.execute() }
        }

    companion object {
        val savedAreas = listOf(
            LegacySavedArea(
                areaCode = Constants.ENGLAND_AREA_CODE,
                areaName = Constants.ENGLAND_AREA_NAME,
                areaType = AreaType.NATION.value
            ),
            LegacySavedArea(
                areaCode = Constants.SCOTLAND_AREA_CODE,
                areaName = Constants.SCOTLAND_AREA_NAME,
                areaType = AreaType.NATION.value
            ),
            LegacySavedArea(
                areaCode = Constants.WALES_AREA_CODE,
                areaName = Constants.WALES_AREA_NAME,
                areaType = AreaType.NATION.value
            )
        )
        val areaEntities = savedAreas.map {
            AreaEntity(
                it.areaCode,
                it.areaName,
                AreaType.from(it.areaType)!!
            )
        }
        val savedAreaEntities = savedAreas.map {
            SavedAreaEntity(it.areaCode)
        }
    }
}
