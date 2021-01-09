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
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import io.mockk.every
import io.mockk.mockk
import java.nio.charset.Charset
import org.junit.Test

class HospitalLookupsAssetDataSourceTest {

    private val moshi = Moshi.Builder().build()
    private val assetManager: AssetManager = mockk()

    private val sut = HospitalLookupsAssetDataSource(moshi, assetManager)

    @Test
    fun `WHEN getItems called THEN asset file loaded`() {
        val lookupsJson = "[{ \"areaCode\": \"E09000002\",\"trustCode\": \"RF4\" }]"
        every { assetManager.open("hospital_lookups.json") } returns
            lookupsJson.byteInputStream(Charset.defaultCharset())

        val items = sut.getItems()

        assertThat(items).isEqualTo(listOf(HospitalLookup("E09000002", "RF4")))
    }
}
