package com.chrisa.covid19.features.search.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class SearchQueryTransformerTest {

    val sut = SearchQueryTransformer()

    @Test
    fun test() {
        val originalQuery = "Test"
        val transFormedQuery = sut.transformQuery(originalQuery)
        assertThat(transFormedQuery).isEqualTo("$originalQuery%")
    }
}
