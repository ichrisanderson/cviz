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

package com.chrisa.covid19.features.home.presentation.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.chrisa.covid19.R
import com.chrisa.covid19.features.home.domain.models.AreaCaseListModel
import java.text.NumberFormat
import kotlinx.android.synthetic.main.widget_saved_area_card.view.*

@ModelView(defaultLayout = R.layout.widget_saved_area_card)
class SavedAreaCard(context: Context, attrs: AttributeSet) : CardView(context, attrs) {

    var clickListener: OnClickListener? = null
        @CallbackProp set

    override fun onFinishInflate() {
        super.onFinishInflate()
        setOnClickListener { clickListener?.onClick(this) }
    }

    @ModelProp
    fun areCase(areCase: AreaCaseListModel) {
        areaName.text = areCase.areaName
        totalCases.text = "${NumberFormat.getInstance().format(areCase.totalLabConfirmedCases)}"
        casesThisWeek.text = "${areCase.totalLabConfirmedCasesLastWeek}"
        changeInCasesThisWeek.text = "${areCase.changeInTotalLabConfirmedCases}"
    }
}
