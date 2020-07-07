package com.chrisa.covid19.core.data

import android.content.res.AssetManager
import com.chrisa.covid19.core.data.TestData.TEST_CASE_MODEL
import com.chrisa.covid19.core.data.TestData.TEST_DEATH_MODEL
import com.chrisa.covid19.core.data.network.CasesModel
import com.chrisa.covid19.core.data.network.DeathsModel
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import java.util.*

class AssetDataSourceTest {

    private val testDispatcher = TestCoroutineDispatcher()
    private val testCoroutineScope = TestCoroutineScope(testDispatcher)
    private val moshi = Moshi.Builder()
        .add(Date::class.java, Rfc3339DateJsonAdapter())
        .build()

    private val assetManager = mockk<AssetManager>(relaxed = true)

    private lateinit var sut: AssetDataSource

    @Before
    fun setup() {
        sut = AssetDataSource(assetManager, moshi)
    }

    @Test
    fun `GIVEN case model exists WHEN getCases is called THEN case model is returned`() =
        testCoroutineScope.runBlockingTest {
            val caseModel = TEST_CASE_MODEL

            val casesModelAdapter = moshi.adapter(CasesModel::class.java)
            val testCaseModelJson = casesModelAdapter.toJson(caseModel)
            every { assetManager.open("coronavirus-cases_latest.json") } returns testCaseModelJson.byteInputStream()

            val result = sut.getCases()
            assertThat(result).isEqualTo(caseModel)
        }

    @Test
    fun `GIVEN deaths model exists WHEN getDeaths is called THEN deaths model is returned`() =
        testCoroutineScope.runBlockingTest {

            val deathsModel = TEST_DEATH_MODEL

            val deathsModeldapter = moshi.adapter(DeathsModel::class.java)
            val deathsModelJson = deathsModeldapter.toJson(deathsModel)
            every { assetManager.open("coronavirus-deaths_latest.json") } returns deathsModelJson.byteInputStream()

            val result = sut.getDeaths()
            assertThat(result).isEqualTo(deathsModel)
        }
}


