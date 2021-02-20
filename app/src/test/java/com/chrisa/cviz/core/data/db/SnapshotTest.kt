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

import com.google.common.truth.Truth.assertThat
import org.junit.Ignore
import org.junit.Test

@Ignore
class SnapshotTest {

    @Test
    fun `GIVEN lookup saved WHEN lsoaAreaCodes called THEN lsoa code emitted`() {
        val savedAreaCodes = setOf("msoa1", "msoa2")
        val savedAreaLookup = areaLookupEntity.copy(msoaCode = "msoa1", lsoaCode = "lsoa1")
        val snapshot = Snapshot(
            savedAreaCodes,
            emptyList(),
            listOf(areaLookupEntity, savedAreaLookup)
        )

        val areaCodes = snapshot.lsoaAreaCodes

        assertThat(areaCodes).isEqualTo(setOf("lsoa1"))
    }

    @Test
    fun `GIVEN lookup saved WHEN msoaAreaCodes called THEN msoa code emitted`() {
        val savedAreaCodes = setOf("msoa1", "msoa2")
        val savedAreaLookup = areaLookupEntity.copy(msoaCode = "msoa2")
        val snapshot = Snapshot(
            savedAreaCodes,
            emptyList(),
            listOf(areaLookupEntity, savedAreaLookup)
        )

        val areaCodes = snapshot.msoaAreaCodes

        assertThat(areaCodes).isEqualTo(setOf("msoa2"))
    }

    @Test
    fun `GIVEN lookup saved WHEN localAndNationalAreaDataCodes called THEN lookup area codes AND national area codes emitted`() {
        val savedAreaCodes = setOf("utla1", "msoa2")
        val savedAreaLookup =
            areaLookupEntity.copy(utlaCode = "utla1", ltlaCode = "ltla1", regionCode = "region1")
        val snapshot = Snapshot(
            savedAreaCodes,
            emptyList(),
            listOf(areaLookupEntity, savedAreaLookup)
        )

        val areaCodes = snapshot.localAndNationalAreaDataCodes

        assertThat(areaCodes).isEqualTo(setOf("utla1", "ltla1", "region1").plus(nationalCodes))
    }

    @Test
    fun `GIVEN lookup saved WHEN healthcareAreaCodes called THEN lookup healthcare area codes AND linked healthcare lookup area codes emitted`() {
        val savedAreaCodes = setOf("utla1", "utla2")
        val savedAreaLookup = areaLookupEntity.copy(
            utlaCode = "utla1",
            ltlaCode = "ltla1",
            regionCode = "region1",
            nhsTrustCode = "nhsTrust5",
            nhsRegionCode = "nhsRegion1",
            nationCode = "nation1"
        )
        val healthcareLookups = listOf(
            HealthcareLookupEntity(
                "utla1",
                "nhsTrust1"
            ),
            HealthcareLookupEntity(
                "utla1",
                "nhsTrust2"
            ),
            HealthcareLookupEntity(
                "utla2",
                "nhsTrust3"
            )
        )
        val snapshot = Snapshot(
            savedAreaCodes,
            healthcareLookups,
            listOf(areaLookupEntity, savedAreaLookup)
        )

        val areaCodes = snapshot.healthcareAreaCodes

        assertThat(areaCodes).isEqualTo(
            setOf(
                "region1",
                "nhsTrust5",
                "nhsRegion1",
                "nation1"
            ).plus(listOf("nhsTrust1", "nhsTrust2"))
        )
    }

    @Test
    fun `GIVEN lookup saved WHEN alertLevelAreaCodes called THEN lookup area codes emitted`() {
        val savedAreaCodes = setOf("utla1", "msoa2")
        val savedAreaLookup =
            areaLookupEntity.copy(utlaCode = "utla1", ltlaCode = "ltla1", regionCode = "region1")
        val snapshot = Snapshot(
            savedAreaCodes,
            emptyList(),
            listOf(areaLookupEntity, savedAreaLookup)
        )

        val areaCodes = snapshot.alertLevelAreaCodes

        assertThat(areaCodes).isEqualTo(setOf("utla1", "ltla1", "region1"))
    }

    @Test
    @Ignore
    fun `GIVEN lookup saved WHEN metadataIds called THEN area metadata AND msoa metadata AND area summary metadata AND healthcare metadata emitted`() {
        val savedAreaCodes = setOf("utla1", "utla2")
        val savedAreaLookup = areaLookupEntity.copy(
            msoaCode = "msoa1",
            utlaCode = "utla1",
            ltlaCode = "ltla1",
            regionCode = "region1",
            nhsTrustCode = "nhsTrust5",
            nhsRegionCode = "nhsRegion1",
            nationCode = "nation1"
        )
        val healthcareLookups = listOf(
            HealthcareLookupEntity(
                "utla1",
                "nhsTrust1"
            ),
            HealthcareLookupEntity(
                "utla1",
                "nhsTrust2"
            ),
            HealthcareLookupEntity(
                "utla2",
                "nhsTrust3"
            )
        )
        val snapshot = Snapshot(
            savedAreaCodes,
            healthcareLookups,
            listOf(areaLookupEntity, savedAreaLookup)
        )

        val metadataIds = snapshot.metadataIds

        assertThat(metadataIds).isEqualTo(
            listOf("utla1", "ltla1", "region1").map(MetaDataIds::areaCodeId)
                .plus(nationalCodes.map(MetaDataIds::areaCodeId))
                .plus(listOf("msoa1").map(MetaDataIds::areaCodeId))
                .plus(MetaDataIds.areaSummaryId())
                .plus(
                    listOf(
                        "region1",
                        "nhsTrust5",
                        "nhsRegion1",
                        "nation1",
                        "nhsTrust1",
                        "nhsTrust2"
                    ).map(MetaDataIds::healthcareId)
                )
        )
    }

    companion object {
        val areaLookupEntity = AreaLookupEntity(
            postcode = "",
            trimmedPostcode = "",
            lsoaCode = "",
            lsoaName = null,
            msoaCode = "",
            msoaName = null,
            ltlaCode = "",
            ltlaName = "",
            utlaCode = "",
            utlaName = "",
            nhsTrustCode = null,
            nhsTrustName = null,
            nhsRegionCode = null,
            nhsRegionName = null,
            regionCode = null,
            regionName = null,
            nationCode = "",
            nationName = ""
        )
        val nationalCodes = listOf(
            Constants.UK_AREA_CODE,
            Constants.ENGLAND_AREA_CODE,
            Constants.NORTHERN_IRELAND_AREA_CODE,
            Constants.SCOTLAND_AREA_CODE,
            Constants.WALES_AREA_CODE
        )
    }
}
