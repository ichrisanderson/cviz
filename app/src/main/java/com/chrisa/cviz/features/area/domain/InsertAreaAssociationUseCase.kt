/*
 * Copyright 2021 Chris Anderson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.chrisa.cviz.features.area.domain

import com.chrisa.cviz.core.data.db.AreaAssociationType
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
            areaAssociationType(areaAssociationType)
        )

    private fun areaAssociationType(associationType: AreaAssociationTypeDto) =
        when (associationType) {
            AreaAssociationTypeDto.AREA_DATA -> AreaAssociationType.AREA_DATA
            AreaAssociationTypeDto.AREA_LOOKUP -> AreaAssociationType.AREA_LOOKUP
            AreaAssociationTypeDto.HEALTHCARE_DATA -> AreaAssociationType.HEALTHCARE_DATA
        }
}
