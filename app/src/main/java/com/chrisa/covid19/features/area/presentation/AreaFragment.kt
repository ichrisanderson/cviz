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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chrisa.covid19.R
import com.jakewharton.rxbinding4.appcompat.itemClicks
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.android.synthetic.main.fragment_area.allCasesChart
import kotlinx.android.synthetic.main.fragment_area.areaToolbar
import kotlinx.android.synthetic.main.fragment_area.latestCasesChart
import kotlinx.android.synthetic.main.fragment_area.totalCasesSubtitle

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

            latestCasesChart.setData(
                areaCasesModel.latestCasesBarChartData,
                areaCasesModel.latestCasesRollingAverageLineChartData
            )
            allCasesChart.setData(
                areaCasesModel.allCasesChartData,
                areaCasesModel.allCasesRollingAverageLineChartData
            )
        })
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
            val menu = areaToolbar.menu
            isSaved?.let { saved ->
                menu.findItem(R.id.insertSavedArea).isVisible = !saved
                menu.findItem(R.id.deleteSavedArea).isVisible = saved
            }
        })
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }
}
