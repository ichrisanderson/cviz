package com.chrisa.covid19.features.area.domain.helper

import com.chrisa.covid19.features.area.data.dtos.CaseDto
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class RollingAverageHelper @Inject constructor() {
    fun average(currentCase: CaseDto, previousCase: CaseDto?): Double {
        if (previousCase == null) return 0.0
        val days = ChronoUnit.DAYS.between(previousCase.date, currentCase.date)
        return (currentCase.totalLabConfirmedCases - previousCase.totalLabConfirmedCases) / (days + 1).toDouble()
    }
}
