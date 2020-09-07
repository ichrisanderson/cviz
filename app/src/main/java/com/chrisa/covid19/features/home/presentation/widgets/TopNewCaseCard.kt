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
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.chrisa.covid19.R
import com.chrisa.covid19.core.ui.NumberFormatter
import com.chrisa.covid19.features.home.domain.models.NewCaseModel
import kotlinx.android.synthetic.main.widget_top_new_case_card.view.areaName
import kotlinx.android.synthetic.main.widget_top_new_case_card.view.areaPosition
import kotlinx.android.synthetic.main.widget_top_new_case_card.view.changeThisWeek
import kotlinx.android.synthetic.main.widget_top_new_case_card.view.currentNewCases

@SuppressLint("NonConstantResourceId")
@ModelView(defaultLayout = R.layout.widget_top_new_case_card)
class TopNewCaseCard(context: Context, attrs: AttributeSet) : CardView(context, attrs) {

    var clickListener: OnClickListener? = null
        @CallbackProp set

    override fun onFinishInflate() {
        super.onFinishInflate()
        setOnClickListener { clickListener?.onClick(this) }
    }

    @ModelProp
    fun newCaseModel(newCaseModel: NewCaseModel) {
        areaPosition.text =
            areaPosition.context.getString(R.string.position_format, newCaseModel.position)
        areaName.text = newCaseModel.areaName
        currentNewCases.text = NumberFormatter.format(newCaseModel.currentNewCases)
        changeThisWeek.text = NumberFormatter.getChangeText(newCaseModel.changeInCases)
        changeThisWeek.setTextColor(
            ContextCompat.getColor(
                changeThisWeek.context,
                NumberFormatter.getChangeColour(newCaseModel.changeInCases)
            )
        )
    }
}
