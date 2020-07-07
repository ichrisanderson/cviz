package com.chrisa.covid19.features.search.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.chrisa.covid19.R
import com.chrisa.covid19.features.search.domain.models.AreaModel
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor

class AreaAdapter : ListAdapter<AreaModel, AreaAdapter.ViewHolder>(
    AreaDiffCallback()
) {

    private val _clickEvent: PublishProcessor<AreaModel> = PublishProcessor.create<AreaModel>()

    val clickEvent: Flowable<AreaModel>
        get() = _clickEvent

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val area = getItem(position)
        holder.bind(area) { _clickEvent.onNext(area) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_area, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(
        private val view: View
    ) : RecyclerView.ViewHolder(view) {

        private val text: TextView = view.findViewById(
            R.id.areaTitle
        )

        fun bind(item: AreaModel, onClickListener: (View) -> Unit) {
            view.setOnClickListener(onClickListener)
            text.text = item.name
        }
    }
}
