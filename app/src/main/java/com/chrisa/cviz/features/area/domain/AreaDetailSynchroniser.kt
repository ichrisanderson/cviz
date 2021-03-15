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

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.features.area.data.dtos.AreaAssociationTypeDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import com.chrisa.cviz.features.area.domain.AlertLevelUseCase.Companion.supportsAlertLevel
import com.chrisa.cviz.features.area.domain.arealookup.AreaLookupCode
import com.chrisa.cviz.features.area.domain.arealookup.AreaLookupCodeResolver
import com.chrisa.cviz.features.area.domain.healthcare.HealthcareDataSynchroniserFacade
import javax.inject.Inject

class AreaDetailSynchroniser @Inject constructor(
    private val areaLookupUseCase: AreaLookupUseCase,
    private val alertLevelUseCase: AlertLevelUseCase,
    private val insertAreaAssociationUseCase: InsertAreaAssociationUseCase,
    private val areaLookupCodeResolver: AreaLookupCodeResolver,
    private val areaDataSynchroniser: AreaDataSynchroniserWrapper,
    private val healthcareDataSynchroniser: HealthcareDataSynchroniserFacade

) {
    suspend fun performSync(
        areaCode: String,
        areaType: AreaType
    ) {
        val areaLookup = areaLookupUseCase.areaLookup(areaCode, areaType)
        insertAreaLookupAssociation(areaCode, areaLookup)
        val areaLookupCode = areaLookupCodeResolver.areaLookupCode(areaCode, areaType, areaLookup)
        insertAreaDataAssociation(areaCode, areaLookupCode)
        syncAreaLookup(areaCode, areaLookup, areaLookupCode)
    }

    private suspend fun syncAreaLookup(
        areaCode: String,
        areaLookup: AreaLookupDto?,
        areaLookupCode: AreaLookupCode
    ) {
        if (areaLookupCode.areaType.supportsAlertLevel()) {
            alertLevelUseCase.syncAlertLevel(areaLookupCode.areaCode, areaLookupCode.areaType)
            insertAreaAssociationUseCase.execute(
                areaCode,
                areaLookupCode.areaCode,
                AreaAssociationTypeDto.ALERT_LEVEL
            )
        }
        areaDataSynchroniser.execute(areaLookupCode.areaCode, areaLookupCode.areaType)
        healthcareDataSynchroniser.syncHealthcare(
            areaCode,
            areaLookupCode.areaCode,
            areaLookupCode.areaType,
            areaLookup
        )
    }

    private fun insertAreaDataAssociation(
        areaCode: String,
        areaLookupCode: AreaLookupCode
    ) =
        insertAreaAssociationUseCase.execute(
            areaCode,
            areaLookupCode.areaCode,
            AreaAssociationTypeDto.AREA_DATA
        )

    private fun insertAreaLookupAssociation(
        areaCode: String,
        areaLookup: AreaLookupDto?
    ) =
        areaLookup?.let {
            insertAreaAssociationUseCase.execute(
                areaCode,
                areaLookup.lsoaCode,
                AreaAssociationTypeDto.AREA_LOOKUP
            )
        }
}
