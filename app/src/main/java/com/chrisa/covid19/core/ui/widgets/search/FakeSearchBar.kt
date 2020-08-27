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

package com.chrisa.covid19.core.ui.widgets.search

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.chrisa.covid19.R
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.widget_fake_search_bar.view.fakeSearchTextInputEditText

class FakeSearchBar(
    context: Context,
    attrs: AttributeSet
) : TextInputLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.widget_fake_search_bar, this, true)

        this.isFocusable = false
        this.isFocusableInTouchMode = false

        fakeSearchTextInputEditText.isFocusable = false
        fakeSearchTextInputEditText.isFocusableInTouchMode = false
        fakeSearchTextInputEditText.isCursorVisible = false

        fakeSearchTextInputEditText.setOnClickListener { performClick() }
    }
}
