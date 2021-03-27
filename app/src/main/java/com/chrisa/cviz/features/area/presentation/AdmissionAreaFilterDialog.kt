/*
 * Copyright 2021 Chris Anderson.
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

import android.content.Context
import android.view.LayoutInflater
import com.chrisa.cviz.R
import com.chrisa.cviz.admissionItem
import com.chrisa.cviz.databinding.AdmissionAreaFilterBinding
import com.chrisa.cviz.features.area.presentation.models.HospitalAdmissionsAreaModel
import com.google.android.material.bottomsheet.BottomSheetDialog

internal class AdmissionAreaFilterDialog(
    context: Context
) : BottomSheetDialog(context) {

    private val binding = AdmissionAreaFilterBinding.inflate(
        LayoutInflater.from(context),
        null,
        false
    )

    init {
        setContentView(binding.root)
    }

    fun bind(
        areaName: String,
        hospitalAdmissionsAreas: List<HospitalAdmissionsAreaModel>,
        listener: (List<HospitalAdmissionsAreaModel>) -> Unit
    ) {
        val selectedAreas =
            hospitalAdmissionsAreas.filter { it.isSelected }.toMutableSet()

        binding.title.text = context.getString(R.string.hospital_admissions_title, areaName)
        binding.recyclerView.withModels {
            hospitalAdmissionsAreas.forEach { admissionArea ->
                admissionItem {
                    id("admissionArea${admissionArea.areaName}")
                    title(admissionArea.areaName)
                    isSelected(admissionArea.isSelected)
                    onCheckedChanged { view, isChecked ->
                        if (!this@AdmissionAreaFilterDialog.isShowing) return@onCheckedChanged
                        if (isChecked) {
                            selectedAreas.add(admissionArea)
                        } else {
                            selectedAreas.remove(admissionArea)
                            if (selectedAreas.isEmpty()) {
                                selectedAreas.add(admissionArea)
                                view.isChecked = true
                            }
                        }
                    }
                }
            }
        }
        binding.apply.setOnClickListener {
            val areas =
                if (selectedAreas.isEmpty()) {
                    listOf(HospitalAdmissionsAreaModel(areaName = "", isSelected = true))
                } else {
                    hospitalAdmissionsAreas.map {
                        it.copy(isSelected = selectedAreas.contains(it))
                    }
                }
            listener.invoke(areas)
            dismiss()
        }
    }
}
