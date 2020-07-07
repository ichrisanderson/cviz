package com.chrisa.covid19.core.data.mappers

import com.chrisa.covid19.core.data.db.DailyRecordEntity
import com.chrisa.covid19.core.data.network.DailyRecordModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.*

class DailyRecordModelMapperTest {

    private val sut = DailyRecordModelMapper()

    @Test
    fun `WHEN mapToDailyRecordsEntity called THEN dailyRecordsEntity is returned`() {

        val lastUpdatedAt = Date(1)
        val dailyRecordModel = DailyRecordModel(
            areaName = "UK",
            dailyLabConfirmedCases = 12,
            totalLabConfirmedCases = 33
        )

        val entity = sut.mapToDailyRecordsEntity(dailyRecordModel, lastUpdatedAt)

        assertThat(entity).isEqualTo(
            DailyRecordEntity(
                areaName = dailyRecordModel.areaName,
                date = lastUpdatedAt,
                dailyLabConfirmedCases = dailyRecordModel.dailyLabConfirmedCases,
                totalLabConfirmedCases = dailyRecordModel.totalLabConfirmedCases!!
            )
        )
    }
}
