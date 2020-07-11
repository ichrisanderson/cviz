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

package com.chrisa.covid19.core.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtils {
    private const val RFC_1123_DATE_TIME = "EEE, dd MMM yyyy HH:mm:ss z"

    fun Date.addHours(hours: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.time = this
        calendar.add(Calendar.HOUR_OF_DAY, hours)

        return calendar.time
    }

    fun Date.toGmtDate(): String {
        val formatter = SimpleDateFormat(
            RFC_1123_DATE_TIME,
            Locale.UK
        )
        formatter.timeZone = TimeZone.getTimeZone("GMT")

        return formatter.format(this)
    }
}
