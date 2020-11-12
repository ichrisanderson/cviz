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

package com.chrisa.cviz.features.startup.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.chrisa.cviz.R
import com.chrisa.cviz.databinding.StartupFragmentBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.plaidapp.core.util.event.EventObserver

@AndroidEntryPoint
class StartupFragment : Fragment(R.layout.startup_fragment) {

    private lateinit var binding: StartupFragmentBinding
    private val viewModel: StartupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = StartupFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.navigateHome.observe(this.viewLifecycleOwner, EventObserver {
            navigateToHome()
        })
        viewModel.isLoading.observe(this.viewLifecycleOwner, {
            binding.progress.isIndeterminate = it
        })
        viewModel.syncError.observe(this.viewLifecycleOwner, EventObserver {
            showSnackbar()
        })
    }

    private fun showSnackbar() {
        Snackbar.make(
            binding.root,
            getString(R.string.sync_error_message),
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(R.string.retry) { viewModel.refresh() }
            .show()
    }

    private fun navigateToHome() {
        findNavController().navigate(StartupFragmentDirections.startupToHome())
    }
}
