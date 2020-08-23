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

package com.chrisa.covid19.features.home.presentation.widgets

import android.content.Context
import android.text.format.DateUtils
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.util.AttributeSet
import androidx.cardview.widget.CardView
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.chrisa.covid19.R
import com.chrisa.covid19.features.home.domain.models.LatestUkData
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.android.synthetic.main.widget_latest_uk_data_card.view.*

@ModelView(defaultLayout = R.layout.widget_latest_uk_data_card)
class LatestUkDataCard(context: Context, attrs: AttributeSet) : CardView(context, attrs) {

    @Override
    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    @ModelProp
    fun latestUkData(latestUkData: LatestUkData) {
        areaName.text = latestUkData.areaName

        val zoneId = ZoneId.of("GMT")
        val gmtTime = latestUkData.lastUpdated.atZone(zoneId)
        val now = LocalDateTime.now().atZone(zoneId)

        lastUpdated.text = lastUpdated.context.getString(
            R.string.last_updated_date,
            DateUtils.getRelativeTimeSpanString(
                gmtTime.toInstant().toEpochMilli(),
                now.toInstant().toEpochMilli(),
                MINUTE_IN_MILLIS
            )
        )

        totalCases.text = formatNumber(latestUkData.totalLabConfirmedCases)
        dailyCases.text = formatNumber(latestUkData.dailyLabConfirmedCases)
    }

    private fun formatNumber(toFormat: Int): String {
        return NumberFormat.getInstance().format(toFormat)
    }
}
