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

package com.chrisa.cviz.core.util

import android.text.format.DateUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object DateFormatter {

    fun getLocalRelativeTimeSpanString(date: LocalDateTime?): CharSequence {
        val zoneId = ZoneId.of("GMT")
        val gmtTime = date?.atZone(zoneId) ?: return ""
        val now = LocalDateTime.now().atZone(zoneId)
        return DateUtils.getRelativeTimeSpanString(
            gmtTime.toInstant().toEpochMilli(),
            now.toInstant().toEpochMilli(),
            DateUtils.MINUTE_IN_MILLIS
        )
    }

    fun mediumLocalizedDate(date: LocalDate?): String {
        return date?.format(
            DateTimeFormatter.ofLocalizedDate(
                FormatStyle.MEDIUM
            )
        ) ?: ""
    }
}
