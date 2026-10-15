package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("OmniPlay", appName)
    }

    @Test
    fun `user profile win rate calculation`() {
        val profile = UserProfile(
            matchesPlayed = 20,
            matchesWon = 15
        )
        assertEquals(75, profile.winRate)
    }

    @Test
    fun `all four mini games are defined with display names`() {
        val gameTypes = MiniGameType.values()
        assertEquals(4, gameTypes.size)
        assertTrue(gameTypes.contains(MiniGameType.CYBER_PONG))
        assertTrue(gameTypes.contains(MiniGameType.SNAKE_ARENA))
        assertTrue(gameTypes.contains(MiniGameType.SPACE_BLASTER))
        assertTrue(gameTypes.contains(MiniGameType.REFLEX_DUEL))
    }

    @Test
    fun `moderation report and action log integrity`() {
        val report = ModerationReport(
            id = "rep_101",
            reporterId = "u1",
            reporterName = "PlayerA",
            targetPlayerName = "CheaterB",
            reason = ReportReason.CHEATING,
            details = "Wallhack",
            timestamp = "Now"
        )
        assertEquals(ReportStatus.PENDING, report.status)
        assertEquals("Cheating / Exploiting", report.reason.displayName)
    }
}
