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

package com.chrisa.cviz.core.data.network

import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.network.AreaDataModel.Companion.AREA_DATA_MODEL_BY_PUBLISH_DATE_STRUCTURE
import com.chrisa.cviz.core.data.network.AreaDataModel.Companion.AREA_DATA_MODEL_BY_SPECIMEN_DATE_STRUCTURE
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AreaDataModelStructureMapperTest {

    private val sut = AreaDataModelStructureMapper()

    @Test
    fun `GIVEN overview type WHEN mapAreaTypeToDataModel THEN publish date model structure returned`() {

        val modelStructure = sut.mapAreaTypeToDataModel(AreaType.OVERVIEW)

        assertThat(modelStructure).isEqualTo(AREA_DATA_MODEL_BY_PUBLISH_DATE_STRUCTURE)
    }

    @Test
    fun `GIVEN nation type WHEN mapAreaTypeToDataModel THEN publish date model structure returned`() {

        val modelStructure = sut.mapAreaTypeToDataModel(AreaType.NATION)

        assertThat(modelStructure).isEqualTo(AREA_DATA_MODEL_BY_PUBLISH_DATE_STRUCTURE)
    }

    @Test
    fun `GIVEN region type WHEN mapAreaTypeToDataModel THEN specimen date model structure returned`() {

        val modelStructure = sut.mapAreaTypeToDataModel(AreaType.REGION)

        assertThat(modelStructure).isEqualTo(AREA_DATA_MODEL_BY_SPECIMEN_DATE_STRUCTURE)
    }

    @Test
    fun `GIVEN utla type WHEN mapAreaTypeToDataModel THEN specimen date model structure returned`() {

        val modelStructure = sut.mapAreaTypeToDataModel(AreaType.UTLA)

        assertThat(modelStructure).isEqualTo(AREA_DATA_MODEL_BY_SPECIMEN_DATE_STRUCTURE)
    }

    @Test
    fun `GIVEN ltla type WHEN mapAreaTypeToDataModel THEN specimen date model structure returned`() {

        val modelStructure = sut.mapAreaTypeToDataModel(AreaType.LTLA)

        assertThat(modelStructure).isEqualTo(AREA_DATA_MODEL_BY_SPECIMEN_DATE_STRUCTURE)
    }
}
