package com.chrisa.covid19.core.data.mappers

import com.chrisa.covid19.core.data.db.DailyRecordEntity
import com.chrisa.covid19.core.data.network.DailyRecordModel
import java.util.Date
import javax.inject.Inject

class DailyRecordModelMapper @Inject constructor() {

    fun mapToDailyRecordsEntity(
        dailyRecord: DailyRecordModel,
        date: Date
    ): DailyRecordEntity {
        return DailyRecordEntity(
            areaName = dailyRecord.areaName,
            dailyLabConfirmedCases = dailyRecord.dailyLabConfirmedCases,
            totalLabConfirmedCases = dailyRecord.totalLabConfirmedCases ?: 0,
            date = date
        )
    }
}
