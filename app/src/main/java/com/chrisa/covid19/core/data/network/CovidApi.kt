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
import org.json.JSONObject
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
    @GET("v1/lookup")
    suspend fun pagedAreaResponse(
        @Header("If-Modified-Since") modifiedDate: String?,
        @Query(encoded = true, value = "filters") filters: String,
        @Query(value = "structure") areaDataModelStructure: String
    ): Response<Page<AreaModel>>

    @Headers(
        "Content-Type: application/json;charset=utf-8",
        "Accept: application/json",
        "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36"
    )
    @GET("v1/data")
    suspend fun pagedAreaDataResponse(
        @Header("If-Modified-Since") modifiedDate: String?,
        @Query(encoded = true, value = "filters") filters: String,
        @Query(value = "structure") areaDataModelStructure: String
    ): Response<Page<AreaDataModel>>
}

val AREA_FILTER = "areaType=nation;areaType=region;areaType=utla;areaType=ltla"
fun AREA_DATA_FILTER(areaCode: String, areaType: String) = "areaCode=$areaCode;areaType=$areaType"
fun DAILY_AREA_DATA_FILTER(date: String, areaType: String) = "date=$date;areaType=$areaType"

val AREA_MODEL_STRUCTURE = JSONObject().apply {
    put("areaCode", "areaCode")
    put("areaName", "areaName")
    put("areaType", "areaType")
}.toString()

val AREA_DATA_MODEL_BY_PUBLISH_DATE_STRUCTURE = JSONObject().apply {
    put("areaCode", "areaCode")
    put("areaName", "areaName")
    put("areaType", "areaType")
    put("date", "date")
    put("newCases", "newCasesByPublishDate")
    put("cmlCases", "cumCasesByPublishDate")
    put("infRate", "cumCasesByPublishDateRate")
}.toString()

val AREA_DATA_MODEL_BY_SPECIMEN_DATE_STRUCTURE = JSONObject().apply {
    put("areaCode", "areaCode")
    put("areaName", "areaName")
    put("areaType", "areaType")
    put("date", "date")
    put("newCases", "newCasesBySpecimenDate")
    put("cmlCases", "cumCasesBySpecimenDate")
    put("infRate", "cumCasesBySpecimenDateRate")
}.toString()

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
    @Json(name = "infRate")
    val infectionRate: Double?
)

@JsonClass(generateAdapter = true)
data class MetadataModel(
    val lastUpdatedAt: LocalDateTime
)
