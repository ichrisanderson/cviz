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

package com.chrisa.cviz.features.home.presentation.savedareas

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrisa.cviz.core.util.coroutines.CoroutineDispatchers
import com.chrisa.cviz.features.home.domain.LoadSavedAreasUseCase
import com.chrisa.cviz.features.home.domain.RefreshDataUseCase
import com.chrisa.cviz.features.home.domain.models.SummaryModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@FlowPreview
class SavedAreasViewModel @ViewModelInject constructor(
    private val loadSavedAreasUseCase: LoadSavedAreasUseCase,
    private val refreshDataUseCase: RefreshDataUseCase,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    private val _savedAreas = MutableLiveData<List<SummaryModel>>()
    val savedAreas: LiveData<List<SummaryModel>>
        get() = _savedAreas

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean>
        get() = _isRefreshing

    init {
        loadSavedAreaCases()
    }

    private fun loadSavedAreaCases() {
        viewModelScope.launch(dispatchers.io) {
            _isLoading.postValue(true)
            val savedAreas = loadSavedAreasUseCase.execute()
            savedAreas.collect {
                _savedAreas.postValue(it)
                _isLoading.postValue(false)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(dispatchers.io) {
            runCatching {
                _isRefreshing.postValue(true)
                refreshDataUseCase.execute()
            }.onSuccess {
                _isRefreshing.postValue(false)
            }.onFailure {
                _isRefreshing.postValue(false)
            }
        }
    }
}
