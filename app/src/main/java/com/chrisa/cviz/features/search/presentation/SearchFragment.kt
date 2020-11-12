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

package com.chrisa.cviz.features.search.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.chrisa.cviz.R
import com.chrisa.cviz.core.util.KeyboardUtils
import com.chrisa.cviz.databinding.SearchFragmentBinding
import com.chrisa.cviz.features.search.domain.models.AreaModel
import com.jakewharton.rxbinding4.appcompat.queryTextChanges
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.search_fragment) {

    private lateinit var binding: SearchFragmentBinding
    private val disposables = CompositeDisposable()
    private val viewModel: SearchViewModel by viewModels()
    private val areaAdapter = AreaAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SearchFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initSearchView()
        initRecyclerView()
        observeViewState()
        observeViewEvents()
    }

    private fun initToolbar() {
        binding.toolbar.navigationIcon =
            ContextCompat.getDrawable(binding.toolbar.context, R.drawable.ic_arrow_back)

        binding.toolbar.setNavigationOnClickListener {
            navigateUp()
        }
    }

    private fun initSearchView() {
        binding.searchView.isIconified = false
        binding.searchView.onActionViewExpanded()
    }

    private fun initRecyclerView() {
        binding.recyclerView.adapter = areaAdapter
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                binding.recyclerView.context,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun observeViewState() {
        viewModel.state.observe(viewLifecycleOwner, Observer {
            val state = it ?: return@Observer
            when (state) {
                SearchState.Loading -> {
                    // TODO: Show loading state
                }
                is SearchState.Success -> areaAdapter.submitList(state.items)
                SearchState.Empty -> {
                    areaAdapter.submitList(emptyList())
                }
            }
        })
    }

    private fun observeViewEvents() {
        disposables.addAll(
            searchViewQueryTextChanges(),
            areaAdapterClickEvents()
        )
    }

    private fun searchViewQueryTextChanges(): Disposable {
        return binding.searchView.queryTextChanges()
            .skipInitialValue()
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { query ->
                viewModel.searchAreas(query.toString())
            }
    }

    private fun areaAdapterClickEvents(): Disposable {
        return areaAdapter.clickEvent
            .debounce(300, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { area ->
                navigateToArea(area)
            }
    }

    private fun navigateUp() {
        hideKeyboard()
        findNavController().navigateUp()
    }

    private fun navigateToArea(area: AreaModel) {
        hideKeyboard()
        val action =
            SearchFragmentDirections.searchToArea(
                areaCode = area.code,
                areaName = area.name,
                areaType = area.type
            )
        findNavController().navigate(action)
    }

    private fun hideKeyboard() {
        KeyboardUtils.hideSoftKeyboard(binding.searchView)
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }
}
