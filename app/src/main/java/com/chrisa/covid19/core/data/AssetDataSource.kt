package com.chrisa.covid19.core.data

import android.content.res.AssetManager
import com.chrisa.covid19.core.data.network.CasesModel
import com.chrisa.covid19.core.data.network.DeathsModel
import com.squareup.moshi.Moshi
import okio.buffer
import okio.source
import java.io.InputStream
import javax.inject.Inject

class AssetDataSource @Inject constructor(
    private val assetManager: AssetManager,
    private val moshi: Moshi
) {

    fun getCases(): CasesModel {
        val adapter = moshi.adapter(CasesModel::class.java)
        val cases = adapter.fromJson(casesFile().source().buffer())
        return cases!!
    }

    fun getDeaths(): DeathsModel {
        val adapter = moshi.adapter(DeathsModel::class.java)
        val deaths = adapter.fromJson(deathsFile().source().buffer())
        return deaths!!
    }

    private fun casesFile(): InputStream {
        return assetManager.open("coronavirus-cases_latest.json")
    }

    private fun deathsFile(): InputStream {
        return assetManager.open("coronavirus-deaths_latest.json")
    }
}
