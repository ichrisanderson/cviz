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

package com.chrisa.cviz.features.home.presentation.dashboard

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.CarouselModel_
import com.airbnb.epoxy.EpoxyControllerAdapter
import com.chrisa.cviz.SectionHeaderBindingModel_

class DashboardItemDecoration(
    private val horizontalMargin: Int,
    private val verticalMargin: Int
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        val adapter = parent.adapter
        if (adapter is EpoxyControllerAdapter) {
            when (adapter.getModelAtPosition(position)) {
                is SectionHeaderBindingModel_ -> applySectionHeaderItemOffsets(outRect, view, parent)
                is CarouselModel_ -> applyCarouselItemOffsets(outRect)
                else -> applyDefaultItemOffsets(outRect, view, parent)
            }
        }
    }

    private fun applySectionHeaderItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView
    ) {
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = verticalMargin
        } else {
            outRect.top = 0
        }
        outRect.left = horizontalMargin * 2
        outRect.right = horizontalMargin * 2
    }

    private fun applyCarouselItemOffsets(
        outRect: Rect
    ) {
        outRect.left = horizontalMargin
        outRect.right = horizontalMargin
    }

    private fun applyDefaultItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView
    ) {
        // Add top margin only for the first item to avoid double space between items
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = verticalMargin
        } else {
            outRect.top = 0
        }
        outRect.left = horizontalMargin
        outRect.right = horizontalMargin
        outRect.bottom = verticalMargin
    }
}
