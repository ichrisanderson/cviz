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

package com.chrisa.covid19.core.ui

import androidx.annotation.ColorRes
import com.chrisa.covid19.R
import java.text.NumberFormat

object NumberFormatter {

    fun format(toFormat: Int): String {
        return NumberFormat.getInstance().format(toFormat)
    }

    @ColorRes
    fun getChangeColour(change: Int): Int {
        return when {
            change > 0 -> R.color.negativeChange
            else -> R.color.positiveChange
        }
    }

    fun getChangeText(change: Int): String {
        val number = format(change)
        return when {
            change > 0 -> "+$number"
            change == 0 -> "-$number"
            else -> number
        }
    }

    fun format(toFormat: Double): String {
        return NumberFormat.getInstance().format(toFormat)
    }

    @ColorRes
    fun getChangeColour(change: Double): Int {
        return when {
            change > 0 -> R.color.negativeChange
            else -> R.color.positiveChange
        }
    }

    fun getChangeText(change: Double): String {
        val number = format(change)
        return when {
            change > 0 -> "+$number"
            change == 0.0 -> "-$number"
            else -> number
        }
    }
}
