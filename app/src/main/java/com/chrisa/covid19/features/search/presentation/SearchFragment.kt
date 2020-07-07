package com.chrisa.covid19.features.search.presentation

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.chrisa.covid19.R
import com.chrisa.covid19.core.util.KeyboardUtils
import com.chrisa.covid19.features.search.domain.models.AreaModel
import com.jakewharton.rxbinding4.appcompat.queryTextChanges
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_search.*
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search) {

    private val disposables = CompositeDisposable()
    private val viewModel: SearchViewModel by viewModels()
    private val areaAdapter = AreaAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initSearchView()
        initRecyclerView()
        observeViewState()
        observeViewEvents()
    }

    private fun initToolbar() {
        searchToolbar.navigationIcon =
            ContextCompat.getDrawable(searchToolbar.context, R.drawable.ic_arrow_back)

        searchToolbar.setNavigationOnClickListener {
            KeyboardUtils.hideSoftKeyboard(it)
            navigateUp()
        }
    }

    private fun initSearchView() {
        searchView.isIconified = false
        searchView.onActionViewExpanded()
    }

    private fun initRecyclerView() {
        searchRecyclerView.adapter = areaAdapter
        searchRecyclerView.addItemDecoration(
            DividerItemDecoration(
                searchRecyclerView.context,
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
        return searchView.queryTextChanges()
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
        findNavController().navigateUp()
    }

    private fun navigateToArea(area: AreaModel) {
        val action =
            SearchFragmentDirections.searchToArea(areaCode = area.code, areaName = area.name)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }
}

