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

package com.chrisa.cviz.features.home.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.chrisa.cviz.R
import com.chrisa.cviz.databinding.HomeFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi

@FlowPreview
@InternalCoroutinesApi
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.home_fragment) {

    private lateinit var binding: HomeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initPager()
        initNavigation()
    }

    private fun initNavigation() {
        binding.navigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.dashboard -> binding.pager.setCurrentItem(0, false)
                R.id.saved -> binding.pager.setCurrentItem(1, false)
                R.id.settings -> binding.pager.setCurrentItem(2, false)
            }
            true
        }
    }

    private fun initPager() {
        binding.pager.isUserInputEnabled = false
        binding.pager.offscreenPageLimit = 3
        binding.pager.adapter = HomeAdapter(childFragmentManager, lifecycle)
    }

    private fun initToolbar() {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.search -> {
                    navigateToSearch()
                    true
                }
                else -> false
            }
        }
    }

    private fun navigateToSearch() {
        findNavController()
            .navigate(HomeFragmentDirections.homeToSearch())
    }
}
