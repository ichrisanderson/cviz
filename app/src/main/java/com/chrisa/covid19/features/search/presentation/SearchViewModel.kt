package com.chrisa.covid19.features.search.presentation

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrisa.covid19.core.util.coroutines.CoroutineDispatchers
import com.chrisa.covid19.features.search.domain.SearchUseCase
import kotlinx.coroutines.launch

class SearchViewModel @ViewModelInject constructor(
    private val searchUseCase: SearchUseCase,
    private val dispatchers: CoroutineDispatchers
) : ViewModel() {

    private val _state = MutableLiveData<SearchState>()
    val state: LiveData<SearchState>
        get() = _state

    fun searchAreas(query: String) {
        _state.postValue(SearchState.Loading)
        viewModelScope.launch(dispatchers.io) {
            // TODO: Error handling
            val areas = searchUseCase.execute(query)
            if (areas.isNotEmpty()) {
                _state.postValue(SearchState.Success(areas))
            } else {
                _state.postValue(SearchState.Empty)
            }
        }
    }
}

