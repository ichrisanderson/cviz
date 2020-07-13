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

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchers
import com.chrisa.covid19.features.home.domain.LoadSavedAreaCasesUseCase
import com.chrisa.covid19.features.home.domain.models.AreaCaseList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@FlowPreview
class HomeViewModel @ViewModelInject constructor(
    private val loadSavedAreaCasesUseCase: LoadSavedAreaCasesUseCase,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    private val _areaCases = MutableLiveData<List<AreaCaseList>>()
    val areaCases: LiveData<List<AreaCaseList>>
        get() = _areaCases

    init {
        loadSavedAreaCases()
    }

    private fun loadSavedAreaCases() {
        viewModelScope.launch(dispatchers.io) {
            val savedAreaCases = loadSavedAreaCasesUseCase.execute()
            savedAreaCases.collect { _areaCases.postValue(it) }
        }
    }
}
