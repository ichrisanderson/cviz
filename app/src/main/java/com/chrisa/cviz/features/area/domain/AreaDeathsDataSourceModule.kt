/*
 * Copyright 2020 Chris Anderson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
