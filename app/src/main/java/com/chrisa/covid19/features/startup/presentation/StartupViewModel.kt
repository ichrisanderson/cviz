package com.chrisa.covid19.features.startup.presentation

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchers
import com.chrisa.covid19.features.startup.domain.BootstrapDataUseCase
import com.chrisa.covid19.features.startup.domain.SynchronizeCasesUseCase
import com.chrisa.covid19.features.startup.domain.SynchronizeDeathsUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class StartupViewModel @ViewModelInject constructor(
    private val bootstrapDataUseCase: BootstrapDataUseCase,
    private val synchronizeCasesUseCase: SynchronizeCasesUseCase,
    private val synchronizeDeathsUseCase: SynchronizeDeathsUseCase,
    dispatchers: CoroutineDispatchers
) : ViewModel() {

    private val syncScope = CoroutineScope(dispatchers.io + Job())

    private val _startupState = MutableLiveData<StartupState>()
    val startupState: LiveData<StartupState>
        get() = _startupState

    init {
        viewModelScope.launch(dispatchers.io) {
            _startupState.postValue(StartupState.Loading)
            boostrapData()
            triggerDataRefresh()
            _startupState.postValue(StartupState.Success)
        }
    }

    private suspend fun boostrapData() {
        // TODO: Error handling
        bootstrapDataUseCase.execute()
    }

    private suspend fun triggerDataRefresh() {
        // TODO: Error handling
        synchronizeCasesUseCase.execute(syncScope)
        synchronizeDeathsUseCase.execute(syncScope)
    }

}

sealed class StartupState {
    object Loading : StartupState()
    object Success : StartupState()
}
