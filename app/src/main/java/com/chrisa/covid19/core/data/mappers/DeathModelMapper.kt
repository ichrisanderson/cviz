package com.chrisa.covid19.core.data.mappers

import com.chrisa.covid19.core.data.db.DeathEntity
import com.chrisa.covid19.core.data.network.DeathModel
import javax.inject.Inject

class DeathModelMapper @Inject constructor() {

    fun mapToDeathsEntity(deathModel: DeathModel): DeathEntity {
        return DeathEntity(
            areaCode = deathModel.areaCode,
            areaName = deathModel.areaName,
            date = deathModel.reportingDate,
            dailyChangeInDeaths = deathModel.dailyChangeInDeaths ?: 0,
            cumulativeDeaths = deathModel.cumulativeDeaths
        )
    }
}
