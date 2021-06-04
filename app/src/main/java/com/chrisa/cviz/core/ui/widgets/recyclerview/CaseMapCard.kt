package com.chrisa.cviz.core.ui.widgets.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import coil.api.load
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelView
import com.chrisa.cviz.R
import com.google.android.material.card.MaterialCardView

@SuppressLint("NonConstantResourceId")
@ModelView(
    defaultLayout = R.layout.core_widget_case_map
)
class CaseMapCard(context: Context, attrs: AttributeSet) :
    MaterialCardView(context, attrs) {

    // 1.You can make it nullable like this and annotate the setter
    var clickListener: OnClickListener? = null
        @CallbackProp set

    override fun onFinishInflate() {
        super.onFinishInflate()
        findViewById<ImageView>(R.id.image)
            .load(mapUri)
        this.setOnClickListener { clickListener?.onClick(it) }
    }

    private companion object {
        private const val mapUri =
            "https://coronavirus.data.gov.uk/public/assets/frontpage/images/map.png"
    }
}
