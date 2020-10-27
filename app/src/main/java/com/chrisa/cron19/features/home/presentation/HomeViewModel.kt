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

package com.chrisa.cron19.features.home.presentation

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrisa.cron19.core.util.coroutines.CoroutineDispatchers
import com.chrisa.cron19.features.home.domain.LoadDashboardDataUseCase
import com.chrisa.cron19.features.home.domain.models.DashboardDataModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@InternalCoroutinesApi
@FlowPreview
class HomeViewModel @ViewModelInject constructor(
    private val loadDashboardDataUseCase: LoadDashboardDataUseCase,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    private val _homeScreenData = MutableLiveData<DashboardDataModel>()
    val dashboardData: LiveData<DashboardDataModel>
        get() = _homeScreenData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    init {
        loadSavedAreaCases()
    }

    private fun loadSavedAreaCases() {
        viewModelScope.launch(dispatchers.io) {
            _isLoading.postValue(true)

            val homeScreenData = loadDashboardDataUseCase.execute()

            homeScreenData.collect {
                _homeScreenData.postValue(it)
                _isLoading.postValue(false)
            }
        }
    }
}
