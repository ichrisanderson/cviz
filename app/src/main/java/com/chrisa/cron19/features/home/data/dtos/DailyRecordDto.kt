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

package com.chrisa.cron19.features.home.data.dtos

import java.time.LocalDateTime

data class DailyRecordDto(
    val areaCode: String,
    val areaName: String,
    val areaType: String,
    val newCases: Int,
    val cumulativeCases: Int,
    val lastUpdated: LocalDateTime
)
