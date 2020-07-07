package com.chrisa.covid19.features.startup.domain

import com.chrisa.covid19.core.data.Bootstrapper
import javax.inject.Inject

class BootstrapDataUseCase @Inject constructor(
    private val bootstrapper: Bootstrapper
) {
    suspend fun execute() {
        bootstrapper.bootstrapData()
    }
}
