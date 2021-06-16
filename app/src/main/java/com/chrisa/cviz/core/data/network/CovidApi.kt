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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.LocalDateTime
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.QueryMap
import retrofit2.http.Url

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

    @GET("v1/code")
    suspend fun areaLookupData(
        @Query(value = "category") category: String,
        @Query(value = "search") search: String
    ): AreaLookupData

    @GET("v1/data")
    suspend fun pagedHealthcareDataResponse(
        @Header("If-Modified-Since") modifiedDate: String?,
        @Query(encoded = true, value = "filters") filters: String,
        @Query(value = "structure") structure: String
    ): Response<Page<HealthcareData>>

    @GET("v2/data")
    suspend fun alertLevel(
        @Header("If-Modified-Since") modifiedDate: String?,
        @QueryMap filters: Map<String, String>
    ): Response<BodyPage<AlertLevel>>

    @GET("v1/soa")
    suspend fun soaData(
        @Header("If-Modified-Since") modifiedDate: String?,
        @Query(value = "filters") filters: String
    ): Response<SoaDataModel>

    @GET
    suspend fun nationPercentile(@Url url: String = NATION_PERCENTILES_URL): Map<String, MapPercentileModel>
}

private const val NATION_PERCENTILES_URL = "https://coronavirus.data.gov.uk/downloads/maps/nation_percentiles.json"

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
    val newCases: Int?,
    val cumulativeCases: Int?,
    val infectionRate: Double?,
    val newDeathsByPublishedDate: Int?,
    val cumulativeDeathsByPublishedDate: Int?,
    val cumulativeDeathsByPublishedDateRate: Double?,
    val newDeathsByDeathDate: Int?,
    val cumulativeDeathsByDeathDate: Int?,
    val cumulativeDeathsByDeathDateRate: Double?,
    val newOnsDeathsByRegistrationDate: Int?,
    @Json(name = "cumOnsDeathsByRegistrationDate")
    val cumulativeOnsDeathsByRegistrationDate: Int?,
    @Json(name = "cumOnsDeathsByRegistrationDateRate")
    val cumulativeOnsDeathsByRegistrationDateRate: Double?
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
            put("newOnsDeathsByRegistrationDate", "newWeeklyNsoDeathsByRegDate")
            put("cumOnsDeathsByRegistrationDate", "cumWeeklyNsoDeathsByRegDate")
            put("cumOnsDeathsByRegistrationDateRate", "cumWeeklyNsoDeathsByRegDateRate")
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
            put("newOnsDeathsByRegistrationDate", "newWeeklyNsoDeathsByRegDate")
            put("cumOnsDeathsByRegistrationDate", "cumWeeklyNsoDeathsByRegDate")
            put("cumOnsDeathsByRegistrationDateRate", "cumWeeklyNsoDeathsByRegDateRate")
        }.toString()
    }
}

@JsonClass(generateAdapter = true)
data class MetadataModel(
    val lastUpdatedAt: LocalDateTime
)

@JsonClass(generateAdapter = true)
data class AreaLookupData(
    val postcode: String,
    val trimmedPostcode: String,
    val lsoa: String,
    val lsoaName: String?,
    val msoa: String,
    val msoaName: String?,
    val ltla: String,
    val ltlaName: String,
    val utla: String,
    val utlaName: String,
    val region: String?,
    val regionName: String?,
    val nhsTrust: String?,
    val nhsTrustName: String?,
    val nhsRegion: String?,
    val nhsRegionName: String?,
    val nation: String,
    val nationName: String
)

@JsonClass(generateAdapter = true)
data class HealthcareData(
    val areaCode: String,
    val areaName: String,
    val areaType: String,
    val date: LocalDate,
    val newAdmissions: Int?,
    val cumulativeAdmissions: Int?,
    val occupiedBeds: Int?,
    val transmissionRateMin: Double?,
    val transmissionRateMax: Double?,
    val transmissionRateGrowthRateMin: Double?,
    val transmissionRateGrowthRateMax: Double?
) {
    companion object {
        val STRUCTURE = JSONObject().apply {
            put("areaCode", "areaCode")
            put("areaName", "areaName")
            put("areaType", "areaType")
            put("date", "date")
            put("newAdmissions", "newAdmissions")
            put("cumulativeAdmissions", "cumAdmissions")
            put("occupiedBeds", "covidOccupiedMVBeds")
            put("transmissionRateMin", "transmissionRateMin")
            put("transmissionRateMax", "transmissionRateMax")
            put("transmissionRateGrowthRateMin", "transmissionRateGrowthRateMin")
            put("transmissionRateGrowthRateMax", "transmissionRateGrowthRateMax")
        }.toString()
    }
}

@JsonClass(generateAdapter = true)
data class BodyPage<T>(
    val length: Int?,
    val body: List<T>
)

@JsonClass(generateAdapter = true)
data class AlertLevel(
    val areaCode: String,
    val areaName: String,
    val areaType: String,
    val date: LocalDate,
    val alertLevel: Int,
    val alertLevelName: String,
    val alertLevelUrl: String,
    val alertLevelValue: Int
)

@JsonClass(generateAdapter = true)
data class SoaDataModel(
    val areaCode: String,
    val areaName: String,
    val areaType: String,
    val latest: LatestChangeModel,
    val newCasesBySpecimenDate: List<RollingChangeModel>
) {
    companion object {
        fun maosFilter(areaCode: String) =
            "&areaType=msoa&areaCode=$areaCode"
    }
}

@JsonClass(generateAdapter = true)
data class LatestChangeModel(
    val newCasesBySpecimenDate: RollingChangeModel
)

@JsonClass(generateAdapter = true)
data class RollingChangeModel(
    val date: LocalDate,
    val rollingSum: Int?,
    val rollingRate: Double?,
    val change: Int?,
    val direction: String?,
    val changePercentage: Double?
)

@JsonClass(generateAdapter = true)
data class MapPercentileModel(
    val min: Double,
    val first: Double,
    val second: Double,
    val third: Double,
    val max: Double
)
