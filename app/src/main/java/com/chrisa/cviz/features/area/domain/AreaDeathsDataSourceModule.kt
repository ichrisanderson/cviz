package com.chrisa.cviz.features.area.domain

import com.chrisa.cviz.features.area.data.AreaDeathsByPublishedDateDataSource
import com.chrisa.cviz.features.area.data.AreaOnsDeathsDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
annotation class PublishedDeaths

@Qualifier
@MustBeDocumented
annotation class OnsDeaths

@InstallIn(ApplicationComponent::class)
@Module
class AreaDeathsDataSourceModule {
    @PublishedDeaths
    @Provides
    fun areaDeathsByPublishedDateUseCase(
        areaLookupUseCase: AreaLookupUseCase,
        areaDeathsByPublishedDateDataSource: AreaDeathsByPublishedDateDataSource
    ): AreaDeathsUseCase {
        return AreaDeathsUseCase(areaLookupUseCase, areaDeathsByPublishedDateDataSource)
    }

    @OnsDeaths
    @Provides
    fun areaOnsDeathsUseCase(
        areaLookupUseCase: AreaLookupUseCase,
        areaOnsDeathsDataSource: AreaOnsDeathsDataSource
    ): AreaDeathsUseCase {
        return AreaDeathsUseCase(areaLookupUseCase, areaOnsDeathsDataSource)
    }
}
