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

package com.chrisa.cviz.features.area.presentation.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.chrisa.cviz.R
import com.chrisa.cviz.core.data.synchronisation.WeeklySummary
import com.chrisa.cviz.core.ui.NumberFormatter
import com.chrisa.cviz.core.ui.NumberFormatter.getChangeColour
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.area_widget_case_summary_card.view.changeInNewCasesThisWeek
import kotlinx.android.synthetic.main.area_widget_case_summary_card.view.currentInfectionRate
import kotlinx.android.synthetic.main.area_widget_case_summary_card.view.currentNewCases
import kotlinx.android.synthetic.main.area_widget_case_summary_card.view.dailyCases
import kotlinx.android.synthetic.main.area_widget_case_summary_card.view.infectionRateChangeThisWeek
import kotlinx.android.synthetic.main.area_widget_case_summary_card.view.totalCases

@SuppressLint("NonConstantResourceId")
@ModelView(defaultLayout = R.layout.area_widget_case_summary_card)
class AreaCaseSummaryCard(context: Context, attrs: AttributeSet) :
    MaterialCardView(context, attrs) {

    @ModelProp
    fun summary(summary: WeeklySummary) {
        dailyCases.text = NumberFormatter.format(summary.dailyTotal)
        totalCases.text = NumberFormatter.format(summary.currentTotal)
        currentNewCases.text = NumberFormatter.format(summary.weeklyTotal)
        changeInNewCasesThisWeek.text =
            NumberFormatter.getChangeText(summary.changeInTotal)

        changeInNewCasesThisWeek.setTextColor(
            ContextCompat.getColor(
                changeInNewCasesThisWeek.context,
                getChangeColour(summary.changeInTotal)
            )
        )

        currentInfectionRate.text =
            NumberFormatter.format(summary.weeklyRate.toInt())
        infectionRateChangeThisWeek.text =
            NumberFormatter.getChangeText(summary.changeInRate.toInt())
        infectionRateChangeThisWeek.setTextColor(
            ContextCompat.getColor(
                changeInNewCasesThisWeek.context,
                getChangeColour(summary.changeInRate.toInt())
            )
        )
    }
}
