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

package com.chrisa.cviz.core.data.network

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateJsonAdapter {
    @ToJson
    fun toJson(localDate: LocalDate): String {
        return localDate.format(FORMATTER)
    }

    @FromJson
    fun fromJson(json: String): LocalDate {
        return FORMATTER.parse(json, LocalDate::from)
    }

    companion object {
        private val FORMATTER = DateTimeFormatter.ISO_DATE
    }
}
