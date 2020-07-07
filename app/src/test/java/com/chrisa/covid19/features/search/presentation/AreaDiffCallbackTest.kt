package com.chrisa.covid19.features.search.presentation

import com.chrisa.covid19.features.search.domain.models.AreaModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AreaDiffCallbackTest {

    private val sut = AreaDiffCallback()

    @Test
    fun `GIVEN area codes are the same WHEN items are compared THEN areItemsTheSame returns true`() {

        val item1 = AreaModel(
            "001",
            "London"
        )
        val item2 = AreaModel(
            "001",
            "Birmingham"
        )

        assertThat(sut.areItemsTheSame(item1, item2)).isEqualTo(true)
    }

    @Test
    fun `GIVEN area codes are not the same WHEN items are compared THEN areItemsTheSame returns false`() {

        val item1 = AreaModel(
            "001",
            "London"
        )
        val item2 = AreaModel(
            "002",
            "Birmingham"
        )

        assertThat(sut.areItemsTheSame(item1, item2)).isEqualTo(false)
    }

    @Test
    fun `GIVEN contents are the same WHEN items are compared THEN areContentsTheSame returns true`() {

        val item1 = AreaModel(
            "001",
            "London"
        )
        val item2 = AreaModel(
            "001",
            "London"
        )

        assertThat(sut.areContentsTheSame(item1, item2)).isEqualTo(true)
    }

    @Test
    fun `GIVEN contents are the same WHEN items are compared THEN areContentsTheSame returns false`() {

        val item1 = AreaModel(
            "001",
            "London"
        )
        val item2 = AreaModel(
            "001",
            "Birmingham"
        )

        assertThat(sut.areContentsTheSame(item1, item2)).isEqualTo(false)
    }
}
