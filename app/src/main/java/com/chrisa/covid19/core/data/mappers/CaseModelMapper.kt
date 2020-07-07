package com.chrisa.covid19.core.data.mappers

import com.chrisa.covid19.core.data.db.CaseEntity
import com.chrisa.covid19.core.data.network.CaseModel
import javax.inject.Inject

class CaseModelMapper @Inject constructor() {

    fun mapToCasesEntity(caseModel: CaseModel): CaseEntity {
        return CaseEntity(
            areaCode = caseModel.areaCode,
            areaName = caseModel.areaName,
            date = caseModel.specimenDate,
            dailyLabConfirmedCases = caseModel.dailyLabConfirmedCases ?: 0,
            dailyTotalLabConfirmedCasesRate = caseModel.dailyTotalLabConfirmedCasesRate ?: 0.0,
            totalLabConfirmedCases = caseModel.totalLabConfirmedCases
        )
    }
}

