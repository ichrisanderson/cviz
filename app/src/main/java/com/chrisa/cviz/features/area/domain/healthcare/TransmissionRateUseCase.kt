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

import com.chrisa.cviz.features.area.data.TransmissionRateDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import com.chrisa.cviz.features.area.domain.models.AreaTransmissionRateModel
import com.chrisa.cviz.features.area.domain.models.TransmissionRateModel
import javax.inject.Inject

class TransmissionRateUseCase @Inject constructor(
    private val transmissionRateDataSource: TransmissionRateDataSource
) {
    fun transmissionRate(
        nhsRegion: AreaDto
    ): AreaTransmissionRateModel? {
        val areaTransmissionRate = areaTransmissionRate(nhsRegion)
        return areaTransmissionRate?.let {
            val metadata = transmissionRateDataSource.healthcareMetaData(nhsRegion.code)
            AreaTransmissionRateModel(
                nhsRegion.name,
                metadata!!.lastUpdatedAt,
                areaTransmissionRate
            )
        }
    }

    private fun areaTransmissionRate(nhsRegion: AreaDto): TransmissionRateModel? =
        transmissionRateDataSource.transmissionRate(nhsRegion.code)?.let {
            TransmissionRateModel(
                it.date,
                it.transmissionRateMin,
                it.transmissionRateMax,
                it.transmissionRateGrowthRateMin,
                it.transmissionRateGrowthRateMax
            )
        }
}
