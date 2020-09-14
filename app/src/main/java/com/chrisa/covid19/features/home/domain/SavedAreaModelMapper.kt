package com.chrisa.covid19.features.home.domain

import com.chrisa.covid19.features.home.data.dtos.SavedAreaCaseDto
import com.chrisa.covid19.features.home.domain.models.SavedAreaModel
import javax.inject.Inject

class SavedAreaModelMapper @Inject() constructor() {

    fun mapSavedAreaModel(
        areaCode: String,
        areaName: String,
        allCases: List<SavedAreaCaseDto>
    ): SavedAreaModel {

        val offset = 3

        val lastCase = allCases.getOrNull(allCases.size - offset)
        val prevCase = allCases.getOrNull(allCases.size - (offset + 7))
        val prevCase1 = allCases.getOrNull(allCases.size - (offset + 14))

        val lastTotalLabConfirmedCases = lastCase?.totalLabConfirmedCases ?: 0
        val prevTotalLabConfirmedCases = prevCase?.totalLabConfirmedCases ?: 0
        val prev1TotalLabConfirmedCases = prevCase1?.totalLabConfirmedCases ?: 0

        val casesThisWeek = (lastTotalLabConfirmedCases - prevTotalLabConfirmedCases)
        val casesLastWeek = (prevTotalLabConfirmedCases - prev1TotalLabConfirmedCases)

        return SavedAreaModel(
            areaCode = areaCode,
            areaName = areaName,
            areaType = allCases.first().areaType,
            changeInTotalLabConfirmedCases = casesThisWeek - casesLastWeek,
            totalLabConfirmedCases = lastTotalLabConfirmedCases,
            totalLabConfirmedCasesLastWeek = casesThisWeek
        )
    }
}
