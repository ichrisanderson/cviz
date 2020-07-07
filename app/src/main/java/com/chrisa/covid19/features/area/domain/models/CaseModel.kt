package com.chrisa.covid19.features.area.domain.models

import java.util.Date

data class CaseModel(
    val dailyLabConfirmedCases: Int,
    val date: Date
)

