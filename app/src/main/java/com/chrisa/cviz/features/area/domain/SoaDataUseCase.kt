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

package com.chrisa.cviz.features.area.domain

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.synchronisation.SoaDataSynchroniser
import com.chrisa.cviz.features.area.data.SoaDataSource
import com.chrisa.cviz.features.area.domain.models.SoaDataModel
import javax.inject.Inject
import timber.log.Timber

class SoaDataUseCase @Inject constructor(
    private val soaDataSource: SoaDataSource,
    private val soaDataSynchroniser: SoaDataSynchroniser
) {
    fun byAreaCode(areaCode: String, areaType: AreaType): SoaDataModel? =
        if (supportsSoaData(areaType)) {
            areaData(areaCode)
        } else {
            null
        }

    suspend fun syncSoaData(
        areaCode: String,
        areaType: AreaType
    ) {
        if (supportsSoaData(areaType)) {
            doSync(areaCode)
        }
    }

    private fun supportsSoaData(
        areaType: AreaType
    ) =
        areaType == AreaType.MSOA

    private suspend fun doSync(areaCode: String) =
        try {
            soaDataSynchroniser.performSync(areaCode)
        } catch (e: Throwable) {
            Timber.e(e)
        }

    private fun areaData(areaCode: String) =
        soaDataSource.byAreaCode(areaCode)?.let { soaData ->
            SoaDataModel(
                soaData.areaCode,
                soaData.areaName,
                soaData.areaType,
                soaData.date,
                soaData.rollingSum,
                soaData.rollingRate,
                soaData.change,
                soaData.changePercentage
            )
        }
}
