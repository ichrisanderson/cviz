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

package com.chrisa.covid19.features.area.presentation

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrisa.covid19.core.data.time.TimeProvider
import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchers
import com.chrisa.covid19.features.area.domain.AreaDetailUseCase
import com.chrisa.covid19.features.area.domain.DeleteSavedAreaUseCase
import com.chrisa.covid19.features.area.domain.InsertSavedAreaUseCase
import com.chrisa.covid19.features.area.domain.IsSavedUseCase
import com.chrisa.covid19.features.area.domain.SyncAreaDetailUseCase
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.presentation.mappers.AreaCasesModelMapper
import com.chrisa.covid19.features.area.presentation.models.AreaCasesModel
import io.plaidapp.core.util.event.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException

@ExperimentalCoroutinesApi
class AreaViewModel @ViewModelInject constructor(
    private val syncAreaDetailUseCase: SyncAreaDetailUseCase,
    private val areaDetailUseCase: AreaDetailUseCase,
    private val isSavedUseCase: IsSavedUseCase,
    private val insertSavedAreaUseCase: InsertSavedAreaUseCase,
    private val deleteSavedAreaUseCase: DeleteSavedAreaUseCase,
    private val dispatchers: CoroutineDispatchers,
    private val areaCasesModelMapper: AreaCasesModelMapper,
    private val timeProvider: TimeProvider,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _isSaved = MutableLiveData<Boolean>()
    val isSaved: LiveData<Boolean>
        get() = _isSaved

    private val _areaCases = MutableLiveData<AreaCasesModel>()
    val areaCases: LiveData<AreaCasesModel>
        get() = _areaCases

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _syncAreaError = MutableLiveData<Event<Boolean>>()
    val syncAreaError: LiveData<Event<Boolean>>
        get() = _syncAreaError

    private val areaCode: String
        get() = savedStateHandle.get<String>("areaCode")!!

    private val areaType: String
        get() = savedStateHandle.get<String>("areaType")!!

    init {
        loadAreaSavedState(areaCode)
        loadAreaDetail(areaCode)
    }

    fun refresh() {
        loadAreaDetail(areaCode)
    }

    fun insertSavedArea() {
        viewModelScope.launch(dispatchers.io) {
            insertSavedAreaUseCase.execute(areaCode)
        }
    }

    fun deleteSavedArea() {
        viewModelScope.launch(dispatchers.io) {
            deleteSavedAreaUseCase.execute(areaCode)
        }
    }

    private fun loadAreaSavedState(areCode: String) {
        viewModelScope.launch(dispatchers.io) {
            val isSavedFlow = isSavedUseCase.execute(areCode)
            isSavedFlow.collect { _isSaved.postValue(it) }
        }
    }

    private fun loadAreaDetail(areCode: String) {
        viewModelScope.launch(dispatchers.io) {
            _isLoading.postValue(true)
            runCatching {
                areaDetailUseCase.execute(areCode)
            }.onSuccess { areaDetail ->
                areaDetail.collect { areaDetailModel ->
                    val now = timeProvider.currentTime()
                    if (areaDetailModel.lastSyncedAt == null || areaDetailModel.lastSyncedAt.plusMinutes(
                            5
                        ).isBefore(now)
                    ) {
                        syncAreaCases(areaDetailModel)
                    } else {
                        _isLoading.postValue(false)
                        _areaCases.postValue(areaCasesModelMapper.mapAreaDetailModel(areaDetailModel))
                    }
                }
            }.onFailure {
                _isLoading.postValue(false)
                _syncAreaError.postValue(Event(true))
            }
        }
    }

    private suspend fun syncAreaCases(areaDetailModel: AreaDetailModel) {
        viewModelScope.launch(dispatchers.io) {
            runCatching {
                syncAreaDetailUseCase.execute(areaCode, areaType)
            }.onFailure { error ->
                if (error is HttpException && error.code() == 304) {
                    _isLoading.postValue(false)
                    _areaCases.postValue(areaCasesModelMapper.mapAreaDetailModel(areaDetailModel))
                } else if (areaDetailModel.lastSyncedAt == null) {
                    _isLoading.postValue(false)
                    _syncAreaError.postValue(Event(true))
                } else {
                    _areaCases.postValue(areaCasesModelMapper.mapAreaDetailModel(areaDetailModel))
                    _isLoading.postValue(false)
                    _syncAreaError.postValue(Event(false))
                }
            }
        }
    }
}
