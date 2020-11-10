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

package com.chrisa.cviz.core.data.synchronisation

import com.chrisa.cviz.core.data.time.TimeProvider
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import org.junit.Test

class SyncTimeHelperTest {

    private val timeProvider: TimeProvider = mockk()

    @Test
    fun `WHEN start time before publish time THEN next sync time is today`() {
        val startTime = LocalDateTime.of(2020, 1, 1, 9, 0)
        every { timeProvider.currentDate() } returns startTime.toLocalDate()
        every { timeProvider.currentTime() } returns startTime
        val sut = SyncTimeHelper(timeProvider)

        val timeToNextSyncInMillis = sut.timeToNextSyncInMillis()

        assertThat(timeToNextSyncInMillis).isEqualTo(TimeUnit.HOURS.toMillis(7))
    }

    @Test
    fun `WHEN start time after publish time THEN next sync time is tomorrow`() {
        val startTime = LocalDateTime.of(2020, 1, 1, 21, 0)
        every { timeProvider.currentDate() } returns startTime.toLocalDate()
        every { timeProvider.currentTime() } returns startTime
        val sut = SyncTimeHelper(timeProvider)

        val timeToNextSyncInMillis = sut.timeToNextSyncInMillis()

        assertThat(timeToNextSyncInMillis).isEqualTo(TimeUnit.HOURS.toMillis(19))
    }
}
