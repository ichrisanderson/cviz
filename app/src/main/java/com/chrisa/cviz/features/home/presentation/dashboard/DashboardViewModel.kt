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

package com.chrisa.cviz.features.home.presentation.dashboard

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrisa.cviz.core.util.coroutines.CoroutineDispatchers
import com.chrisa.cviz.features.home.domain.LoadDashboardDataUseCase
import com.chrisa.cviz.features.home.domain.RefreshDataUseCase
import com.chrisa.cviz.features.home.domain.models.DashboardDataModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@InternalCoroutinesApi
@FlowPreview
class DashboardViewModel @ViewModelInject constructor(
    private val loadDashboardDataUseCase: LoadDashboardDataUseCase,
    private val refreshDataUseCase: RefreshDataUseCase,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    private val _dashboardData = MutableLiveData<DashboardDataModel>()
    val dashboardData: LiveData<DashboardDataModel>
        get() = _dashboardData

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
            val homeScreenData = loadDashboardDataUseCase.execute()
            homeScreenData.collect {
                _isLoading.postValue(false)
                _dashboardData.postValue(it)
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
