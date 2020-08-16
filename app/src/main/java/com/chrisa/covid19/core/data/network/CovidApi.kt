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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.LocalDateTime
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface CovidApi {
    @Headers(
        "Content-Type: application/json;charset=utf-8",
        "Accept: application/json",
        "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36"
    )
    @GET("v1/lookup?filters=areaType=nation%257CareaType=region%257CareaType=utla%257CareaType=ltla&structure=%7B%22areaName%22:%22areaName%22,%22areaCode%22:%22areaCode%22,%22areaType%22:%22areaType%22%7D")
    suspend fun pagedAreaResponse(@Header("If-Modified-Since") modifiedDate: String): Response<Page<AreaModel>>

    @Headers(
        "Content-Type: application/json;charset=utf-8",
        "Accept: application/json",
        "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36"
    )
    @GET("v1/data?filters=areaType=overview&structure=%7B%22areaType%22:%22areaType%22,%22areaName%22:%22areaName%22,%22areaCode%22:%22areaCode%22,%22date%22:%22date%22,%22newCases%22:%22newCasesByPublishDate%22,%22cmlCases%22:%22cumCasesByPublishDate%22,%22infectionRate%22:%22cumCasesByPublishDateRate%22%7D&format=json")
    suspend fun pagedUkOverviewAreaDataResponse(@Header("If-Modified-Since") modifiedDate: String): Response<Page<AreaDataModel>>

    @Headers(
        "Content-Type: application/json;charset=utf-8",
        "Accept: application/json",
        "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36"
    )
    @GET("v1/data?structure=%7B%22areaType%22:%22areaType%22,%22areaName%22:%22areaName%22,%22areaCode%22:%22areaCode%22,%22date%22:%22date%22,%22newCases%22:%22newCasesByPublishDate%22,%22cmlCases%22:%22cumCasesByPublishDate%22,%22infectionRate%22:%22cumCasesByPublishDateRate%22%7D&format=json")
    suspend fun pagedAreaCodeDataByPublishDateResponse(@Query(encoded = true, value = "filters") filters: String): Response<Page<AreaDataModel>>

    @Headers(
        "Content-Type: application/json;charset=utf-8",
        "Accept: application/json",
        "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36"
    )
    @GET("v1/data?structure=%7B%22areaType%22:%22areaType%22,%22areaName%22:%22areaName%22,%22areaCode%22:%22areaCode%22,%22date%22:%22date%22,%22newCases%22:%22newCasesBySpecimenDate%22,%22cmlCases%22:%22cumCasesBySpecimenDate%22,%22infectionRate%22:%22cumCasesBySpecimenDateRate%22%7D&format=json")
    suspend fun pagedAreaCodeDataBySpeciminDateResponse(@Query(encoded = true, value = "filters") filters: String): Response<Page<AreaDataModel>>
}

@JsonClass(generateAdapter = true)
data class Page<T>(
    val length: Int?,
    val maxPageLimit: Int?,
    val data: List<T>
)

@JsonClass(generateAdapter = true)
data class AreaModel(
    val areaCode: String,
    val areaName: String,
    val areaType: String
)

@JsonClass(generateAdapter = true)
data class AreaDataModel(
    val areaCode: String,
    val areaName: String,
    val areaType: String,
    val date: LocalDate,
    val newCases: Int?,
    @Json(name = "cmlCases")
    val cumulativeCases: Int?,
    val infectionRate: Double?
)

@JsonClass(generateAdapter = true)
data class MetadataModel(
    val lastUpdatedAt: LocalDateTime
)

// curl 'https://api.coronavirus.data.gov.uk/v1/data?filters=areaType=ltla&latestBy=cumCasesByPublishDate&structure=%7B%22areaName%22:%22areaName%22,%22cumCasesByPublishDate%22:%22cumCasesByPublishDate%22,%22cumCasesByPublishDateRate%22:%22cumCasesByPublishDateRate%22%7D' \
// -H 'Connection: keep-alive' \
// -H 'Pragma: no-cache' \
// -H 'Cache-Control: no-cache' \
// -H 'Accept: application/json, text/plain, */*' \
// -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36' \
// -H 'Origin: https://coronavirus.data.gov.uk' \
// -H 'Sec-Fetch-Site: same-site' \
// -H 'Sec-Fetch-Mode: cors' \
// -H 'Sec-Fetch-Dest: empty' \
// -H 'Referer: https://coronavirus.data.gov.uk/cases?areaType=nation&areaName=England' \
// -H 'Accept-Language: en-GB,en-US;q=0.9,en;q=0.8' \
// --compressed
