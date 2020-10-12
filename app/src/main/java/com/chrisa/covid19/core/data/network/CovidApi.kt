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
import retrofit2.http.Query

interface CovidApi {

    @GET("v1/lookup")
    suspend fun pagedAreaResponse(
        @Header("If-Modified-Since") modifiedDate: String?,
        @Query(encoded = true, value = "filters") filters: String,
        @Query(value = "structure") structure: String
    ): Response<Page<AreaModel>>

    @GET("v1/data")
    suspend fun pagedAreaDataResponse(
        @Header("If-Modified-Since") modifiedDate: String?,
        @Query(encoded = true, value = "filters") filters: String,
        @Query(value = "structure") structure: String
    ): Response<Page<AreaDataModel>>

    @GET("v1/data")
    suspend fun pagedAreaData(
        @Header("If-Modified-Since") modifiedDate: String?,
        @Query(encoded = true, value = "filters") filters: String,
        @Query(value = "structure") structure: String
    ): Page<AreaDataModel>
}

val AREA_FILTER = "areaType=nation;areaType=region;areaType=utla;areaType=ltla"
fun AREA_DATA_FILTER(areaCode: String, areaType: String) = "areaCode=$areaCode;areaType=$areaType"
fun DAILY_AREA_DATA_FILTER(date: String, areaType: String) = "date=$date;areaType=$areaType"

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
) {
    companion object {
        val AREA_MODEL_STRUCTURE = JSONObject().apply {
            put("areaCode", "areaCode")
            put("areaName", "areaName")
            put("areaType", "areaType")
        }.toString()
    }
}

@JsonClass(generateAdapter = true)
data class AreaDataModel(
    val areaCode: String,
    val areaName: String,
    val areaType: String,
    val date: LocalDate,
    @Json(name = "newCases")
    val newCases: Int?,
    @Json(name = "cumulativeCases")
    val cumulativeCases: Int?,
    @Json(name = "infectionRate")
    val infectionRate: Double?,
    @Json(name = "newDeathsByPublishedDate")
    val newDeathsByPublishedDate: Int?,
    @Json(name = "cumulativeDeathsByPublishedDate")
    val cumulativeDeathsByPublishedDate: Int?,
    @Json(name = "cumulativeDeathsByPublishedDateRate")
    val cumulativeDeathsByPublishedDateRate: Double?,
    @Json(name = "newDeathsByDeathDate")
    val newDeathsByDeathDate: Int?,
    @Json(name = "cumulativeDeathsByDeathDate")
    val cumulativeDeathsByDeathDate: Int?,
    @Json(name = "cumulativeDeathsByDeathDateRate")
    val cumulativeDeathsByDeathDateRate: Double?,
    @Json(name = "newAdmissions")
    val newAdmissions: Int?,
    @Json(name = "cumulativeAdmissions")
    val cumulativeAdmissions: Int?,
    @Json(name = "occupiedBeds")
    val occupiedBeds: Int?
) {
    companion object {

        val AREA_DATA_MODEL_BY_PUBLISH_DATE_STRUCTURE = JSONObject().apply {
            put("areaCode", "areaCode")
            put("areaName", "areaName")
            put("areaType", "areaType")
            put("date", "date")
            put("newCases", "newCasesByPublishDate")
            put("cumulativeCases", "cumCasesByPublishDate")
            put("infectionRate", "cumCasesByPublishDateRate")
            put("newDeathsByPublishedDate", "newDeaths28DaysByPublishDate")
            put("cumulativeDeathsByPublishedDate", "cumDeaths28DaysByPublishDate")
            put("cumulativeDeathsByPublishedDateRate", "cumDeaths28DaysByPublishDateRate")
            put("newDeathsByDeathDate", "newDeaths28DaysByDeathDate")
            put("cumulativeDeathsByDeathDate", "cumDeaths28DaysByDeathDate")
            put("cumulativeDeathsByDeathDateRate", "cumDeaths28DaysByDeathDateRate")
            put("newAdmissions", "newAdmissions")
            put("cumulativeAdmissions", "cumAdmissions")
            put("occupiedBeds", "covidOccupiedMVBeds")
        }.toString()

        val AREA_DATA_MODEL_BY_SPECIMEN_DATE_STRUCTURE = JSONObject().apply {
            put("areaCode", "areaCode")
            put("areaName", "areaName")
            put("areaType", "areaType")
            put("date", "date")
            put("newCases", "newCasesBySpecimenDate")
            put("cumulativeCases", "cumCasesBySpecimenDate")
            put("infectionRate", "cumCasesBySpecimenDateRate")
            put("newDeathsByPublishedDate", "newDeaths28DaysByPublishDate")
            put("cumulativeDeathsByPublishedDate", "cumDeaths28DaysByPublishDate")
            put("cumulativeDeathsByPublishedDateRate", "cumDeaths28DaysByPublishDateRate")
            put("newDeathsByDeathDate", "newDeaths28DaysByDeathDate")
            put("cumulativeDeathsByDeathDate", "cumDeaths28DaysByDeathDate")
            put("cumulativeDeathsByDeathDateRate", "cumDeaths28DaysByDeathDateRate")
            put("newAdmissions", "newAdmissions")
            put("cumulativeAdmissions", "cumAdmissions")
            put("occupiedBeds", "covidOccupiedMVBeds")
        }.toString()

        val AREA_DATA_MODEL_BY_SPECIMEN_DATE_STRUCTURE_NO_DEATHS = JSONObject().apply {
            put("areaCode", "areaCode")
            put("areaName", "areaName")
            put("areaType", "areaType")
            put("date", "date")
            put("newCases", "newCasesBySpecimenDate")
            put("cumulativeCases", "cumCasesBySpecimenDate")
            put("infectionRate", "cumCasesBySpecimenDateRate")
        }.toString()
    }
}

@JsonClass(generateAdapter = true)
data class MetadataModel(
    val lastUpdatedAt: LocalDateTime
)
