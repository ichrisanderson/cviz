package com.chrisa.covid19.features.area.data

import com.chrisa.covid19.core.data.db.AppDatabase
import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.features.area.data.dtos.CaseDTO
import com.chrisa.covid19.features.area.data.dtos.MetadataDTO
import javax.inject.Inject

class AreaDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) {
    fun loadCases(areaCode: String): List<CaseDTO> {
        return appDatabase.casesDao()
            .searchAllCases(areaCode).map {
                CaseDTO(dailyLabConfirmedCases = it.dailyLabConfirmedCases, date = it.date)
            }
    }

    fun loadCaseMetadata(): MetadataDTO {
        return appDatabase.metadataDao()
            .searchMetadata(MetadataEntity.CASE_METADATA_ID).map {
                MetadataDTO(lastUpdatedAt = it.lastUpdatedAt)
            }.first()
    }
}

