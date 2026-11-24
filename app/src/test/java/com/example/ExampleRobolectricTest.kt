package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.mock.MockSportsData
import com.example.data.model.SportCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("BetZone X", appName)
    }

    @Test
    fun `sports catalog covers all 7 categories and 6 tournaments`() {
        val sportsInFixtures = MockSportsData.initialMatches.map { it.sport }.toSet()
        assertEquals(SportCategory.entries.toSet(), sportsInFixtures)
        assertTrue(MockSportsData.popularTournaments.size >= 6)
        assertTrue(MockSportsData.sportsQuizzes.isNotEmpty())
    }
}
