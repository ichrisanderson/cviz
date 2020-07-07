package com.chrisa.covid19.core.ui.widgets.search

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.chrisa.covid19.R
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.view_fake_search_bar.view.*

class FakeSearchBar(
    context: Context,
    attrs: AttributeSet
) : TextInputLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_fake_search_bar, this, true)

        this.isFocusable = false
        this.isFocusableInTouchMode = false

        fakeSearchTextInputEditText.isFocusable = false
        fakeSearchTextInputEditText.isFocusableInTouchMode = false
        fakeSearchTextInputEditText.isCursorVisible = false

        fakeSearchTextInputEditText.setOnClickListener { performClick() }
    }
}
