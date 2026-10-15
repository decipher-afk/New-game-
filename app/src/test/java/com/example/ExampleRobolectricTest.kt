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

    @Test
    fun `daily mission completion and rewards`() {
        val mission = DailyMission(
            id = "dm_test",
            title = "Orb Glutton",
            description = "Eat 10 glowing pellets",
            objectiveType = MissionObjectiveType.EAT_PELLETS,
            gameType = MiniGameType.SNAKE_ARENA,
            currentProgress = 8,
            targetProgress = 10,
            rewardXp = 200,
            rewardCoins = 250,
            rewardGems = 5,
            isCompleted = false,
            isClaimed = false,
            iconEmoji = "🐍"
        )

        assertFalse(mission.isCompleted)
        val updated = mission.copy(
            currentProgress = 10,
            isCompleted = true
        )
        assertTrue(updated.isCompleted)
        assertEquals(250, updated.rewardCoins)
        assertEquals(200, updated.rewardXp)
        assertEquals(5, updated.rewardGems)
    }

    @Test
    fun `user profile customization and lifetime currency`() {
        val initialProfile = UserProfile(
            username = "CyberViper",
            avatarEmoji = "⚡",
            level = 12,
            coins = 3450,
            gems = 120,
            totalCoinsEarned = 12450,
            totalGemsEarned = 380,
            statusBio = "Ready for combat!"
        )

        val updatedProfile = initialProfile.copy(
            username = "NeonGlitch",
            avatarEmoji = "🥷",
            statusBio = "Shadow striker on the leaderboard"
        )

        assertEquals("NeonGlitch", updatedProfile.username)
        assertEquals("🥷", updatedProfile.avatarEmoji)
        assertEquals("Shadow striker on the leaderboard", updatedProfile.statusBio)
        assertEquals(12, updatedProfile.level)
        assertEquals(12450, updatedProfile.totalCoinsEarned)
        assertEquals(380, updatedProfile.totalGemsEarned)
        assertEquals(3450, updatedProfile.coins)
        assertEquals(120, updatedProfile.gems)
    }
}
