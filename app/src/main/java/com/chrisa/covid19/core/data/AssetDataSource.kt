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

package com.chrisa.covid19.core.data

import android.content.res.AssetManager
import com.chrisa.covid19.core.data.network.CasesModel
import com.chrisa.covid19.core.data.network.DeathsModel
import com.squareup.moshi.Moshi
import java.io.InputStream
import javax.inject.Inject
import okio.buffer
import okio.source

class AssetDataSource @Inject constructor(
    private val assetManager: AssetManager,
    private val moshi: Moshi
) {

    fun getCases(): CasesModel {
        val adapter = moshi.adapter(CasesModel::class.java)
        val cases = adapter.fromJson(casesFile().source().buffer())
        return cases!!
    }

    fun getDeaths(): DeathsModel {
        val adapter = moshi.adapter(DeathsModel::class.java)
        val deaths = adapter.fromJson(deathsFile().source().buffer())
        return deaths!!
    }

    private fun casesFile(): InputStream {
        return assetManager.open("coronavirus-cases_latest.json")
    }

    private fun deathsFile(): InputStream {
        return assetManager.open("coronavirus-deaths_latest.json")
    }
}
