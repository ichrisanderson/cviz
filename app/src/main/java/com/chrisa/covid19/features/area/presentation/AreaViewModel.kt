package com.chrisa.covid19.features.area.presentation

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchers
import com.chrisa.covid19.features.area.domain.AreaUseCase
import com.chrisa.covid19.features.area.presentation.mappers.AreaUiModelMapper
import kotlinx.coroutines.launch

class AreaViewModel @ViewModelInject constructor(
    private val areaUseCase: AreaUseCase,
    private val dispatchers: CoroutineDispatchers,
    private val areaUiModelMapper: AreaUiModelMapper,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableLiveData<AreaState>()
    val state: LiveData<AreaState>
        get() = _state

    init {
        val code = savedStateHandle.get<String>("areaCode")!!
        loadAreaDetail(code)
    }

    private fun loadAreaDetail(areCode: String) {
        _state.postValue(AreaState.Loading)
        viewModelScope.launch(dispatchers.io) {
            val areaDetail = areaUseCase.execute(areCode)
            _state.postValue(AreaState.Success(areaUiModelMapper.mapAreaDetailModel(areaDetail)))
        }
    }
}

