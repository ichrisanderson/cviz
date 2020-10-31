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

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrisa.cviz.core.util.coroutines.CoroutineDispatchers
import com.chrisa.cviz.features.search.domain.SearchUseCase
import kotlinx.coroutines.launch

class SearchViewModel @ViewModelInject constructor(
    private val searchUseCase: SearchUseCase,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    private val _state = MutableLiveData<SearchState>()
    val state: LiveData<SearchState>
        get() = _state

    fun searchAreas(query: String) {
        _state.postValue(SearchState.Loading)
        viewModelScope.launch(dispatchers.io) {
            // TODO: Error handling
            val areas = searchUseCase.execute(query)
            if (areas.isNotEmpty()) {
                _state.postValue(SearchState.Success(areas))
            } else {
                _state.postValue(SearchState.Empty)
            }
        }
    }
}
