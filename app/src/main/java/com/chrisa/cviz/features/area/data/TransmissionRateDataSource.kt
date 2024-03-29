/*
 * Copyright 2020 Chris Anderson.
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

package com.chrisa.cviz.features.area.data

import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.HealthcareEntity
import com.chrisa.cviz.core.data.db.MetadataIds
import com.chrisa.cviz.core.data.time.TimeProvider
import com.chrisa.cviz.features.area.data.dtos.MetadataDto
import com.chrisa.cviz.features.area.data.dtos.TransmissionRateDto
import javax.inject.Inject

class TransmissionRateDataSource @Inject constructor(
    private val appDatabase: AppDatabase,
    private val timeProvider: TimeProvider
) {

    fun transmissionRate(areaCode: String): TransmissionRateDto? {
        val allHealthcareData = allHealthcareData(areaCode)
        val cutOffDate = timeProvider.currentDate().minusDays(15)
        return allHealthcareData
            .filter(::hasTransmissionRate)
            .sortedByDescending { it.date }
            .map(::mapTransmissionRate)
            .firstOrNull { it.date.isAfter(cutOffDate) }
    }

    fun healthcareMetaData(areaCode: String): MetadataDto? =
        appDatabase.metadataDao().metadata(MetadataIds.healthcareId(areaCode))?.let {
            MetadataDto(it.lastUpdatedAt, it.lastSyncTime)
        }

    private fun allHealthcareData(areaCode: String) =
        appDatabase.healthcareDao().byAreaCode(areaCode)

    private fun hasTransmissionRate(it: HealthcareEntity): Boolean =
        it.transmissionRateMin != null && it.transmissionRateMax != null &&
            it.transmissionRateGrowthRateMin != null && it.transmissionRateGrowthRateMax != null

    private fun mapTransmissionRate(areaData: HealthcareEntity): TransmissionRateDto {
        return TransmissionRateDto(
            date = areaData.date,
            transmissionRateMin = areaData.transmissionRateMin!!,
            transmissionRateMax = areaData.transmissionRateMax!!,
            transmissionRateGrowthRateMin = areaData.transmissionRateGrowthRateMin!!,
            transmissionRateGrowthRateMax = areaData.transmissionRateGrowthRateMax!!
        )
    }
}
