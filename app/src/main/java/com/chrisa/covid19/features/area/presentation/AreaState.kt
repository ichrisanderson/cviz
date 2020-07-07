package com.chrisa.covid19.features.area.presentation

import com.chrisa.covid19.features.area.presentation.models.AreaUiModel

sealed class AreaState {
    object Loading : AreaState()
    data class Success(val areaUiModel: AreaUiModel) : AreaState()
}
