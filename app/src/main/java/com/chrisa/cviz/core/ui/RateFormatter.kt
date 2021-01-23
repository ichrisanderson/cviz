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

package com.chrisa.cviz.core.ui

import androidx.annotation.ColorRes
import com.chrisa.cviz.R
import java.math.RoundingMode
import java.text.DecimalFormat

object RateFormatter {

    private val decimalFormat = DecimalFormat("0.0").apply {
        roundingMode = RoundingMode.CEILING
    }

    fun formattedRate(rate: Double): String =
        decimalFormat.format(rate)

    fun formattedRateChange(rate: Double): String {
        val formatted = formattedRate(rate)
        return when {
            rate > 0 -> "+$formatted%"
            else -> "$formatted%"
        }
    }

    @ColorRes
    fun getRateChangeColour(min: Double, max: Double): Int {
        return when {
            min <= 0 && max <= 0 -> R.color.positiveChange
            min > 0 && max > 0 -> R.color.negativeChange
            else -> R.color.upwardChange
        }
    }
}
