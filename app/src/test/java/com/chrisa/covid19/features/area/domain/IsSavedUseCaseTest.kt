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
import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test

@ExperimentalCoroutinesApi
class IsSavedUseCaseTest {

    private val areaDataSource = mockk<AreaDataSource>()
    private val sut = IsSavedUseCase(areaDataSource)

    @Test
    fun `GIVEN area is not saved WHEN savedState called THEN savedState is false`() =
        runBlockingTest {

            val areaCode = "A01"
            val publisher = ConflatedBroadcastChannel(false)

            every { areaDataSource.isSaved(areaCode) } returns publisher.asFlow()

            val savedState = sut.execute(areaCode).first()

            Truth.assertThat(savedState).isEqualTo(false)
        }
}
