package com.chrisa.covid19.features.area.domain

import com.chrisa.covid19.features.area.data.AreaDataSource
import com.chrisa.covid19.features.area.data.dtos.CaseDTO
import com.chrisa.covid19.features.area.data.dtos.MetadataDTO
import com.chrisa.covid19.features.area.domain.models.AreaDetailModel
import com.chrisa.covid19.features.area.domain.models.CaseModel
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.util.Date

class AreaUseCaseTest {

    private val areaDataSource = mockk<AreaDataSource>()

    private val sut = AreaUseCase(areaDataSource)

    @Test
    fun `WHEN execute called THEN area detail contains the latest cases for the area`() {

        val areaCode = "1234"

        val metadataDTO = MetadataDTO(
            lastUpdatedAt = Date(0)
        )

        val caseDTOs = (1 until 100).map {
            CaseDTO(
                date = Date(it.toLong()),
                dailyLabConfirmedCases = it
            )
        }

        every { areaDataSource.loadCaseMetadata() } returns metadataDTO
        every { areaDataSource.loadCases(areaCode) } returns caseDTOs

        val areaDetailModel = sut.execute(areaCode)

        val caseModels = caseDTOs.map {
            CaseModel(
                date = it.date,
                dailyLabConfirmedCases = it.dailyLabConfirmedCases
            )
        }

        assertThat(areaDetailModel).isEqualTo(
            AreaDetailModel(
                lastUpdatedAt = metadataDTO.lastUpdatedAt,
                allCases = caseModels,
                latestCases = caseModels.takeLast(7)
            )
        )
    }
}
