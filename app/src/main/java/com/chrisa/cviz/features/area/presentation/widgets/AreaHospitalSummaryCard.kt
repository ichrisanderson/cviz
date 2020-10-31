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
import kotlinx.android.synthetic.main.area_widget_hospital_summary_card.view.changeInNewHospitalAdmissionsThisWeek
import kotlinx.android.synthetic.main.area_widget_hospital_summary_card.view.currentNewHospitalAdmissions
import kotlinx.android.synthetic.main.area_widget_hospital_summary_card.view.dailyHospitalAdmissions
import kotlinx.android.synthetic.main.area_widget_hospital_summary_card.view.totalHospitalAdmissions

@SuppressLint("NonConstantResourceId")
@ModelView(defaultLayout = R.layout.area_widget_hospital_summary_card)
class AreaHospitalSummaryCard(context: Context, attrs: AttributeSet) :
    MaterialCardView(context, attrs) {

    @ModelProp
    fun summary(summary: WeeklySummary) {
        dailyHospitalAdmissions.text = NumberFormatter.format(summary.dailyTotal)
        totalHospitalAdmissions.text = NumberFormatter.format(summary.currentTotal)

        currentNewHospitalAdmissions.text = NumberFormatter.format(summary.weeklyTotal)
        changeInNewHospitalAdmissionsThisWeek.text =
            NumberFormatter.getChangeText(summary.changeInTotal)

        changeInNewHospitalAdmissionsThisWeek.setTextColor(
            ContextCompat.getColor(
                changeInNewHospitalAdmissionsThisWeek.context,
                getChangeColour(summary.changeInTotal)
            )
        )
    }
}
