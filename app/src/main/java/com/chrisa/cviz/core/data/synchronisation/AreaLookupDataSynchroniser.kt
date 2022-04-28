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

package com.chrisa.cviz.core.data.synchronisation

import androidx.room.withTransaction
import com.chrisa.cviz.core.data.db.AppDatabase
import com.chrisa.cviz.core.data.db.AreaLookupEntity
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.network.AreaLookupData
import com.chrisa.cviz.core.data.network.CovidApi
import com.chrisa.cviz.core.util.NetworkUtils
import java.io.IOException
import javax.inject.Inject

interface AreaLookupDataSynchroniser {
    suspend fun performSync(areaCode: String, areaType: AreaType)
}

internal class AreaLookupDataSynchroniserImpl @Inject constructor(
    private val api: CovidApi,
    private val appDatabase: AppDatabase,
    private val networkUtils: NetworkUtils
) : AreaLookupDataSynchroniser {

    override suspend fun performSync(
        areaCode: String,
        areaType: AreaType
    ) {
        if (!canLookupData(areaType)) return
        val lookupData = lookupData(areaCode, areaType)
        if (lookupData != null) { return }
        if (!networkUtils.hasNetworkConnection()) throw IOException()
        cacheAreaData(
            api.areaLookupData(category = areaType.value, search = areaCode)
        )
    }

    private fun canLookupData(areaType: AreaType): Boolean {
        return when (areaType) {
            AreaType.UTLA,
            AreaType.LTLA -> true
            else -> false
        }
    }

    private fun lookupData(areaCode: String, areaType: AreaType): AreaLookupEntity? {
        return when (areaType) {
            AreaType.UTLA -> appDatabase.areaLookupDao().byUtla(areaCode)
            AreaType.LTLA -> appDatabase.areaLookupDao().byLtla(areaCode)
            else -> null
        }
    }

    private suspend fun cacheAreaData(
        areaLookupData: AreaLookupData
    ) {
        appDatabase.withTransaction {
            appDatabase.areaLookupDao().insert(
                AreaLookupEntity(
                    postcode = areaLookupData.postcode.orEmpty(),
                    trimmedPostcode = areaLookupData.trimmedPostcode.orEmpty(),
                    lsoaCode = areaLookupData.lsoa.orEmpty(),
                    lsoaName = areaLookupData.lsoaName,
                    msoaName = areaLookupData.msoaName,
                    msoaCode = areaLookupData.msoa.orEmpty(),
                    ltlaCode = areaLookupData.ltla,
                    ltlaName = areaLookupData.ltlaName,
                    utlaCode = areaLookupData.utla,
                    utlaName = areaLookupData.utlaName,
                    nhsTrustCode = areaLookupData.nhsTrust,
                    nhsTrustName = areaLookupData.nhsTrustName,
                    nhsRegionCode = areaLookupData.nhsRegion,
                    nhsRegionName = areaLookupData.nhsRegionName,
                    regionCode = areaLookupData.region,
                    regionName = areaLookupData.regionName,
                    nationCode = areaLookupData.nation,
                    nationName = areaLookupData.nationName
                )
            )
        }
    }
}
