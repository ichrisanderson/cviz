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

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.chrisa.covid19.R
import com.chrisa.covid19.core.ui.NumberFormatter
import com.chrisa.covid19.features.home.domain.models.SummaryModel
import kotlinx.android.synthetic.main.widget_summary_card.view.areaName
import kotlinx.android.synthetic.main.widget_summary_card.view.areaPosition
import kotlinx.android.synthetic.main.widget_summary_card.view.casesGroup
import kotlinx.android.synthetic.main.widget_summary_card.view.changeInCasesThisWeek
import kotlinx.android.synthetic.main.widget_summary_card.view.changeInInfectionRateThisWeek
import kotlinx.android.synthetic.main.widget_summary_card.view.currentInfectionRate
import kotlinx.android.synthetic.main.widget_summary_card.view.currentNewCases
import kotlinx.android.synthetic.main.widget_summary_card.view.infectionRateGroup

@SuppressLint("NonConstantResourceId")
@ModelView(defaultLayout = R.layout.widget_summary_card)
class SummaryCard(context: Context, attrs: AttributeSet) : CardView(context, attrs) {

    var clickListener: OnClickListener? = null
        @CallbackProp set

    override fun onFinishInflate() {
        super.onFinishInflate()
        setOnClickListener { clickListener?.onClick(this) }
    }

    @ModelProp
    fun summary(summary: SummaryModel) {
        areaPosition.text =
            areaPosition.context.getString(R.string.position_format, summary.position)
        areaName.text = summary.areaName
        currentNewCases.text = NumberFormatter.format(summary.currentNewCases)
        changeInCasesThisWeek.text = NumberFormatter.getChangeText(summary.changeInCases)
        changeInCasesThisWeek.setTextColor(
            ContextCompat.getColor(
                changeInCasesThisWeek.context,
                NumberFormatter.getChangeColour(summary.changeInCases)
            )
        )
        currentInfectionRate.text = NumberFormatter.format(summary.currentInfectionRate.toInt())
        changeInInfectionRateThisWeek.text =
            NumberFormatter.getChangeText(summary.changeInInfectionRate.toInt())
        changeInInfectionRateThisWeek.setTextColor(
            ContextCompat.getColor(
                changeInInfectionRateThisWeek.context,
                NumberFormatter.getChangeColour(summary.changeInInfectionRate.toInt())
            )
        )
    }

    @ModelProp
    fun showCases(isVisible: Boolean) {
        casesGroup.isVisible = isVisible
    }

    @ModelProp
    fun showInfectionRates(isVisible: Boolean) {
        infectionRateGroup.isVisible = isVisible
    }
}
