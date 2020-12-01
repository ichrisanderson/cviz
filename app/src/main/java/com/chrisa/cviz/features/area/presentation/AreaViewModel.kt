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

package com.chrisa.cviz.features.area.presentation

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrisa.cviz.core.util.coroutines.CoroutineDispatchers
import com.chrisa.cviz.features.area.domain.AreaDetailModelResult
import com.chrisa.cviz.features.area.domain.AreaDetailUseCase
import com.chrisa.cviz.features.area.domain.DeleteSavedAreaUseCase
import com.chrisa.cviz.features.area.domain.InsertSavedAreaUseCase
import com.chrisa.cviz.features.area.domain.IsSavedUseCase
import com.chrisa.cviz.features.area.domain.models.AreaDetailModel
import com.chrisa.cviz.features.area.presentation.mappers.AreaDataModelMapper
import com.chrisa.cviz.features.area.presentation.models.AreaDataModel
import io.plaidapp.core.util.event.Event
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class AreaViewModel @ViewModelInject constructor(
    private val areaDetailUseCase: AreaDetailUseCase,
    private val isSavedUseCase: IsSavedUseCase,
    private val insertSavedAreaUseCase: InsertSavedAreaUseCase,
    private val deleteSavedAreaUseCase: DeleteSavedAreaUseCase,
    private val dispatchers: CoroutineDispatchers,
    private val areaDataModelMapper: AreaDataModelMapper,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _isSaved = MutableLiveData<Boolean>()
    val isSaved: LiveData<Boolean>
        get() = _isSaved

    private val _areaData = MutableLiveData<AreaDataModel>()
    val areaDataModel: LiveData<AreaDataModel>
        get() = _areaData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean>
        get() = _isRefreshing

    private val _syncAreaError = MutableLiveData<Event<Boolean>>()
    val syncAreaError: LiveData<Event<Boolean>>
        get() = _syncAreaError

    private val areaCode: String
        get() = savedStateHandle.get<String>("areaCode")!!

    private val areaType: String
        get() = savedStateHandle.get<String>("areaType")!!

    init {
        loadAreaSavedState(areaCode)
        loadAreaDetail(areCode = areaCode, isLoading = true, isRefreshing = false)
    }

    fun retry() {
        loadAreaDetail(areCode = areaCode, isLoading = true, isRefreshing = false)
    }

    fun refresh() {
        loadAreaDetail(areCode = areaCode, isLoading = false, isRefreshing = true)
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

    private fun loadAreaDetail(areCode: String, isLoading: Boolean, isRefreshing: Boolean) {
        _isLoading.postValue(isLoading)
        _isRefreshing.postValue(isRefreshing)
        viewModelScope.launch(dispatchers.io) {
            runCatching {
                areaDetailUseCase.execute(areCode, areaType)
            }.onSuccess { areaDetail ->
                areaDetail.collect { result ->
                    when (result) {
                        is AreaDetailModelResult.Success -> postAreaDetailModel(result.data)
                        is AreaDetailModelResult.NoData -> postError()
                    }
                }
            }.onFailure {
                postError()
            }
        }
    }

    private fun postError() {
        _isRefreshing.postValue(false)
        _isLoading.postValue(false)
        _syncAreaError.postValue(Event(true))
    }

    private fun postAreaDetailModel(areaDetailModel: AreaDetailModel) {
        _isRefreshing.postValue(false)
        _isLoading.postValue(false)
        _areaData.postValue(areaDataModelMapper.mapAreaDetailModel(areaDetailModel))
    }
}
