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

package com.chrisa.covid19.features.home.presentation.summarylist

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchers
import com.chrisa.covid19.features.home.domain.LoadAreaSummariesUseCase
import com.chrisa.covid19.features.home.domain.models.SortOption
import com.chrisa.covid19.features.home.domain.models.SummaryModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@FlowPreview
class SummaryListViewModel @ViewModelInject constructor(
    private val loadAreaSummariesUseCase: LoadAreaSummariesUseCase,
    private val dispatchers: CoroutineDispatchers,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _summaries = MutableLiveData<List<SummaryModel>>()
    val summaries: LiveData<List<SummaryModel>>
        get() = _summaries

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    val sortOption: SortOption
        get() = savedStateHandle.get<SortOption>("sortOption")!!

    init {
        loadSavedAreaCases()
    }

    private fun loadSavedAreaCases() {
        viewModelScope.launch(dispatchers.io) {
            _isLoading.postValue(true)

            val summaries = loadAreaSummariesUseCase.execute(sortOption)

            summaries.collect {
                _summaries.postValue(it)
                _isLoading.postValue(false)
            }
        }
    }
}
