package com.chrisa.cviz.features.area.domain

import com.chrisa.cviz.features.area.data.AreaAssociationDataSource
import com.chrisa.cviz.features.area.data.dtos.AreaAssociationTypeDto
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class InsertAreaAssociationUseCaseTest {

    private val dataSource: AreaAssociationDataSource = mockk()
    private val sut = InsertAreaAssociationUseCase(dataSource)

    @Before
    fun setup() {
        every { dataSource.insert(any(), any(), any()) } just Runs
    }

    @Test
    fun `WHEN insert called THEN data source updated`() {
        sut.execute("1", "2", AreaAssociationTypeDto.AREA_LOOKUP)

        verify { dataSource.insert("1", "2", AreaAssociationTypeDto.AREA_LOOKUP) }
    }
}
