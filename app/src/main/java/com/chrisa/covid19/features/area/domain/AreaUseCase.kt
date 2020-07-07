package com.chrisa.covid19.features.area.domain

import com.chrisa.covid19.features.area.data.AreaDataSource
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import javax.inject.Inject

class AreaUseCase @Inject constructor(
    private val areaDataSource: AreaDataSource
) {
    fun execute(areaCode: String): AreaDetailModel {

        val metadata = areaDataSource.loadCaseMetadata()

        val allCases = areaDataSource.loadCases(areaCode).map {
            CaseModel(
                dailyLabConfirmedCases = it.dailyLabConfirmedCases,
                date = it.date
            )
        }

        return AreaDetailModel(
            lastUpdatedAt = metadata.lastUpdatedAt,
            allCases = allCases,
            latestCases = allCases.takeLast(7)
        )
    }
}

