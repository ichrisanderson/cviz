package com.chrisa.covid19.features.startup.presentation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.chrisa.covid19.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartupFragment : Fragment(R.layout.fragment_startup) {

    private val viewModel: StartupViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.startupState.observe(this.viewLifecycleOwner, Observer {
            val state = it ?: return@Observer
            when (state) {
                StartupState.Success -> navigateToHome()
                StartupState.Loading -> {
                    // TODO: Show loading state
                }
            }
        })
    }

    private fun navigateToHome() {
        findNavController().navigate(StartupFragmentDirections.startupToHome())
    }
}
