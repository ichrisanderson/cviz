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

package com.chrisa.covid19.features.home.presentation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.chrisa.covid19.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.home_fragment.homeNavigation
import kotlinx.android.synthetic.main.home_fragment.homePager
import kotlinx.android.synthetic.main.home_fragment.homeToolbar
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi

@FlowPreview
@InternalCoroutinesApi
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.home_fragment) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeToolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.search -> {
                    navigateToSearch()
                    true
                }
                else -> false
            }
        }
        homePager.isUserInputEnabled = false
        homePager.offscreenPageLimit = 3
        homePager.adapter = HomeAdapter(childFragmentManager, lifecycle)
        homeNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.dashboard -> homePager.setCurrentItem(0, true)
                R.id.saved -> homePager.setCurrentItem(1, true)
            }
            true
        }
    }

    private fun navigateToSearch() {
        findNavController()
            .navigate(HomeFragmentDirections.homeToSearch())
    }
}
