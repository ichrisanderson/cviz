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

package com.chrisa.covid19.features.area.presentation

import android.os.Bundle
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.text.format.DateUtils.getRelativeTimeSpanString
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chrisa.covid19.R
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding4.appcompat.itemClicks
import dagger.hilt.android.AndroidEntryPoint
import io.plaidapp.core.util.event.EventObserver
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.android.synthetic.main.area_content.allCasesChart
import kotlinx.android.synthetic.main.area_content.areaContent
import kotlinx.android.synthetic.main.area_content.changeInNewCasesThisWeek
import kotlinx.android.synthetic.main.area_content.currentInfectionRate
import kotlinx.android.synthetic.main.area_content.currentNewCases
import kotlinx.android.synthetic.main.area_content.infectionRateChangeThisWeek
import kotlinx.android.synthetic.main.area_content.latestCasesChart
import kotlinx.android.synthetic.main.area_content.totalCasesSubtitle
import kotlinx.android.synthetic.main.area_error.areaError
import kotlinx.android.synthetic.main.area_error.errorAction
import kotlinx.android.synthetic.main.fragment_area.areaProgress
import kotlinx.android.synthetic.main.fragment_area.areaToolbar
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class AreaFragment : Fragment(R.layout.fragment_area) {

    private val viewModel: AreaViewModel by viewModels()
    private val args by navArgs<AreaFragmentArgs>()
    private val disposables = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        observeCases()
        observeIsSaved()
        observeIsLoading()
        observeSyncAreaError()
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    private fun initToolbar() {
        areaToolbar.navigationIcon =
            ContextCompat.getDrawable(areaToolbar.context, R.drawable.ic_arrow_back)
        areaToolbar.title = args.areaName
        areaToolbar.setNavigationOnClickListener { navigateUp() }
        disposables.addAll(subscribeMenuClicks())
    }

    private fun subscribeMenuClicks(): @NonNull Disposable {
        return areaToolbar.itemClicks().subscribe {
            when (it.itemId) {
                R.id.insertSavedArea -> {
                    viewModel.insertSavedArea()
                }
                R.id.deleteSavedArea -> {
                    viewModel.deleteSavedArea()
                }
                else -> {
                    // Ignore unknown menu options
                }
            }
        }
    }

    private fun observeCases() {
        viewModel.areaCases.observe(viewLifecycleOwner, Observer {
            val areaCasesModel = it ?: return@Observer

            bindLastUpdated(areaCasesModel.lastUpdatedAt)

            currentNewCases.text = formatNumber(areaCasesModel.currentNewCases)
            changeInNewCasesThisWeek.text = getChangeText(areaCasesModel.changeInNewCasesThisWeek)
            changeInNewCasesThisWeek.setTextColor(
                ContextCompat.getColor(
                    changeInNewCasesThisWeek.context,
                    getChangeColour(areaCasesModel.changeInNewCasesThisWeek)
                )
            )

            currentInfectionRate.text = formatNumber(areaCasesModel.currentInfectionRate)
            infectionRateChangeThisWeek.text = getChangeText(areaCasesModel.changeInInfectionRatesThisWeek)
            infectionRateChangeThisWeek.setTextColor(
                ContextCompat.getColor(
                    changeInNewCasesThisWeek.context,
                    getChangeColour(areaCasesModel.changeInInfectionRatesThisWeek)
                )
            )

            latestCasesChart.setData(
                areaCasesModel.latestCasesBarChartData,
                areaCasesModel.latestCasesRollingAverageLineChartData
            )
            allCasesChart.setData(
                areaCasesModel.allCasesChartData,
                areaCasesModel.allCasesRollingAverageLineChartData
            )
            areaContent.isVisible = true
        })
    }

    private fun formatNumber(toFormat: Int): String {
        return NumberFormat.getInstance().format(toFormat)
    }

    @ColorRes
    private fun getChangeColour(change: Int): Int {
        return when {
            change > 0 -> R.color.negativeChange
            else -> R.color.positiveChange
        }
    }

    private fun getChangeText(change: Int): String {
        val number = formatNumber(change)
        return when {
            change > 0 -> "+$number"
            change == 0 -> "-$number"
            else -> number
        }
    }

    private fun formatNumber(toFormat: Double): String {
        return NumberFormat.getInstance().format(toFormat)
    }

    @ColorRes
    private fun getChangeColour(change: Double): Int {
        return when {
            change > 0 -> R.color.negativeChange
            else -> R.color.positiveChange
        }
    }

    private fun getChangeText(change: Double): String {
        val number = formatNumber(change)
        return when {
            change > 0 -> "+$number"
            change == 0.0 -> "-$number"
            else -> number
        }
    }

    private fun bindLastUpdated(lastUpdatedAt: LocalDateTime?) {
        if (lastUpdatedAt == null) {
            totalCasesSubtitle.text = ""
        } else {
            val zoneId = ZoneId.of("GMT")
            val gmtTime = lastUpdatedAt.atZone(zoneId)
            val now = LocalDateTime.now().atZone(zoneId)
            totalCasesSubtitle.text = getString(
                R.string.last_updated_date,
                getRelativeTimeSpanString(
                    gmtTime.toInstant().toEpochMilli(),
                    now.toInstant().toEpochMilli(),
                    MINUTE_IN_MILLIS
                )
            )
        }
    }

    private fun observeIsSaved() {
        viewModel.isSaved.observe(viewLifecycleOwner, Observer { isSaved ->
            isSaved?.let { saved ->
                val menu = areaToolbar.menu
                menu.findItem(R.id.insertSavedArea).isVisible = !saved
                menu.findItem(R.id.deleteSavedArea).isVisible = saved
            }
        })
    }

    private fun observeIsLoading() {
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            isLoading?.let { loading ->
                areaProgress.isVisible = loading
            }
        })
    }

    private fun observeSyncAreaError() {
        viewModel.syncAreaError.observe(viewLifecycleOwner, EventObserver { isFatal ->
            if (isFatal) {
                errorAction.setOnClickListener { viewModel.refresh() }
                areaError.isVisible = true
                Snackbar.make(
                    areaContent,
                    getString(R.string.sync_error_message),
                    Snackbar.LENGTH_SHORT
                ).show()
            } else {
                Snackbar.make(
                    areaContent,
                    getString(R.string.sync_error_message),
                    Snackbar.LENGTH_SHORT
                )
                    .setAction(R.string.retry) {
                        viewModel.refresh()
                    }
                    .show()
            }
        })
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }
}
