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

package com.chrisa.covid19.core.data.synchronisation

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RollingAverageHelperTest {
    private val sut = RollingAverageHelper()

    @Test
    fun `GIVEN a daily increase of 10 WHEN average called for week THEN average of 40 is returned`() {
        // 10, 20, 30, 40, 50, 60, 70
        val values = (0 until 7).map { 10 * (it + 1) }

        // 10, 20, 30, 40, 50, 60, 70 = 280
        val result = sut.average(values.lastIndex, values)

        // 280 / 7.0 = 40
        assertThat(result).isEqualTo(40)
    }
}
