package com.chrisa.covid19.core.data.network

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import java.util.Date

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
    val lastUpdatedAt: Date
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
    val specimenDate: Date,
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
    val reportingDate: Date
)
