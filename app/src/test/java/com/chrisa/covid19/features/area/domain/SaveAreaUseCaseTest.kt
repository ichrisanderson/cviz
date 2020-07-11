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

package com.chrisa.covid19.features.area.domain

import com.chrisa.covid19.features.area.data.AreaDataSource
import com.chrisa.covid19.features.area.data.dtos.SavedAreaDto
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class SaveAreaUseCaseTest {

    private val areaDataSource = mockk<AreaDataSource>()

    @Test
    fun `WHEN execute called THEN data source is updated with the area code`() {

        val areCode = "1234"
        val sut = SaveAreaUseCase(areaDataSource)

        val dto = SavedAreaDto(areCode)

        every { areaDataSource.saveArea(any()) } just Runs

        sut.execute(areCode)

        verify(exactly = 1) { areaDataSource.saveArea(dto) }
    }
}
