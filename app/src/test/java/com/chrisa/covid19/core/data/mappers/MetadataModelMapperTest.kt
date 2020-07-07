package com.chrisa.covid19.core.data.mappers

import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.MetadataModel
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.util.*

class MetadataModelMapperTest {

    private val sut = MetadataModelMapper()

    @Test
    fun `WHEN mapToMetadataEntity called THEN metadataEntity is returned`() {

        val id = "Foo"
        val metadataModel = MetadataModel(
            disclaimer = "New metadata",
            lastUpdatedAt = Date(1)
        )

        val entity = sut.mapToMetadataEntity(id, metadataModel)

        assertThat(entity).isEqualTo(
            MetadataEntity(
                id = id,
                disclaimer = metadataModel.disclaimer,
                lastUpdatedAt = metadataModel.lastUpdatedAt
            )
        )
    }
}
