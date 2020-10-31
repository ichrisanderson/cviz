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
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.chrisa.cviz.R
import kotlinx.android.synthetic.main.area_widget_section_header.view.subtitle1
import kotlinx.android.synthetic.main.area_widget_section_header.view.subtitle2
import kotlinx.android.synthetic.main.area_widget_section_header.view.title

@SuppressLint("NonConstantResourceId")
@ModelView(defaultLayout = R.layout.area_widget_section_header)
class AreaSectionHeader(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    @ModelProp
    fun title(text: String) {
        title.text = text
    }

    @ModelProp
    fun subtitle1(text: String?) {
        subtitle1.text = text
    }

    @ModelProp
    fun subtitle2(text: String?) {
        subtitle2.text = text
    }
}
