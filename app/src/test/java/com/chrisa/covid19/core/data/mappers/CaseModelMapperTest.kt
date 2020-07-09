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

package com.chrisa.covid19.core.data.mappers

import com.chrisa.covid19.core.data.db.CaseEntity
import com.chrisa.covid19.core.data.network.CaseModel
import com.google.common.truth.Truth.assertThat
import java.util.Date
import org.junit.Test

class CaseModelMapperTest {

    private val sut = CaseModelMapper()

    @Test
    fun `GIVEN no null fields WHEN mapToCasesEntity called THEN casesEntity is returned with correct values`() {

        val caseModel = CaseModel(
            areaCode = "001",
            areaName = "UK",
            specimenDate = Date(0),
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33,
            dailyTotalLabConfirmedCasesRate = 100.0,
            changeInDailyCases = 11,
            changeInTotalCases = 22,
            previouslyReportedTotalCases = 33,
            previouslyReportedDailyCases = 44
        )

        val entity = sut.mapToCasesEntity(caseModel)

        assertThat(entity).isEqualTo(
            CaseEntity(
                areaCode = caseModel.areaCode,
                areaName = caseModel.areaName,
                date = caseModel.specimenDate,
                dailyLabConfirmedCases = caseModel.dailyLabConfirmedCases!!,
                dailyTotalLabConfirmedCasesRate = caseModel.dailyTotalLabConfirmedCasesRate!!,
                totalLabConfirmedCases = caseModel.totalLabConfirmedCases!!
            )
        )
    }

    @Test
    fun `GIVEN dailyLabConfirmedCases is null WHEN mapToCasesEntity called THEN casesEntity is returned with 0 dailyLabConfirmedCases`() {

        val caseModel = CaseModel(
            areaCode = "001",
            areaName = "UK",
            specimenDate = Date(0),
            dailyLabConfirmedCases = null,
            totalLabConfirmedCases = 33,
            dailyTotalLabConfirmedCasesRate = 222.0,
            changeInDailyCases = 11,
            changeInTotalCases = 22,
            previouslyReportedTotalCases = 33,
            previouslyReportedDailyCases = 44
        )

        val entity = sut.mapToCasesEntity(caseModel)

        assertThat(entity).isEqualTo(
            CaseEntity(
                areaCode = caseModel.areaCode,
                areaName = caseModel.areaName,
                date = caseModel.specimenDate,
                dailyLabConfirmedCases = 0,
                dailyTotalLabConfirmedCasesRate = caseModel.dailyTotalLabConfirmedCasesRate!!,
                totalLabConfirmedCases = caseModel.totalLabConfirmedCases!!
            )
        )
    }

    @Test
    fun `GIVEN dailyTotalLabConfirmedCasesRate is null WHEN mapToCasesEntity called THEN casesEntity is returned with 0 dailyTotalLabConfirmedCasesRate`() {

        val caseModel = CaseModel(
            areaCode = "001",
            areaName = "UK",
            specimenDate = Date(0),
            dailyLabConfirmedCases = null,
            totalLabConfirmedCases = 33,
            dailyTotalLabConfirmedCasesRate = null,
            changeInDailyCases = 11,
            changeInTotalCases = 22,
            previouslyReportedTotalCases = 33,
            previouslyReportedDailyCases = 44
        )

        val entity = sut.mapToCasesEntity(caseModel)

        assertThat(entity).isEqualTo(
            CaseEntity(
                areaCode = caseModel.areaCode,
                areaName = caseModel.areaName,
                date = caseModel.specimenDate,
                dailyLabConfirmedCases = 0,
                dailyTotalLabConfirmedCasesRate = 0.0,
                totalLabConfirmedCases = caseModel.totalLabConfirmedCases!!
            )
        )
    }

    @Test
    fun `GIVEN totalLabConfirmedCases is null WHEN mapToCasesEntity called THEN casesEntity is returned with 0 totalLabConfirmedCases`() {

        val caseModel = CaseModel(
            areaCode = "001",
            areaName = "UK",
            specimenDate = Date(0),
            dailyLabConfirmedCases = null,
            totalLabConfirmedCases = null,
            dailyTotalLabConfirmedCasesRate = null,
            changeInDailyCases = 11,
            changeInTotalCases = 22,
            previouslyReportedTotalCases = 33,
            previouslyReportedDailyCases = 44
        )

        val entity = sut.mapToCasesEntity(caseModel)

        assertThat(entity).isEqualTo(
            CaseEntity(
                areaCode = caseModel.areaCode,
                areaName = caseModel.areaName,
                date = caseModel.specimenDate,
                dailyLabConfirmedCases = 0,
                dailyTotalLabConfirmedCasesRate = 0.0,
                totalLabConfirmedCases = 0
            )
        )
    }
}
