package com.chrisa.covid19.core.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27])
class DeathDaoTest {

    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @Test
    fun `GIVEN no deaths WHEN deathsCount called THEN count is zero`() {
        val count = db.deathsDao().deathsCount()
        assertThat(count).isEqualTo(0)
    }

    @Test
    fun `GIVEN deaths exist WHEN deathsCount called THEN count is not zero`() {
        db.deathsDao().insertAll(
            listOf(
                DeathEntity(
                    areaCode = "1234",
                    areaName = "UK",
                    date = Date(0),
                    dailyChangeInDeaths = 1,
                    cumulativeDeaths = 2
                )
            )
        )
        val count = db.deathsDao().deathsCount()
        assertThat(count).isEqualTo(1)
    }

    @Test
    fun `GIVEN death does not exist WHEN insertDeaths called THEN death is added`() {

        val newDeathsEntity = DeathEntity(
            areaCode = "001",
            areaName = "UK",
            date = Date(0),
            cumulativeDeaths = 0,
            dailyChangeInDeaths = 2
        )

        db.deathsDao().insertAll(listOf(newDeathsEntity))

        val deaths = db.deathsDao().searchAllDeathsOrderedByDateDesc(newDeathsEntity.areaCode)

        assertThat(deaths.size).isEqualTo(1)

        assertThat(deaths.first()).isEqualTo(newDeathsEntity)
    }

    @Test
    fun `GIVEN death does exist WHEN insertDeaths called with same date and area code THEN death is updated`() {

        val oldDeathsEntity = DeathEntity(
            areaCode = "001",
            areaName = "UK",
            date = Date(0),
            cumulativeDeaths = 0,
            dailyChangeInDeaths = 2
        )

        db.deathsDao().insertAll(listOf(oldDeathsEntity))

        val newDeathsEntity = DeathEntity(
            areaCode = "001",
            areaName = "UK",
            date = Date(0),
            cumulativeDeaths = 11,
            dailyChangeInDeaths = 22
        )

        db.deathsDao().insertAll(listOf(newDeathsEntity))

        val deaths = db.deathsDao().searchAllDeathsOrderedByDateDesc(oldDeathsEntity.areaCode)

        assertThat(deaths.size).isEqualTo(1)
        assertThat(deaths[0]).isEqualTo(newDeathsEntity)
    }

    @Test
    fun `GIVEN death does exist WHEN insertDeaths called with same area code and different date THEN death is added`() {

        val oldDeathsEntity = DeathEntity(
            areaCode = "001",
            areaName = "UK",
            date = Date(0),
            cumulativeDeaths = 0,
            dailyChangeInDeaths = 2
        )

        db.deathsDao().insertAll(listOf(oldDeathsEntity))

        val newDeathsEntity = DeathEntity(
            areaCode = "001",
            areaName = "UK",
            date = Date(1),
            cumulativeDeaths = 11,
            dailyChangeInDeaths = 22
        )
        db.deathsDao().insertAll(listOf(newDeathsEntity))

        val deaths = db.deathsDao().searchAllDeathsOrderedByDateDesc(oldDeathsEntity.areaCode)

        assertThat(deaths.size).isEqualTo(2)
        assertThat(deaths[0]).isEqualTo(newDeathsEntity)
        assertThat(deaths[1]).isEqualTo(oldDeathsEntity)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}
