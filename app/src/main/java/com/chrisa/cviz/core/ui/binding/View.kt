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

package com.chrisa.cviz.core.ui.binding

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.chrisa.cviz.R
import com.chrisa.cviz.core.ui.NumberFormatter
import com.chrisa.cviz.core.util.DateFormatter

@BindingAdapter("isVisible")
fun isVisible(view: View, isVisible: Boolean) {
    view.isVisible = isVisible
}

@BindingAdapter("goneOnEmpty")
fun goneOnEmpty(view: View, text: String?) {
    view.isVisible = !text.isNullOrEmpty()
}

@BindingAdapter("position")
fun position(textView: TextView, position: Int?) {
    textView.text =
        if (position == null) "" else textView.context.getString(R.string.position_format, position)
}

@BindingAdapter("formattedInt")
fun formattedInt(textView: TextView, number: Int?) {
    textView.text = if (number == null) "" else NumberFormatter.format(number)
}

@BindingAdapter("changeText")
fun changeText(textView: TextView, number: Int?) {
    textView.text = NumberFormatter.getChangeText(number ?: 0)
}

@BindingAdapter("changeTextColor")
fun changeTextColor(textView: TextView, number: Int?) {
    textView.setTextColor(
        ContextCompat.getColor(
            textView.context,
            NumberFormatter.getChangeColour(number ?: 0)
        )
    )
}

@BindingAdapter("lastUpdatedDate")
fun lastUpdatedDate(textView: TextView, date: java.time.LocalDateTime?) {
    textView.text = if (date == null) "" else textView.context.getString(
        R.string.last_updated_date,
        DateFormatter.getLocalRelativeTimeSpanString(date)
    )
}
