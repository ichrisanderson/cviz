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
import com.chrisa.cviz.core.data.db.HealthcareLookupEntity
import com.chrisa.cviz.features.area.data.dtos.HealthcareLookupDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test

class HealthcareLookupDataSourceTest {

    private val appDatabase: AppDatabase = mockk()
    private val healthcareLookupDao: HealthcareLookupDao = mockk(relaxed = true)
    private val sut = HealthcareLookupDataSource(appDatabase)

    @Before
    fun setup() {
        every { appDatabase.healthcareLookupDao() } returns healthcareLookupDao
    }

    @Test
    fun `WHEN healthcareLookups called THEN healthcareLookupDao queried`() {
        val areaCode = "E1"
        val healthcareLookupEntity = HealthcareLookupEntity(
            areaCode = "Foo",
            nhsTrustCode = "Ba"
        )
        every { healthcareLookupDao.byAreaCode(areaCode) } returns listOf(healthcareLookupEntity)

        val data = sut.healthcareLookups(areaCode)

        assertThat(data).isEqualTo(
            listOf(
                HealthcareLookupDto(
                    healthcareLookupEntity.areaCode,
                    healthcareLookupEntity.nhsTrustCode
                )
            )
        )
    }
}
