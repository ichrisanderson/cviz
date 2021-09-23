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

package com.chrisa.cviz.core.util

import com.chrisa.cviz.core.util.DateUtils.toGmtDateTime
import com.google.common.truth.Truth.assertThat
import java.time.LocalDateTime
import java.util.Locale
import java.util.TimeZone
import org.junit.Test

class DateUtilsTest {

    @Test
    fun foo() {
        Locale.setDefault(Locale("en", "GB"))
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/London"))
        val dateString = "Mon, 20 Sep 2021 15:00:05 GMT"
        val date = dateString.toGmtDateTime()
        assertThat(date).isEqualTo(LocalDateTime.of(2021, 9, 20, 15, 0, 5))
    }
}
