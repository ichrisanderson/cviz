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

package com.chrisa.covid19.core.data

import com.chrisa.covid19.core.data.network.CaseModel
import com.chrisa.covid19.core.data.network.CasesModel
import com.chrisa.covid19.core.data.network.DailyRecordModel
import com.chrisa.covid19.core.data.network.DeathModel
import com.chrisa.covid19.core.data.network.DeathsModel
import com.chrisa.covid19.core.data.network.MetadataModel
import java.util.Date

object TestData {

    val TEST_COUNTRY_CASE_MODEL = CaseModel(
        areaName = "Test Country",
        areaCode = "TC001",
        changeInDailyCases = 1,
        changeInTotalCases = 2,
        dailyLabConfirmedCases = 3,
        dailyTotalLabConfirmedCasesRate = 40.0,
        specimenDate = Date(0),
        totalLabConfirmedCases = 3,
        previouslyReportedDailyCases = 3,
        previouslyReportedTotalCases = 5
    )

    val TEST_LTAS_CASE_MODEL = CaseModel(
        areaName = "Test LTAS",
        areaCode = "TL001",
        changeInDailyCases = 1,
        changeInTotalCases = 2,
        dailyLabConfirmedCases = 3,
        dailyTotalLabConfirmedCasesRate = 40.0,
        specimenDate = Date(0),
        totalLabConfirmedCases = 3,
        previouslyReportedDailyCases = 3,
        previouslyReportedTotalCases = 5
    )

    val TEST_UTLAS_CASE_MODEL = CaseModel(
        areaName = "Test UTLAS",
        areaCode = "TU001",
        changeInDailyCases = 1,
        changeInTotalCases = 2,
        dailyLabConfirmedCases = 3,
        dailyTotalLabConfirmedCasesRate = 40.0,
        specimenDate = Date(0),
        totalLabConfirmedCases = 3,
        previouslyReportedDailyCases = 3,
        previouslyReportedTotalCases = 5
    )

    val TEST_REGION_MODEL = CaseModel(
        areaName = "Test Region",
        areaCode = "TR001",
        changeInDailyCases = 1,
        changeInTotalCases = 2,
        dailyLabConfirmedCases = 3,
        dailyTotalLabConfirmedCasesRate = 40.0,
        specimenDate = Date(0),
        totalLabConfirmedCases = 3,
        previouslyReportedDailyCases = 3,
        previouslyReportedTotalCases = 5
    )

    val TEST_DAILY_RECORD_MODEL = DailyRecordModel(
        areaName = "Test United Kingdom",
        totalLabConfirmedCases = 0,
        dailyLabConfirmedCases = 0
    )

    val TEST_CASE_METADATA = MetadataModel(
        disclaimer = "Test disclaimer",
        lastUpdatedAt = Date(0)
    )

    val TEST_CASE_MODEL = CasesModel(
        countries = listOf(TEST_COUNTRY_CASE_MODEL),
        ltlas = listOf(TEST_LTAS_CASE_MODEL),
        utlas = listOf(TEST_UTLAS_CASE_MODEL),
        regions = listOf(TEST_REGION_MODEL),
        dailyRecords = TEST_DAILY_RECORD_MODEL,
        metadata = TEST_CASE_METADATA
    )

    val TEST_COUNTRY_DEATH_MODEL = DeathModel(
        areaCode = "TC001",
        areaName = "Test Country",
        cumulativeDeaths = 0,
        dailyChangeInDeaths = 0,
        reportingDate = Date(0)
    )

    val TEST_OVERVIEW_DEATH_MODEL = DeathModel(
        areaCode = "TC001",
        areaName = "Test Overviews",
        cumulativeDeaths = 0,
        dailyChangeInDeaths = 0,
        reportingDate = Date(0)
    )

    val TEST_DEATH_METADATA = MetadataModel(
        disclaimer = "Test death disclaimer",
        lastUpdatedAt = Date(0)
    )

    val TEST_DEATH_MODEL = DeathsModel(
        countries = listOf(TEST_COUNTRY_DEATH_MODEL),
        metadata = TEST_DEATH_METADATA,
        overview = listOf(TEST_OVERVIEW_DEATH_MODEL)
    )
}
