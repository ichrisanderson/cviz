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
import com.chrisa.cviz.core.data.db.AreaLookupEntity
import com.chrisa.cviz.features.area.data.dtos.AreaLookupDto
import javax.inject.Inject

class AreaLookupDataSource @Inject constructor(
    private val appDatabase: AppDatabase
) {

    fun areaLookupByLtla(areaCode: String): AreaLookupDto? =
        appDatabase.areaLookupDao().byLtla(areaCode)?.toAreaLookupDto()

    fun areaLookupByUtla(areaCode: String): AreaLookupDto? =
        appDatabase.areaLookupDao().byUtla(areaCode)?.toAreaLookupDto()

    fun areaLookupByRegion(areaCode: String): AreaLookupDto? =
        appDatabase.areaLookupDao().byRegion(areaCode)?.toAreaLookupDto()

    fun areaLookupByNhsRegion(areaCode: String): AreaLookupDto? =
        appDatabase.areaLookupDao().byNhsRegion(areaCode)?.toAreaLookupDto()
}

fun AreaLookupEntity.toAreaLookupDto(): AreaLookupDto? {
    return AreaLookupDto(
        lsoaCode = this.lsoaCode,
        lsoaName = this.lsoaName,
        msoaCode = this.msoaCode,
        msoaName = this.msoaName,
        ltlaName = this.ltlaName,
        ltlaCode = this.ltlaCode,
        utlaName = this.utlaName,
        utlaCode = this.utlaCode,
        nhsRegionName = this.nhsRegionName,
        nhsRegionCode = this.nhsRegionCode,
        regionCode = this.regionCode,
        regionName = this.regionName,
        nationName = this.nationName,
        nationCode = this.nationCode
    )
}
