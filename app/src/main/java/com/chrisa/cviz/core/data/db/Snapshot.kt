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

package com.chrisa.cviz.core.data.db

class Snapshot(
    private val savedAreaCodes: Set<String>,
    healthcareLookups: List<HealthcareLookupEntity>,
    areaLookups: List<AreaLookupEntity>
) {
    private val savedAreaLookups =
        areaLookups.filter { lookup ->
            savedAreaCodes.contains(lookup.msoaCode) ||
                savedAreaCodes.contains(lookup.ltlaCode) ||
                savedAreaCodes.contains(lookup.utlaCode) ||
                lookup.regionCode != null && savedAreaCodes.contains(lookup.regionCode)
        }

    val lsoaAreaCodes = savedAreaLookups.map { it.lsoaCode }.toSet()

    val msoaAreaCodes =
        savedAreaLookups.filter { savedAreaCodes.contains(it.msoaCode) }.map { it.msoaCode }.toSet()

    private val localAreaDataCodes =
        mutableSetOf<String>()
            .plus(savedAreaLookups.filter {
                savedAreaCodes.contains(it.utlaCode) || savedAreaCodes.contains(it.msoaCode)
            }.map { it.utlaCode })
            .plus(savedAreaLookups.filter {
                savedAreaCodes.contains(it.ltlaCode) || savedAreaCodes.contains(it.msoaCode)
            }.map { it.ltlaCode })
            .plus(savedAreaLookups.mapNotNull { it.regionCode })
            .toSet()

    val localAndNationalAreaDataCodes =
        localAreaDataCodes
            .plus(
                listOf(
                    Constants.UK_AREA_CODE,
                    Constants.ENGLAND_AREA_CODE,
                    Constants.NORTHERN_IRELAND_AREA_CODE,
                    Constants.SCOTLAND_AREA_CODE,
                    Constants.WALES_AREA_CODE
                )
            )
            .toSet()

    private val healthcareLookupCodes =
        healthcareLookups.filter { localAndNationalAreaDataCodes.contains(it.areaCode) }
            .map { it.nhsTrustCode }

    val healthcareAreaCodes =
        savedAreaLookups.asSequence()
            .mapNotNull { it.regionCode }
            .plus(savedAreaLookups.mapNotNull { it.nhsTrustCode })
            .plus(savedAreaLookups.mapNotNull { it.nhsRegionCode })
            .plus(savedAreaLookups.map { it.nationCode })
            .plus(healthcareLookupCodes)
            .toSet()

    val alertLevelAreaCodes = savedAreaLookups.map { it.utlaCode }
        .plus(savedAreaLookups.map { it.ltlaCode })
        .plus(savedAreaLookups.mapNotNull { it.regionCode })
        .toSet()

    val metadataIds =
        localAndNationalAreaDataCodes.map(MetaDataIds::areaCodeId)
            .plus(msoaAreaCodes.map(MetaDataIds::areaCodeId))
            .plus(MetaDataIds.areaSummaryId())
            .plus(healthcareAreaCodes.map(MetaDataIds::healthcareId))
}
