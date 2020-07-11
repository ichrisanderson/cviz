package com.chrisa.covid19.features.area.domain

import com.chrisa.covid19.features.area.data.AreaDataSource
import com.chrisa.covid19.features.area.data.dtos.SavedAreaDto
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class DeleteSavedAreaUseCaseTest {

    private val areaDataSource = mockk<AreaDataSource>()

    @Test
    fun `WHEN execute called THEN saved area is deleted`() {

        val areCode = "1234"
        val sut = DeleteSavedAreaUseCase(areaDataSource)

        val dto = SavedAreaDto(areCode)

        every { areaDataSource.deleteSavedArea(dto) } returns 1

        val deletedRows = sut.execute(areCode)

        assertThat(deletedRows).isEqualTo(1)
    }
}
