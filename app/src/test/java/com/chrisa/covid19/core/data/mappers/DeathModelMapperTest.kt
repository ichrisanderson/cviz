package com.chrisa.covid19.core.data.mappers

import com.chrisa.covid19.core.data.db.DeathEntity
import com.chrisa.covid19.core.data.network.DeathModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.*

class DeathModelMapperTest {

    val sut = DeathModelMapper()

    @Test
    fun `GIVEN no null fields WHEN mapToDeathsEntity called THEN deathsEntity returned`() {

        val deathModel = DeathModel(
            areaCode = "001",
            areaName = "UK",
            reportingDate = Date(0),
            cumulativeDeaths = 110,
            dailyChangeInDeaths = 222
        )

        val entity = sut.mapToDeathsEntity(deathModel)

        assertThat(entity).isEqualTo(
            DeathEntity(
                areaCode = deathModel.areaCode,
                areaName = deathModel.areaName,
                date = deathModel.reportingDate,
                cumulativeDeaths = deathModel.cumulativeDeaths,
                dailyChangeInDeaths = deathModel.dailyChangeInDeaths!!
            )
        )
    }

    @Test
    fun `GIVEN dailyChangeInDeaths is null WHEN mapToDeathsEntity called THEN deathsEntity returned with 0 dailyChangeInDeaths`() {

        val deathModel = DeathModel(
            areaCode = "001",
            areaName = "UK",
            reportingDate = Date(0),
            cumulativeDeaths = 110,
            dailyChangeInDeaths = null
        )

        val entity = sut.mapToDeathsEntity(deathModel)

        assertThat(entity).isEqualTo(
            DeathEntity(
                areaCode = deathModel.areaCode,
                areaName = deathModel.areaName,
                date = deathModel.reportingDate,
                cumulativeDeaths = deathModel.cumulativeDeaths,
                dailyChangeInDeaths = 0
            )
        )
    }
}
