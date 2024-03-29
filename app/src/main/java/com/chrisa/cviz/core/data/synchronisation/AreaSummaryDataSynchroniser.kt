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
import com.chrisa.cviz.core.data.db.AreaEntity
import com.chrisa.cviz.core.data.db.AreaSummaryEntity
import com.chrisa.cviz.core.data.db.AreaType
import com.chrisa.cviz.core.data.db.MetadataEntity
import com.chrisa.cviz.core.data.db.MetadataIds
import com.chrisa.cviz.core.data.network.AreaDataModel
import com.chrisa.cviz.core.data.network.AreaDataModelStructureMapper
import com.chrisa.cviz.core.data.network.CovidApi
import com.chrisa.cviz.core.data.network.DAILY_AREA_DATA_FILTER
import com.chrisa.cviz.core.data.network.Page
import com.chrisa.cviz.core.data.time.TimeProvider
import com.chrisa.cviz.core.util.DateUtils.formatAsIso8601
import com.chrisa.cviz.core.util.NetworkUtils
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject
import org.json.JSONObject

internal class AreaSummaryDataSynchroniser @Inject constructor(
    private val appDatabase: AppDatabase,
    private val monthlyDataLoader: MonthlyDataLoader,
    private val areaEntityListBuilder: AreaEntityListBuilder,
    private val networkUtils: NetworkUtils,
    private val timeProvider: TimeProvider
) {
    suspend fun performSync() {
        if (!networkUtils.hasNetworkConnection()) throw IOException()
        val date = timeProvider.currentDate().minusDays(3)
        try {
            val monthlyData = monthlyDataLoader.load(date, AreaType.LTLA)
            val areas = monthlyData.week1.data.map {
                AreaEntity(
                    it.areaCode,
                    it.areaName,
                    AreaType.from(it.areaType)!!
                )
            }.distinct()
            insertAreaEntityList(date, areas, areaEntityListBuilder.build(monthlyData))
        } catch (throwable: Throwable) {
            throw throwable
        }
    }

    private suspend fun insertAreaEntityList(
        date: LocalDate,
        areas: List<AreaEntity>,
        areaEntityList: List<AreaSummaryEntity>
    ) {
        appDatabase.withTransaction {
            appDatabase.areaSummaryDao().deleteAll()
            appDatabase.areaDao().insertAll(areas)
            appDatabase.metadataDao().insert(
                MetadataEntity(
                    id = MetadataIds.areaSummaryId(),
                    lastUpdatedAt = date.atStartOfDay(),
                    lastSyncTime = date.atStartOfDay()
                )
            )
            appDatabase.areaSummaryDao().insertAll(areaEntityList)
        }
    }
}

data class MonthlyData(
    val lastDate: LocalDate,
    val areaType: AreaType,
    val week1: Page<AreaDataModel>,
    val week2: Page<AreaDataModel>,
    val week3: Page<AreaDataModel>,
    val week4: Page<AreaDataModel>
)

class MonthlyDataLoader @Inject constructor(
    private val api: CovidApi,
    private val areaDataModelStructureMapper: AreaDataModelStructureMapper
) {

    suspend fun load(lastDate: LocalDate, areaType: AreaType): MonthlyData {
        val week1 = pagedAreaData(lastDate, areaType)
        require(week1.length != 0)
        val week2 = pagedAreaData(lastDate.minusDays(7), areaType)
        val week3 = pagedAreaData(lastDate.minusDays(14), areaType)
        val week4 = pagedAreaData(lastDate.minusDays(21), areaType)
        return MonthlyData(
            lastDate,
            areaType,
            week1,
            week2,
            week3,
            week4
        )
    }

    private suspend fun pagedAreaData(
        lastDate: LocalDate,
        areaType: AreaType
    ): Page<AreaDataModel> {
        return api.pagedAreaData(
            modifiedDate = null,
            filters = DAILY_AREA_DATA_FILTER(lastDate.formatAsIso8601(), areaType.value),
            structure = AREA_SUMMARY_STRUCTURE
        )
    }

    companion object {
        val AREA_SUMMARY_STRUCTURE = JSONObject().apply {
            put("areaCode", "areaCode")
            put("areaName", "areaName")
            put("areaType", "areaType")
            put("date", "date")
            put("newCases", "newCasesBySpecimenDate")
            put("cumulativeCases", "cumCasesBySpecimenDate")
            put("infectionRate", "cumCasesBySpecimenDateRate")
        }.toString()
    }
}

class AreaEntityListBuilder @Inject constructor() {
    fun build(monthlyData: MonthlyData): List<AreaSummaryEntity> {
        val areaSummaryMap = mutableMapOf<String, AreaSummaryEntity>()
        monthlyData.week1.data
            .filter { it.infectionRate != null && it.cumulativeCases != null }
            .forEach {
                val data = AreaSummaryEntity(
                    areaCode = it.areaCode,
                    date = it.date,
                    baseInfectionRate = it.infectionRate!! / it.cumulativeCases!!,
                    cumulativeCasesWeek1 = it.cumulativeCases,
                    cumulativeCaseInfectionRateWeek1 = it.infectionRate,
                    newCaseInfectionRateWeek1 = 0.0,
                    newCasesWeek1 = 0,
                    cumulativeCasesWeek2 = 0,
                    cumulativeCaseInfectionRateWeek2 = 0.0,
                    newCaseInfectionRateWeek2 = 0.0,
                    newCasesWeek2 = 0,
                    cumulativeCasesWeek3 = 0,
                    cumulativeCaseInfectionRateWeek3 = 0.0,
                    newCaseInfectionRateWeek3 = 0.0,
                    newCasesWeek3 = 0,
                    cumulativeCasesWeek4 = 0,
                    cumulativeCaseInfectionRateWeek4 = 0.0
                )
                areaSummaryMap[it.areaCode] = data
            }

        monthlyData.week2.data.forEach {
            val summary = areaSummaryMap[it.areaCode] ?: return@forEach
            val newCases = summary.cumulativeCasesWeek1 - it.cumulativeCases!!
            areaSummaryMap[it.areaCode] = summary.copy(
                newCasesWeek1 = newCases,
                newCaseInfectionRateWeek1 = newCases * summary.baseInfectionRate,
                cumulativeCaseInfectionRateWeek2 = it.infectionRate!!,
                cumulativeCasesWeek2 = it.cumulativeCases
            )
        }

        monthlyData.week3.data.forEach {
            val summary = areaSummaryMap[it.areaCode] ?: return@forEach
            val newCases = summary.cumulativeCasesWeek2 - it.cumulativeCases!!
            areaSummaryMap[it.areaCode] = summary.copy(
                newCasesWeek2 = newCases,
                newCaseInfectionRateWeek2 = newCases * summary.baseInfectionRate,
                cumulativeCaseInfectionRateWeek3 = it.infectionRate!!,
                cumulativeCasesWeek3 = it.cumulativeCases
            )
        }

        monthlyData.week4.data.forEach {
            val summary = areaSummaryMap[it.areaCode] ?: return@forEach
            val newCases = summary.cumulativeCasesWeek3 - it.cumulativeCases!!
            areaSummaryMap[it.areaCode] = summary.copy(
                newCasesWeek3 = newCases,
                newCaseInfectionRateWeek3 = newCases * summary.baseInfectionRate,
                cumulativeCaseInfectionRateWeek4 = it.infectionRate!!,
                cumulativeCasesWeek4 = it.cumulativeCases
            )
        }

        return areaSummaryMap.values.toList()
    }
}
