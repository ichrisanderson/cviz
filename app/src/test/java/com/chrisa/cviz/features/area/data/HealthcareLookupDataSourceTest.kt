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

package com.chrisa.cviz.features.area.data

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.HealthcareLookupDao
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class HealthcareLookupDataSourceTest {

    private val appDatabase: AppDatabase = mockk()
    private val healthcareLookupDao: HealthcareLookupDao = mockk(relaxed = true)
    private val sut = HealthcareLookupDataSource(appDatabase)

    @Before
    fun init() {
        every { appDatabase.healthcareLookupDao() } returns healthcareLookupDao
    }

    @Test
    fun `WHEN healthcareLookups called THEN healthcareLookupDao queried`() {
        val areaCode = "E1"

        sut.healthcareLookups(areaCode)

        verify(exactly = 1) { healthcareLookupDao.byAreaCode(areaCode) }
    }
}
