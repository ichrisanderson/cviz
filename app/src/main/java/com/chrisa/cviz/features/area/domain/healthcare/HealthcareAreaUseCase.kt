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

package com.chrisa.cviz.features.area.domain.healthcare

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.features.area.data.AreaCodeResolver
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import javax.inject.Inject

class HealthcareAreaUseCase @Inject constructor(
    private val areaCodeResolver: AreaCodeResolver
) {
    fun healthcareArea(
        areaCode: String,
        areaType: AreaType,
        areaLookup: AreaLookupDto?
    ): AreaDto {
        val nhsTrustCode = areaLookup?.nhsTrustCode
        val nhsRegionCode = areaLookup?.nhsRegionCode
        return if (canUseNhsTrust(areaType) && nhsTrustCode != null) {
            AreaDto(nhsTrustCode, areaLookup.nhsTrustName.orEmpty(), AreaType.NHS_TRUST)
        } else if (canUseNhsRegion(areaType) && nhsRegionCode != null) {
            AreaDto(nhsRegionCode, areaLookup.nhsRegionName.orEmpty(), AreaType.NHS_REGION)
        } else {
            areaCodeResolver.defaultAreaDto(areaCode)
        }
    }

    private fun canUseNhsTrust(areaType: AreaType) =
        nhsTrustAreaTypes.contains(areaType)

    private fun canUseNhsRegion(areaType: AreaType) =
        nhsRegionAreaTypes.contains(areaType)

    companion object {
        private val nhsTrustAreaTypes = setOf(AreaType.UTLA, AreaType.LTLA, AreaType.NHS_TRUST)
        private val nhsRegionAreaTypes =
            nhsTrustAreaTypes.plus(listOf(AreaType.NHS_REGION, AreaType.REGION))
    }
}
