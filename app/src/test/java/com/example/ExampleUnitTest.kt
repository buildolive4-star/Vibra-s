package com.example

import com.example.data.mock.MockSportsData
import com.example.data.model.SportCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun sports_catalog_covers_all_categories_and_tournaments() {
        val sports = MockSportsData.initialMatches.map { it.sport }.toSet()
        assertEquals(SportCategory.entries.toSet(), sports)
        assertTrue(MockSportsData.popularTournaments.size >= 6)
        assertTrue(MockSportsData.sportsQuizzes.isNotEmpty())
        assertTrue(MockSportsData.weeklyChallenges.isNotEmpty())
    }
}
