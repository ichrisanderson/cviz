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

package com.chrisa.cviz.core.data.db

object BootstrapData {

    fun areas(): List<AreaEntity> {
        return listOf(
            AreaEntity(
                areaCode = "K02000001",
                areaName = "United Kingdom",
                areaType = AreaType.OVERVIEW
            ),
            AreaEntity(
                areaCode = "E92000001",
                areaName = "England",
                areaType = AreaType.NATION
            ),
            AreaEntity(
                areaCode = "N92000002",
                areaName = "Northern Ireland",
                areaType = AreaType.NATION
            ),
            AreaEntity(
                areaCode = "S92000003",
                areaName = "Scotland",
                areaType = AreaType.NATION
            ),
            AreaEntity(
                areaCode = "W92000004",
                areaName = "Wales",
                areaType = AreaType.NATION
            ),
            AreaEntity(
                areaCode = "E12000004",
                areaName = "East Midlands",
                areaType = AreaType.REGION
            ),
            AreaEntity(
                areaCode = "E12000006",
                areaName = "East of England",
                areaType = AreaType.REGION
            ),
            AreaEntity(
                areaCode = "E12000007",
                areaName = "London",
                areaType = AreaType.REGION
            ),
            AreaEntity(
                areaCode = "E12000001",
                areaName = "North East",
                areaType = AreaType.REGION
            ),
            AreaEntity(
                areaCode = "E12000002",
                areaName = "North West",
                areaType = AreaType.REGION
            ),
            AreaEntity(
                areaCode = "E12000008",
                areaName = "South East",
                areaType = AreaType.REGION
            ),
            AreaEntity(
                areaCode = "E12000009",
                areaName = "South West",
                areaType = AreaType.REGION
            ),
            AreaEntity(
                areaCode = "E12000005",
                areaName = "West Midlands",
                areaType = AreaType.REGION
            ),
            AreaEntity(
                areaCode = "E12000003",
                areaName = "Yorkshire and The Humber",
                areaType = AreaType.REGION
            )
        )
    }
}
