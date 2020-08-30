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

package com.chrisa.covid19.core.data.network

import com.chrisa.covid19.core.data.db.AreaType
import com.chrisa.covid19.core.data.network.AreaDataModel.Companion.AREA_DATA_MODEL_BY_PUBLISH_DATE_STRUCTURE
import com.chrisa.covid19.core.data.network.AreaDataModel.Companion.AREA_DATA_MODEL_BY_SPECIMEN_DATE_STRUCTURE
import javax.inject.Inject

class AreaDataModelStructureMapper @Inject constructor() {
    fun mapAreaTypeToDataModel(areaType: AreaType): String {
        return when (areaType) {
            AreaType.OVERVIEW, AreaType.NATION -> AREA_DATA_MODEL_BY_PUBLISH_DATE_STRUCTURE
            else -> AREA_DATA_MODEL_BY_SPECIMEN_DATE_STRUCTURE
        }
    }
}
