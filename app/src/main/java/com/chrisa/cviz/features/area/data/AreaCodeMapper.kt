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

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.Constants
import com.chrisa.cviz.features.area.data.dtos.AreaDto
import javax.inject.Inject

class AreaCodeMapper @Inject constructor() {

    fun defaultAreaDto(areaCode: String): AreaDto {
        return when {
            areaCode.startsWith("S") ->
                AreaDto(Constants.SCOTLAND_AREA_CODE, "Scotland", AreaType.NATION)
            areaCode.startsWith("W") ->
                AreaDto(Constants.WALES_AREA_CODE, "Wales", AreaType.NATION)
            areaCode.startsWith("E") ->
                AreaDto(Constants.ENGLAND_AREA_CODE, "England", AreaType.NATION)
            areaCode.startsWith("N") ->
                AreaDto(Constants.NORTHERN_IRELAND_AREA_CODE, "Northern Ireland", AreaType.NATION)
            else ->
                AreaDto(Constants.UK_AREA_CODE, "United Kingdom", AreaType.OVERVIEW)
        }
    }
}
