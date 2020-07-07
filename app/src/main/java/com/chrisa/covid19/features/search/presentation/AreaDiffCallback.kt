package com.chrisa.covid19.features.search.presentation

import androidx.recyclerview.widget.DiffUtil
import com.chrisa.covid19.features.search.domain.models.AreaModel

class AreaDiffCallback : DiffUtil.ItemCallback<AreaModel>() {

    override fun areItemsTheSame(oldItem: AreaModel, newItem: AreaModel): Boolean {
        return oldItem.code == newItem.code
    }

    override fun areContentsTheSame(oldItem: AreaModel, newItem: AreaModel): Boolean {
        return oldItem == newItem
    }
}
