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

import androidx.room.withTransaction
import javax.inject.Inject

class UnusedDataCleaner @Inject constructor(
    private val appDatabase: AppDatabase
) {
    suspend fun execute() =
        appDatabase.withTransaction {
            val allSavedAreas = appDatabase.areaDao().allSavedAreas()
            val allSavedAreaCodes = allSavedAreas.map { it.areaCode }

            val allSoaAreaMetadataCodes = allSavedAreas
                .filter { it.areaType == AreaType.MSOA }
                .map { MetadataIds.areaCodeId(it.areaCode) }

            val allNonMsoaAreaCodes = allSavedAreas
                .filterNot { it.areaType == AreaType.MSOA }
                .map { it.areaCode }

            val allAssociations =
                appDatabase.areaAssociationDao()
                    .inAreaCode(allSavedAreaCodes)

            val allAreaDataCodes =
                allAssociations.asSequence()
                    .filter { it.associatedAreaType == AreaAssociationType.AREA_DATA }
                    .map { it.associatedAreaCode }
                    .plus(allNonMsoaAreaCodes)
                    .plus(nationCodes)
                    .distinct()
                    .map { MetadataIds.areaCodeId(it) }.toList()

            val allAlertLevelCodes =
                allAssociations.asSequence()
                    .filter { it.associatedAreaType == AreaAssociationType.ALERT_LEVEL }
                    .map { it.associatedAreaCode }
                    .distinct()
                    .map { MetadataIds.alertLevelId(it) }.toList()

            val allHealthcareCodes =
                allAssociations.filter { it.associatedAreaType == AreaAssociationType.HEALTHCARE_DATA }
                    .map { it.associatedAreaCode }
                    .distinct()
                    .map { MetadataIds.healthcareId(it) }

            val allMetadataIds =
                allSoaAreaMetadataCodes
                    .plus(allAreaDataCodes)
                    .plus(allHealthcareCodes)
                    .plus(allAlertLevelCodes)
                    .plus(MetadataIds.areaSummaryId())

            val allLsoaCodes =
                allAssociations.filter { it.associatedAreaType == AreaAssociationType.AREA_LOOKUP }
                    .map { it.associatedAreaCode }
                    .distinct()

            appDatabase.metadataDao().deleteAllNotInId(allMetadataIds)
            appDatabase.areaLookupDao().deleteAllNotInLsoaCode(allLsoaCodes)
        }

    companion object {
        val nationCodes = listOf(
            Constants.UK_AREA_CODE,
            Constants.ENGLAND_AREA_CODE,
            Constants.NORTHERN_IRELAND_AREA_CODE,
            Constants.SCOTLAND_AREA_CODE,
            Constants.WALES_AREA_CODE
        )
    }
}
