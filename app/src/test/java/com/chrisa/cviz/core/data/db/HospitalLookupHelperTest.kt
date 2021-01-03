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

package com.chrisa.cviz.core.data.db

import com.chrisa.cviz.core.data.db.hospitallookups.HospitalLookup
import com.chrisa.cviz.core.data.db.hospitallookups.HospitalLookupHelper
import com.chrisa.cviz.core.data.db.hospitallookups.HospitalLookupsAssetDataSource
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class HospitalLookupHelperTest {

    private val appDatabase: AppDatabase = mockk(relaxed = true)
    private val healthcareLookupDao: HealthcareLookupDao = mockk(relaxed = true)
    private val hospitalLookupsDataSource: HospitalLookupsAssetDataSource = mockk(relaxed = true)
    private lateinit var sut: HospitalLookupHelper

    @Before
    fun setup() {
        every { appDatabase.healthcareLookupDao() } returns healthcareLookupDao

        sut = HospitalLookupHelper(appDatabase, hospitalLookupsDataSource)
    }

    @Test
    fun `GIVEN healthcare lookups exist WHEN insertHospitalLookupData called THEN lookups not inserted`() {
        every { healthcareLookupDao.countAll() } returns 1

        sut.insertHospitalLookupData()

        verify(exactly = 0) { healthcareLookupDao.insert(any()) }
    }

    @Test
    fun `GIVEN asset lookups exist WHEN insertHospitalLookupData called THEN lookups inserted`() {
        val hospitalLookup = HospitalLookup("Foo", "Ba")
        every { healthcareLookupDao.countAll() } returns 0
        every { hospitalLookupsDataSource.getItems() } returns listOf(hospitalLookup)

        sut.insertHospitalLookupData()

        verify(exactly = 1) {
            healthcareLookupDao.insert(
                HealthcareLookupEntity(
                    areaCode = "Foo",
                    nhsTrustCode = "Ba"
                )
            )
        }
    }
}
