package com.chrisa.covid19.features.area.presentation

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.chrisa.covid19.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_area.*

@AndroidEntryPoint
class AreaFragment : Fragment(R.layout.fragment_area) {

    private val viewModel: AreaViewModel by viewModels()
    private val args by navArgs<AreaFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        observeViewModel()
    }

    private fun initToolbar() {
        areaToolbar.navigationIcon =
            ContextCompat.getDrawable(areaToolbar.context, R.drawable.ic_arrow_back)
        areaToolbar.title = args.areaName
        areaToolbar.setNavigationOnClickListener { navigateUp() }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner, Observer {
            val state = it ?: return@Observer
            when (state) {
                AreaState.Loading -> {
                    // TODO: Loading State
                }
                is AreaState.Success -> {
                    latestCasesChart.setData(state.areaUiModel.latestCasesChartData)
                    allCasesChart.setData(state.areaUiModel.allCasesChartData)
                }
            }
        })
    }

    private fun navigateUp() {
        findNavController().navigateUp()
    }
}

