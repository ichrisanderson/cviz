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

package com.chrisa.cviz.core.data.db.hospitallookups

import android.content.res.AssetManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.io.InputStream
import javax.inject.Inject
import okio.buffer
import okio.source

class HospitalLookupsAssetDataSource @Inject constructor(
    private val moshi: Moshi,
    private val assetManager: AssetManager
) {

    fun getItems(): List<HospitalLookup> {
        val type = Types.newParameterizedType(List::class.java, HospitalLookup::class.java)
        val adapter = moshi.adapter<List<HospitalLookup>>(type)
        return adapter.fromJson(hospitalLookupsFile().source().buffer())!!
    }

    private fun hospitalLookupsFile(): InputStream =
        assetManager.open("hospital_lookups.json")
}
