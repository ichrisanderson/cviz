package com.chrisa.covid19.features.home.domain.models

data class NewCaseModel(
    val areaCode: String,
    val areaName: String,
    val areaType: String,
    val changeInCases: Int,
    val currentNewCases: Int
)
