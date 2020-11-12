package com.chrisa.cviz.core.ui.binding

import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter

@BindingAdapter("isVisible")
fun isVisible(view: View, isVisible: Boolean) {
    view.isVisible = isVisible
}

@BindingAdapter("goneOnEmpty")
fun goneOnEmpty(view: View, text: String?) {
    view.isVisible = !text.isNullOrEmpty()
}
