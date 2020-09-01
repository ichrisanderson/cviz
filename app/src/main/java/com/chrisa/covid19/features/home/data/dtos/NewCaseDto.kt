package com.chrisa.covid19.features.home.data.dtos

data class NewCaseDto(
    val areaCode: String,
    val areaName: String,
    val areaType: String,
    val changeInCases: Int,
    val currentNewCases: Int
)
