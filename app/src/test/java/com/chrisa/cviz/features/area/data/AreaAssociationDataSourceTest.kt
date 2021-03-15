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
import com.chrisa.cviz.core.data.db.AreaAssociation
import com.chrisa.cviz.core.data.db.AreaAssociationDao
import com.chrisa.cviz.core.data.db.AreaAssociationType
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class AreaAssociationDataSourceTest {

    private val areaAssociationDao: AreaAssociationDao = mockk()
    private val db: AppDatabase = mockk()
    private val sut = AreaAssociationDataSource(db)

    @Before
    fun setup() {
        every { db.areaAssociationDao() } returns areaAssociationDao
        every { areaAssociationDao.insert(any()) } just Runs
    }

    @Test
    fun `GIVEN association type is area lookup WHEN insert called THEN area lookup association inserted`() {
        sut.insert("1", "2", AreaAssociationType.AREA_LOOKUP)

        verify {
            areaAssociationDao.insert(
                AreaAssociation(
                    "1",
                    "2",
                    AreaAssociationType.AREA_LOOKUP
                )
            )
        }
    }

    @Test
    fun `GIVEN association type is area data WHEN insert called THEN area data association inserted`() {
        sut.insert("1", "2", AreaAssociationType.AREA_DATA)

        verify {
            areaAssociationDao.insert(
                AreaAssociation(
                    "1",
                    "2",
                    AreaAssociationType.AREA_DATA
                )
            )
        }
    }

    @Test
    fun `GIVEN association type is healtchcare data WHEN insert called THEN healtchcare data association inserted`() {
        sut.insert("1", "2", AreaAssociationType.HEALTHCARE_DATA)

        verify {
            areaAssociationDao.insert(
                AreaAssociation(
                    "1",
                    "2",
                    AreaAssociationType.HEALTHCARE_DATA
                )
            )
        }
    }

    @Test
    fun `GIVEN association type is alert level WHEN insert called THEN alert level association inserted`() {
        sut.insert("1", "2", AreaAssociationType.ALERT_LEVEL)

        verify {
            areaAssociationDao.insert(
                AreaAssociation(
                    "1",
                    "2",
                    AreaAssociationType.ALERT_LEVEL
                )
            )
        }
    }
}
