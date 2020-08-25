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

package com.chrisa.covid19.features.startup.presentation

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchers
import com.chrisa.covid19.features.startup.domain.BootstrapDataUseCase
import kotlinx.coroutines.launch

class StartupViewModel @ViewModelInject constructor(
    private val bootstrapDataUseCase: BootstrapDataUseCase,
    dispatchers: CoroutineDispatchers
) : ViewModel() {

    private val _startupState = MutableLiveData<StartupState>()
    val startupState: LiveData<StartupState>
        get() = _startupState

    init {
        viewModelScope.launch(dispatchers.io) {
            _startupState.postValue(StartupState.Loading)
            boostrapData()
            _startupState.postValue(StartupState.Success)
        }
    }

    private suspend fun boostrapData() {
        // TODO: Error handling
        bootstrapDataUseCase.execute()
    }
}

sealed class StartupState {
    object Loading : StartupState()
    object Success : StartupState()
}
