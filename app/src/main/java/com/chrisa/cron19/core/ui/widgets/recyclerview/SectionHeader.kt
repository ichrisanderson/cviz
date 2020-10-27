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

package com.chrisa.cron19.core.ui.widgets.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.chrisa.cron19.R
import kotlinx.android.synthetic.main.core_widget_section_header.view.moreButton
import kotlinx.android.synthetic.main.core_widget_section_header.view.subtitle
import kotlinx.android.synthetic.main.core_widget_section_header.view.title

@SuppressLint("NonConstantResourceId")
@ModelView(defaultLayout = R.layout.core_widget_section_header)
class SectionHeader(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    var clickListener: OnClickListener? = null
        @CallbackProp set

    override fun onFinishInflate() {
        super.onFinishInflate()
        moreButton.setOnClickListener { clickListener?.onClick(this) }
    }

    @ModelProp
    fun title(text: String) {
        title.text = text
    }

    @ModelProp
    fun subtitle(text: String?) {
        subtitle.text = text
        subtitle.isVisible = !text.isNullOrEmpty()
    }

    @ModelProp
    fun isMoreButtonVisible(isVisible: Boolean) {
        moreButton.isVisible = isVisible
    }
}
