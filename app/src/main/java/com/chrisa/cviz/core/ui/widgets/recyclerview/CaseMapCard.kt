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

package com.chrisa.cviz.core.ui.widgets.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import coil.api.load
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.chrisa.cviz.R
import com.chrisa.cviz.core.util.DateFormatter
import com.google.android.material.card.MaterialCardView
import java.time.LocalDate

@SuppressLint("NonConstantResourceId")
@ModelView(
    defaultLayout = R.layout.core_widget_case_map
)
class CaseMapCard(context: Context, attrs: AttributeSet) : MaterialCardView(context, attrs) {

    var clickListener: OnClickListener? = null
        @CallbackProp set

    override fun onFinishInflate() {
        super.onFinishInflate()
        this.setOnClickListener { clickListener?.onClick(it) }
    }

    @ModelProp
    fun mapDate(mapDate: LocalDate) {
        findViewById<ImageView>(R.id.image)
            .load(uri = mapUri)
        findViewById<TextView>(R.id.map_title).text =
            context.getString(
                R.string.map_title_format,
                DateFormatter.mediumLocalizedDate(mapDate)
            )
    }

    private companion object {
        private const val mapUri =
            "https://coronavirus.data.gov.uk/public/assets/frontpage/images/map.png"
    }
}
