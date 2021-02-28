package com.chrisa.cviz.features.area.domain

import com.chrisa.cviz.features.area.data.AreaAssociationDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaAssociationTypeDto
import javax.inject.Inject

class InsertAreaAssociationUseCase @Inject constructor(
    private val areaAssociationDataSource: AreaAssociationDataSource
) {
    fun execute(
        areaCode: String,
        associatedAreaCode: String,
        areaAssociationType: AreaAssociationTypeDto
    ) =
        areaAssociationDataSource.insert(
            areaCode,
            associatedAreaCode,
            areaAssociationType
        )
}
