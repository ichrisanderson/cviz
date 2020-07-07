package com.chrisa.covid19.features.search.presentation

import com.chrisa.covid19.features.search.domain.models.AreaModel

sealed class SearchState {
    object Loading : SearchState()
    object Empty : SearchState()
    data class Success(val items: List<AreaModel>) : SearchState()
}
