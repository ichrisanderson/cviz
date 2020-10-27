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

package com.chrisa.cron19.features.area.domain

import com.chrisa.cron19.features.area.data.AreaDataSource
import com.chrisa.cron19.features.area.data.dtos.SavedAreaDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class DeleteSavedAreaUseCaseTest {

    private val areaDataSource = mockk<AreaDataSource>()
    private val sut = DeleteSavedAreaUseCase(areaDataSource)

    @Test
    fun `WHEN execute called THEN saved area is deleted`() {
        val areCode = "1234"
        every { areaDataSource.deleteSavedArea(SavedAreaDto(areCode)) } returns 1

        val deletedRows = sut.execute(areCode)

        assertThat(deletedRows).isEqualTo(1)
    }
}
