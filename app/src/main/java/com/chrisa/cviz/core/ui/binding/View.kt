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

