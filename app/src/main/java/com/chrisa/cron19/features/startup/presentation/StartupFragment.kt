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

package com.chrisa.cron19.features.startup.presentation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.chrisa.cron19.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StartupFragment : Fragment(R.layout.startup_fragment) {

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
