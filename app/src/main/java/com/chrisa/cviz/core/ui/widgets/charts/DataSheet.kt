/*
 * Copyright 2021 Chris Anderson.
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

package com.chrisa.cviz.core.ui.widgets.charts

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.chrisa.cviz.dataSheetHeaders
import com.chrisa.cviz.dataSheetItem
import com.chrisa.cviz.databinding.CoreWidgetChartDataSheetBinding

class DataSheet(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs) {

    private var binding: CoreWidgetChartDataSheetBinding =
        CoreWidgetChartDataSheetBinding.inflate(LayoutInflater.from(context), this)

    init {
        val x = object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_MOVE -> {
                        rv.parent.requestDisallowInterceptTouchEvent(true)
                    }
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
            }

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
            }
        }
        binding.recyclerView.addOnItemTouchListener(x)
    }

    fun setData(columnHeaders: DataSheetColumnHeaders, data: List<DataSheetItem>) {
        binding.recyclerView.withModels {
            dataSheetHeaders {
                id("data_sheet_headers")
                header1(columnHeaders.labelHeader)
                header2(columnHeaders.valueHeader)
                header3(columnHeaders.cumulativeValueHeader)
            }
            data.forEach { dataItem ->
                dataSheetItem {
                    id(dataItem.label)
                    label(dataItem.label)
                    value1(dataItem.value.toString())
                    value2(dataItem.cumulativeValue.toString())
                }
            }
        }
    }
}
