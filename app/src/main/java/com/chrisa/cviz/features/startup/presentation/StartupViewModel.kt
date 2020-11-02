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

package com.chrisa.cviz.features.startup.presentation

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrisa.cviz.core.util.coroutines.CoroutineDispatchers
import com.chrisa.cviz.features.startup.domain.StartupResult
import com.chrisa.cviz.features.startup.domain.StartupUseCase
import io.plaidapp.core.util.event.Event
import kotlinx.coroutines.launch

class StartupViewModel @ViewModelInject constructor(
    private val startupUseCase: StartupUseCase,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _syncError = MutableLiveData<Event<Boolean>>()
    val syncError: LiveData<Event<Boolean>>
        get() = _syncError

    private val _navigateHome = MutableLiveData<Event<Unit>>()
    val navigateHome: LiveData<Event<Unit>>
        get() = _navigateHome

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch(dispatchers.io) {
            refreshData()
        }
    }

    private suspend fun refreshData() {
        _isLoading.postValue(true)
        when (startupUseCase.execute()) {
            StartupResult.ShowHomeScreen -> {
                _navigateHome.postValue(Event(Unit))
            }
            StartupResult.ShowHomeScreenWithSyncError -> {
                _navigateHome.postValue(Event(Unit))
                _syncError.postValue(Event(false))
            }
            is StartupResult.ShowFatalError -> {
                _isLoading.postValue(false)
                _syncError.postValue(Event(true))
            }
        }
    }
}
