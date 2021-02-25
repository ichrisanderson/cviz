package com.chrisa.cviz.features.area.data

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaAssociation
import com.chrisa.cviz.core.data.db.AreaAssociationType
import com.chrisa.cviz.features.area.data.dtos.AreaAssociationTypeDto
import javax.inject.Inject

class AreaAssociationDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) {
    fun insert(
        areaCode: String,
        associatedAreaCode: String,
        associationType: AreaAssociationTypeDto
    ) {
        appDatabase.areaAssociationDao().insert(
            AreaAssociation(
                areaCode,
                associatedAreaCode,
                areaAssociationType(associationType)
            )
        )
    }

    private fun areaAssociationType(associationType: AreaAssociationTypeDto) =
        when (associationType) {
            AreaAssociationTypeDto.AREA_DATA -> AreaAssociationType.AREA_DATA
            AreaAssociationTypeDto.AREA_LOOKUP -> AreaAssociationType.AREA_LOOKUP
            AreaAssociationTypeDto.HEALTHCARE_DATA -> AreaAssociationType.HEALTHCARE_DATA
            AreaAssociationTypeDto.SOA_DATA -> AreaAssociationType.SOA_DATA
        }
}
