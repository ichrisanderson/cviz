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
import com.chrisa.cviz.core.data.synchronisation.AlertLevelSynchroniser
import com.chrisa.cviz.features.area.data.AlertLevelDataSource
import com.chrisa.cviz.features.area.domain.models.AlertLevelModel
import javax.inject.Inject
import timber.log.Timber

class AlertLevelUseCase @Inject constructor(
    private val alertLevelDataSource: AlertLevelDataSource,
    private val alertLevelSynchroniser: AlertLevelSynchroniser
) {

    fun alertLevel(areaCode: String): AlertLevelModel? {
        val metadata = alertLevelDataSource.metadata(areaCode)
        return metadata?.let {
            val alertLevel = alertLevelDataSource.alertLevel(areaCode)
            alertLevel?.let {
                AlertLevelModel(
                    areaName = alertLevel.areaName,
                    date = alertLevel.date,
                    lastUpdatedAt = metadata.lastUpdatedAt,
                    alertLevelName = alertLevel.alertLevelName,
                    alertLevelUrl = alertLevel.alertLevelUrl
                )
            }
        }
    }

    suspend fun syncAlertLevel(areaCode: String, areaType: AreaType) =
        when (areaType) {
            AreaType.UTLA, AreaType.LTLA -> doSync(areaCode)
            else -> Unit
        }

    private suspend fun doSync(areaCode: String) =
        try {
            alertLevelSynchroniser.performSync(areaCode, AreaType.LTLA)
        } catch (error: Throwable) {
            Timber.e(error)
        }
}
