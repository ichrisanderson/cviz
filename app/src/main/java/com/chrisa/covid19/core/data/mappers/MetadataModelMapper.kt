package com.chrisa.covid19.core.data.mappers

import com.chrisa.covid19.core.data.db.MetadataEntity
import com.chrisa.covid19.core.data.network.MetadataModel
import javax.inject.Inject

class MetadataModelMapper @Inject constructor() {
    fun mapToMetadataEntity(id: String, metadata: MetadataModel): MetadataEntity {
        return MetadataEntity(
            id = id,
            disclaimer = metadata.disclaimer,
            lastUpdatedAt = metadata.lastUpdatedAt
        )
    }
}

