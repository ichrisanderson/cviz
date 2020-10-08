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

package com.chrisa.covid19.features.area.presentation.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.chrisa.covid19.R
import com.chrisa.covid19.core.ui.NumberFormatter
import com.chrisa.covid19.core.ui.NumberFormatter.getChangeColour
import com.chrisa.covid19.core.util.DateFormatter
import com.chrisa.covid19.core.util.DateFormatter.mediumLocalizedDate
import com.chrisa.covid19.features.area.presentation.models.AreaCasesModel
import com.google.android.material.card.MaterialCardView
import java.time.LocalDateTime
import kotlinx.android.synthetic.main.area_widget_detail_card.view.changeInNewCasesThisWeek
import kotlinx.android.synthetic.main.area_widget_detail_card.view.currentInfectionRate
import kotlinx.android.synthetic.main.area_widget_detail_card.view.currentNewCases
import kotlinx.android.synthetic.main.area_widget_detail_card.view.infectionRateChangeThisWeek
import kotlinx.android.synthetic.main.area_widget_detail_card.view.lastUpdated
import kotlinx.android.synthetic.main.area_widget_detail_card.view.totalCases
import kotlinx.android.synthetic.main.area_widget_detail_card.view.totalCasesCaption

@SuppressLint("NonConstantResourceId")
@ModelView(defaultLayout = R.layout.area_widget_detail_card)
class AreaDetailCard(context: Context, attrs: AttributeSet) : MaterialCardView(context, attrs) {

    @ModelProp
    fun areaCasesModel(areaCasesModel: AreaCasesModel) {
        bindLastUpdated(areaCasesModel.lastUpdatedAt)

        totalCases.text = NumberFormatter.format(areaCasesModel.totalCases)
        totalCasesCaption.text = context.getString(
            R.string.up_to_postfix,
            context.getString(R.string.total_cases),
            mediumLocalizedDate(areaCasesModel.lastUpdatedAt)
        )

        currentNewCases.text = NumberFormatter.format(areaCasesModel.currentNewCases)
        changeInNewCasesThisWeek.text =
            NumberFormatter.getChangeText(areaCasesModel.changeInNewCasesThisWeek)
        changeInNewCasesThisWeek.setTextColor(
            ContextCompat.getColor(
                changeInNewCasesThisWeek.context,
                getChangeColour(areaCasesModel.changeInNewCasesThisWeek)
            )
        )

        currentInfectionRate.text =
            NumberFormatter.format(areaCasesModel.currentInfectionRate.toInt())
        infectionRateChangeThisWeek.text =
            NumberFormatter.getChangeText(areaCasesModel.changeInInfectionRatesThisWeek.toInt())
        infectionRateChangeThisWeek.setTextColor(
            ContextCompat.getColor(
                changeInNewCasesThisWeek.context,
                getChangeColour(areaCasesModel.changeInInfectionRatesThisWeek.toInt())
            )
        )
    }

    private fun bindLastUpdated(lastUpdatedAt: LocalDateTime?) {
        if (lastUpdatedAt == null) {
            lastUpdated.text = ""
        } else {
            lastUpdated.text = context.getString(
                R.string.last_updated_date,
                DateFormatter.getLocalRelativeTimeSpanString(lastUpdatedAt)
            )
        }
    }
}
