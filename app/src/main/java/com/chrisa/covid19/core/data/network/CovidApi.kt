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

package com.chrisa.covid19.core.data.network

import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.LocalDateTime
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

interface CovidApi {

    @Headers(
        "Accept-Encoding: gzip, deflate",
        "Content-Type: application/json;charset=utf-8",
        "Accept: application/json"
    )
    @GET("downloads/json/coronavirus-cases_latest.json")
    suspend fun getCases(@Header("If-Modified-Since") modifiedDate: String): Response<CasesModel>

    @Headers(
        "Accept-Encoding: gzip, deflate",
        "Content-Type: application/json;charset=utf-8",
        "Accept: application/json"
    )
    @GET("downloads/json/coronavirus-deaths_latest.json")
    suspend fun getDeaths(@Header("If-Modified-Since") modifiedDate: String): Response<DeathsModel>
}

@JsonClass(generateAdapter = true)
data class CasesModel(
    val countries: List<CaseModel>,
    val dailyRecords: DailyRecordModel,
    val ltlas: List<CaseModel>,
    val metadata: MetadataModel,
    val regions: List<CaseModel>,
    val utlas: List<CaseModel>
)

@JsonClass(generateAdapter = true)
data class DailyRecordModel(
    val areaName: String,
    val dailyLabConfirmedCases: Int,
    val totalLabConfirmedCases: Int?
)

@JsonClass(generateAdapter = true)
data class MetadataModel(
    val disclaimer: String,
    val lastUpdatedAt: LocalDateTime
)

@JsonClass(generateAdapter = true)
data class CaseModel(
    val areaCode: String,
    val areaName: String,
    val changeInDailyCases: Int?,
    val changeInTotalCases: Int?,
    val dailyLabConfirmedCases: Int?,
    val dailyTotalLabConfirmedCasesRate: Double?,
    val previouslyReportedDailyCases: Int?,
    val previouslyReportedTotalCases: Int?,
    val specimenDate: LocalDate,
    val totalLabConfirmedCases: Int?
)

@JsonClass(generateAdapter = true)
data class DeathsModel(
    val countries: List<DeathModel>,
    val metadata: MetadataModel,
    val overview: List<DeathModel>
)

@JsonClass(generateAdapter = true)
data class DeathModel(
    val areaCode: String,
    val areaName: String,
    val cumulativeDeaths: Int,
    val dailyChangeInDeaths: Int?,
    val reportingDate: LocalDate
)
