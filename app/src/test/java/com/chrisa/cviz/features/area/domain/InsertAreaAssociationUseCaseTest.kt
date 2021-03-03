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

package com.chrisa.cviz.features.area.domain

import com.chrisa.cviz.core.data.db.AreaAssociationType
import com.chrisa.cviz.features.area.data.AreaAssociationDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaAssociationTypeDto
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class InsertAreaAssociationUseCaseTest {

    private val dataSource: AreaAssociationDataSource = mockk()
    private val sut = InsertAreaAssociationUseCase(dataSource)

    @Before
    fun setup() {
        every { dataSource.insert(any(), any(), any()) } just Runs
    }

    @Test
    fun `GIVEN area lookup type WHEN insert called THEN data source updated`() {
        sut.execute("1", "2", AreaAssociationTypeDto.AREA_LOOKUP)

        verify { dataSource.insert("1", "2", AreaAssociationType.AREA_LOOKUP) }
    }

    @Test
    fun `GIVEN area data type WHEN insert called THEN data source updated`() {
        sut.execute("1", "2", AreaAssociationTypeDto.AREA_DATA)

        verify { dataSource.insert("1", "2", AreaAssociationType.AREA_DATA) }
    }

    @Test
    fun `GIVEN healthcare data type WHEN insert called THEN data source updated`() {
        sut.execute("1", "2", AreaAssociationTypeDto.HEALTHCARE_DATA)

        verify { dataSource.insert("1", "2", AreaAssociationType.HEALTHCARE_DATA) }
    }

    @Test
    fun `GIVEN soa data type WHEN insert called THEN data source updated`() {
        sut.execute("1", "2", AreaAssociationTypeDto.SOA_DATA)

        verify { dataSource.insert("1", "2", AreaAssociationType.SOA_DATA) }
    }
}
