package com.chrisa.covid19.core.data.mappers

import com.chrisa.covid19.core.data.db.CaseEntity
import com.chrisa.covid19.core.data.network.CaseModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.*

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
                totalLabConfirmedCases = caseModel.totalLabConfirmedCases
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
                totalLabConfirmedCases = caseModel.totalLabConfirmedCases
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
                totalLabConfirmedCases = caseModel.totalLabConfirmedCases
            )
        )
    }
}
