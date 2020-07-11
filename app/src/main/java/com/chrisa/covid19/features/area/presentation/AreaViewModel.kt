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
import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchers
import com.chrisa.covid19.features.area.domain.AreaDetailUseCase
import com.chrisa.covid19.features.area.domain.DeleteSavedAreaUseCase
import com.chrisa.covid19.features.area.domain.IsSavedUseCase
import com.chrisa.covid19.features.area.domain.InsertSavedAreaUseCase
import com.chrisa.covid19.features.area.presentation.mappers.AreaCasesModelMapper
import com.chrisa.covid19.features.area.presentation.models.AreaCasesModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AreaViewModel @ViewModelInject constructor(
    private val areaDetailUseCase: AreaDetailUseCase,
    private val isSavedUseCase: IsSavedUseCase,
    private val insertSavedAreaUseCase: InsertSavedAreaUseCase,
    private val deleteSavedAreaUseCase: DeleteSavedAreaUseCase,
    private val dispatchers: CoroutineDispatchers,
    private val areaCasesModelMapper: AreaCasesModelMapper,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _isSaved = MutableLiveData<Boolean>()
    val isSaved: LiveData<Boolean>
        get() = _isSaved

    private val _areaCases = MutableLiveData<AreaCasesModel>()
    val areaCases: LiveData<AreaCasesModel>
        get() = _areaCases

    private val areaCode: String
        get() = savedStateHandle.get<String>("areaCode")!!

    init {
        loadAreaSavedState(areaCode)
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
            val areaDetail = areaDetailUseCase.execute(areCode)
            _areaCases.postValue(areaCasesModelMapper.mapAreaDetailModel(areaDetail))
        }
    }
}
