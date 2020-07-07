package com.chrisa.covid19.features.area.domain.models

import java.util.Date

data class AreaDetailModel(
    val lastUpdatedAt: Date,
    val allCases: List<CaseModel>,
    val latestCases: List<CaseModel>
)
