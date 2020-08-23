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
import com.chrisa.covid19.core.data.network.AreaDataModel
import com.chrisa.covid19.core.data.network.AreaModel
import com.chrisa.covid19.core.data.network.Page
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.InputStream
import javax.inject.Inject
import okio.buffer
import okio.source

class AssetDataSource @Inject constructor(
    private val assetManager: AssetManager,
    private val moshi: Moshi
) {

    fun getAreas(): List<AreaModel> {
        val type = Types.newParameterizedType(Page::class.java, AreaModel::class.java)
        val adapter = moshi.adapter<Page<AreaModel>>(type)
        val areas = adapter.fromJson(areasFile().source().buffer())
        return areas!!.data
    }

    fun getOverviewAreaData(): List<AreaDataModel> {
        val type = Types.newParameterizedType(Page::class.java, AreaDataModel::class.java)
        val adapter = moshi.adapter<Page<AreaDataModel>>(type)
        val areas = adapter.fromJson(overviewFile().source().buffer())
        return areas!!.data
    }

    private fun areasFile(): InputStream {
        return assetManager.open("areas.json")
    }

    private fun overviewFile(): InputStream {
        return assetManager.open("overview.json")
    }
}
