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

    fun alertLevel(areaCode: String, areaType: AreaType): AlertLevelModel? {
        val alertLevel = alertLevelDataSource.alertLevel(areaCode)
        return alertLevel?.let {
            AlertLevelModel(
                alertLevelUrl = alertLevel.alertLevelUrl
            )
        } ?: defaultAlertLevel(areaCode, areaType)
    }

    private fun defaultAlertLevel(areaCode: String, areaType: AreaType): AlertLevelModel? {
        if (!canDisplayAlertLevel(areaType)) return null
        return when {
            areaCode.startsWith("S") ->
                AlertLevelModel(SCOTLAND_RESTRICTIONS_URL)
            areaCode.startsWith("W") ->
                AlertLevelModel(WALES_RESTRICTIONS_URL)
            areaCode.startsWith("E") ->
                AlertLevelModel(UK_RESTRICTIONS_URL)
            areaCode.startsWith("N") ->
                AlertLevelModel(NORTHERN_IRELAND_RESTRICTIONS_URL)
            else -> null
        }
    }

    private fun canDisplayAlertLevel(areaType: AreaType): Boolean =
        when (areaType) {
            AreaType.UTLA,
            AreaType.LTLA -> true
            else -> false
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

    companion object {
        const val UK_RESTRICTIONS_URL =
            "https://www.gov.uk/coronavirus"
        const val WALES_RESTRICTIONS_URL =
            "https://gov.wales/coronavirus"
        const val SCOTLAND_RESTRICTIONS_URL =
            "https://www.gov.scot/coronavirus-covid-19/"
        const val NORTHERN_IRELAND_RESTRICTIONS_URL =
            "https://www.nidirect.gov.uk/campaigns/coronavirus-covid-19"
    }
}
