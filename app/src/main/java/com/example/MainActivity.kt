package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.ProfileAndAuthScreen
import com.example.dashboard.*
import com.example.game.CyberPongGameScreen
import com.example.game.ReflexDuelGameScreen
import com.example.game.RetroSnakeGameScreen
import com.example.game.SpaceBlasterGameScreen
import com.example.lobby.LobbyRoomScreen
import com.example.matchmaking.MatchmakingHubScreen
import com.example.missions.DailyMissionsScreen
import com.example.moderation.ModerationCenterScreen
import com.example.moderation.ReportUserDialog
import com.example.model.*
import com.example.tutorial.TutorialHubScreen
import com.example.ui.theme.*

enum class AppDestination(val label: String, val iconEmoji: String) {
    HOME("Arcade", "🕹️"),
    LOBBIES("Match", "⚔️"),
    MISSIONS("Missions", "🎯"),
    TUTORIALS("Academy", "🎓"),
    FRIENDS("Friends", "👥"),
    REWARDS("Shop", "🛍️")
}

sealed class ActiveGameFlow {
    data class Playing(val game: MiniGameType, val isSpectator: Boolean, val player1: String = "CyberViper", val player2: String = "NeonPhantom") : ActiveGameFlow()
    data class Tutorial(val game: MiniGameType) : ActiveGameFlow()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OmniPlayTheme {
                OmniPlayApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OmniPlayApp() {
    var currentDestination by remember { mutableStateOf(AppDestination.HOME) }
    var activeGameFlow by remember { mutableStateOf<ActiveGameFlow?>(null) }
    var activeLobby by remember { mutableStateOf<LobbyRoom?>(null) }
    var showProfileScreen by remember { mutableStateOf(false) }
    var showModerationCenter by remember { mutableStateOf(false) }
    var reportingTargetPlayer by remember { mutableStateOf<String?>(null) }

    // User Profile state
    var userProfile by remember {
        mutableStateOf(
            UserProfile(
                id = "u_player_1",
                username = "CyberViper",
                gamerTag = "#ARC-7729",
                avatarEmoji = "⚡",
                level = 12,
                xp = 1840,
                maxXp = 2500,
                coins = 3450,
                gems = 120,
                trophies = 1420,
                rankTier = "Platinum II",
                matchesPlayed = 68,
                matchesWon = 47,
                favoriteGame = MiniGameType.CYBER_PONG
            )
        )
    }

    // Daily Missions state
    var dailyMissions by remember {
        mutableStateOf(
            listOf(
                DailyMission(
                    id = "dm_1",
                    title = "Cyber Striker",
                    description = "Score 3 goals against opponents in Neon Cyber Pong",
                    objectiveType = MissionObjectiveType.SCORE_GOALS,
                    gameType = MiniGameType.CYBER_PONG,
                    currentProgress = 1,
                    targetProgress = 3,
                    rewardXp = 250,
                    rewardCoins = 300,
                    rewardGems = 5,
                    isCompleted = false,
                    isClaimed = false,
                    iconEmoji = "🏓",
                    difficulty = "Medium"
                ),
                DailyMission(
                    id = "dm_2",
                    title = "Orb Glutton",
                    description = "Eat 10 glowing energy pellets in Retro Snake Arena",
                    objectiveType = MissionObjectiveType.EAT_PELLETS,
                    gameType = MiniGameType.SNAKE_ARENA,
                    currentProgress = 10,
                    targetProgress = 10,
                    rewardXp = 200,
                    rewardCoins = 250,
                    rewardGems = 0,
                    isCompleted = true,
                    isClaimed = false,
                    iconEmoji = "🐍",
                    difficulty = "Easy"
                ),
                DailyMission(
                    id = "dm_3",
                    title = "Starfield Sweeper",
                    description = "Shoot down 12 asteroids in Space Blaster Survivors",
                    objectiveType = MissionObjectiveType.DESTROY_METEORS,
                    gameType = MiniGameType.SPACE_BLASTER,
                    currentProgress = 5,
                    targetProgress = 12,
                    rewardXp = 350,
                    rewardCoins = 400,
                    rewardGems = 10,
                    isCompleted = false,
                    isClaimed = false,
                    iconEmoji = "🚀",
                    difficulty = "Hard"
                )
            )
        )
    }
    var isMegaChestClaimed by remember { mutableStateOf(false) }

    val alternateMissions = remember {
        listOf(
            DailyMission("dm_alt1", "Reflex Surge", "Achieve a 4x combo streak in Tap Blitz", MissionObjectiveType.REACH_COMBO, MiniGameType.REFLEX_DUEL, 0, 4, 250, 300, 5, false, false, "⚡", "Medium"),
            DailyMission("dm_alt2", "Arena Victor", "Win 1 match in any multiplayer game mode", MissionObjectiveType.WIN_MATCH, null, 0, 1, 300, 350, 10, false, false, "🏆", "Hard"),
            DailyMission("dm_alt3", "Spectator Hype", "Watch 1 live match and cheer the contenders", MissionObjectiveType.SPECTATE_CHEER, null, 0, 1, 150, 200, 0, false, false, "👁️", "Easy")
        )
    }

    // Helper to increment mission progress
    fun recordMissionProgress(type: MissionObjectiveType, amount: Int) {
        dailyMissions = dailyMissions.map { mission ->
            if (mission.objectiveType == type && !mission.isCompleted) {
                val newProgress = (mission.currentProgress + amount).coerceAtMost(mission.targetProgress)
                mission.copy(
                    currentProgress = newProgress,
                    isCompleted = newProgress >= mission.targetProgress
                )
            } else mission
        }
    }

    // Completed Tutorials state
    var completedTutorials by remember {
        mutableStateOf(setOf(MiniGameType.CYBER_PONG))
    }

    // Lobbies state
    var lobbies by remember {
        mutableStateOf(
            listOf(
                LobbyRoom(
                    id = "lob_1",
                    code = "ARC924",
                    name = "Neon Cyber Showdown",
                    gameType = MiniGameType.CYBER_PONG,
                    maxPlayers = 2,
                    currentPlayers = listOf(
                        PlayerSlot("u1", "CyberViper", "⚡", isReady = true),
                        PlayerSlot("u2", "NeonPhantom", "🔥", isReady = true, isHost = true)
                    ),
                    hostId = "u2",
                    hostName = "NeonPhantom",
                    spectatorsCount = 14,
                    pingMs = 24
                ),
                LobbyRoom(
                    id = "lob_2",
                    code = "SLT882",
                    name = "Snake Slither Colosseum",
                    gameType = MiniGameType.SNAKE_ARENA,
                    maxPlayers = 2,
                    currentPlayers = listOf(
                        PlayerSlot("u3", "ViperKing", "🐍", isReady = true, isHost = true)
                    ),
                    hostId = "u3",
                    hostName = "ViperKing",
                    spectatorsCount = 8,
                    pingMs = 31
                ),
                LobbyRoom(
                    id = "lob_3",
                    code = "SPC104",
                    name = "Starlight Dogfight Arena",
                    gameType = MiniGameType.SPACE_BLASTER,
                    maxPlayers = 2,
                    currentPlayers = listOf(
                        PlayerSlot("u4", "StarLord99", "🚀", isReady = true, isHost = true)
                    ),
                    hostId = "u4",
                    hostName = "StarLord99",
                    spectatorsCount = 22,
                    pingMs = 19
                ),
                LobbyRoom(
                    id = "lob_4",
                    code = "TAP551",
                    name = "Hyper Reflex Speedrun",
                    gameType = MiniGameType.REFLEX_DUEL,
                    maxPlayers = 2,
                    currentPlayers = listOf(
                        PlayerSlot("u5", "FlashMaster", "⚡", isReady = false, isHost = true)
                    ),
                    hostId = "u5",
                    hostName = "FlashMaster",
                    spectatorsCount = 11,
                    pingMs = 26
                )
            )
        )
    }

    // Friends state
    var friends by remember {
        mutableStateOf(
            listOf(
                Friend("f1", "NeonPhantom", "#ARC-1029", "🔥", FriendStatus.IN_MATCH, "Neon Cyber Pong (3-2)", "lob_1", 1380),
                Friend("f2", "StarLord99", "#ARC-4412", "🚀", FriendStatus.IN_LOBBY, "Starlight Dogfight Arena", "lob_3", 1520),
                Friend("f3", "ShadowStrike", "#ARC-9910", "🥷", FriendStatus.ONLINE, "Browsing Store", null, 1290),
                Friend("f4", "PixelQueen", "#ARC-6623", "👑", FriendStatus.OFFLINE, "Last seen 2h ago", null, 1150)
            )
        )
    }

    // Achievements state
    var achievements by remember {
        mutableStateOf(
            listOf(
                Achievement("a1", "First Blood", "Win your first multiplayer arcade match", "⚔️", 1, 1, isUnlocked = true, isClaimed = true, rewardCoins = 100, rewardXp = 100),
                Achievement("a2", "Paddle Maestro", "Score 10 goals in Cyber Pong", "🏓", 7, 10, isUnlocked = false, isClaimed = false, rewardCoins = 250, rewardXp = 200),
                Achievement("a3", "Snake Charmer", "Collect 20 glowing energy pellets in Snake Arena", "🐍", 20, 20, isUnlocked = true, isClaimed = false, rewardCoins = 300, rewardXp = 250),
                Achievement("a4", "Ace Starfighter", "Shoot down 25 meteors in Space Blaster", "🚀", 18, 25, isUnlocked = false, isClaimed = false, rewardCoins = 350, rewardXp = 300),
                Achievement("a5", "Lightning Reflex", "Reach a 5x combo in Tap Blitz Duel", "⚡", 5, 5, isUnlocked = true, isClaimed = false, rewardCoins = 200, rewardXp = 150),
                Achievement("a6", "Master Spectator", "Cheer and watch 3 live ongoing matches", "👁️", 3, 3, isUnlocked = true, isClaimed = false, rewardCoins = 150, rewardXp = 100),
                Achievement("a7", "Community Guardian", "Submit or review a violation report", "🛡️", 1, 1, isUnlocked = true, isClaimed = false, rewardCoins = 200, rewardXp = 150)
            )
        )
    }

    // Store state
    var storeItems by remember {
        mutableStateOf(
            listOf(
                StoreItem("s1", "Neon Cyan Paddle", StoreCategory.PADDLE_SKIN, 800, 0, isUnlocked = true, isEquipped = true, "Glowing cyan edge with laser trail", "🏓"),
                StoreItem("s2", "Solar Flare Paddle", StoreCategory.PADDLE_SKIN, 1200, 0, isUnlocked = false, isEquipped = false, "Blazing orange plasma finish", "🔥"),
                StoreItem("s3", "Quantum Sparkle Trail", StoreCategory.SNAKE_TRAIL, 1000, 0, isUnlocked = true, isEquipped = true, "Glittering particle tail for snake", "✨"),
                StoreItem("s4", "Void Dragon Skin", StoreCategory.SNAKE_TRAIL, 2500, 40, isUnlocked = false, isEquipped = false, "Dark cosmic matter serpent scales", "🐉"),
                StoreItem("s5", "Cyber Samurai Avatar", StoreCategory.AVATAR, 600, 0, isUnlocked = false, isEquipped = false, "Futuristic neon ronin crest", "🥷"),
                StoreItem("s6", "Arcade Legend Title", StoreCategory.TITLE, 1500, 20, isUnlocked = false, isEquipped = false, "Exclusive veteran gamer tag suffix", "👑")
            )
        )
    }

    // Moderation state
    var moderationReports by remember {
        mutableStateOf(
            listOf(
                ModerationReport(
                    id = "rep_1",
                    reporterId = "u_phantom",
                    reporterName = "NeonPhantom",
                    targetPlayerName = "ToxicGamer99",
                    reason = ReportReason.HARASSMENT,
                    details = "Repeated offensive insults in lobby chat during rematch.",
                    timestamp = "10 mins ago",
                    matchOrLobby = "Lobby #ARC924"
                ),
                ModerationReport(
                    id = "rep_2",
                    reporterId = "u_viper",
                    reporterName = "CyberViper",
                    targetPlayerName = "SpeedBot_X",
                    reason = ReportReason.CHEATING,
                    details = "Instantaneous 1ms reflexes in Tap Blitz without human delay.",
                    timestamp = "25 mins ago",
                    matchOrLobby = "Lobby #TAP551"
                )
            )
        )
    }

    var moderationActionLogs by remember {
        mutableStateOf(
            listOf(
                ModerationActionLog(
                    id = "act_1",
                    targetPlayerName = "TrollKing",
                    actionType = ModerationActionType.MUTE_1H,
                    reason = "Toxic Chat in Space Blaster",
                    moderatorNotes = "Chat logs confirmed hostility",
                    timestamp = "1 hour ago"
                )
            )
        )
    }

    // Back handling
    BackHandler(
        enabled = activeGameFlow != null || activeLobby != null || showProfileScreen || showModerationCenter
    ) {
        if (activeGameFlow != null) {
            activeGameFlow = null
        } else if (activeLobby != null) {
            activeLobby = null
        } else if (showProfileScreen) {
            showProfileScreen = false
        } else if (showModerationCenter) {
            showModerationCenter = false
        }
    }

    // ACTIVE GAME FLOW (Cyber Pong / Retro Snake / Space Blaster / Reflex Duel)
    activeGameFlow?.let { flow ->
        when (flow) {
            is ActiveGameFlow.Playing -> {
                when (flow.game) {
                    MiniGameType.CYBER_PONG -> {
                        CyberPongGameScreen(
                            isSpectator = flow.isSpectator,
                            isTutorial = false,
                            player1Name = flow.player1,
                            player2Name = flow.player2,
                            onExitGame = { activeGameFlow = null },
                            onMatchFinished = { winner, s1, s2 ->
                                val xpEarned = if (winner == userProfile.username) 120 else 50
                                val coinsEarned = if (winner == userProfile.username) 150 else 60
                                userProfile = userProfile.copy(
                                    xp = (userProfile.xp + xpEarned).coerceAtMost(userProfile.maxXp),
                                    coins = userProfile.coins + coinsEarned,
                                    totalCoinsEarned = userProfile.totalCoinsEarned + coinsEarned,
                                    matchesPlayed = userProfile.matchesPlayed + 1,
                                    matchesWon = userProfile.matchesWon + (if (winner == userProfile.username) 1 else 0),
                                    trophies = userProfile.trophies + (if (winner == userProfile.username) 25 else -10)
                                )
                                recordMissionProgress(MissionObjectiveType.SCORE_GOALS, s1)
                                if (winner == userProfile.username) {
                                    recordMissionProgress(MissionObjectiveType.WIN_MATCH, 1)
                                }
                                if (flow.isSpectator) {
                                    recordMissionProgress(MissionObjectiveType.SPECTATE_CHEER, 1)
                                }
                            }
                        )
                    }
                    MiniGameType.SNAKE_ARENA -> {
                        RetroSnakeGameScreen(
                            isSpectator = flow.isSpectator,
                            isTutorial = false,
                            player1Name = flow.player1,
                            player2Name = flow.player2,
                            onExitGame = { activeGameFlow = null },
                            onMatchFinished = { winner, s1, s2 ->
                                val xpEarned = if (winner == userProfile.username) 120 else 50
                                val coinsEarned = if (winner == userProfile.username) 150 else 60
                                userProfile = userProfile.copy(
                                    xp = (userProfile.xp + xpEarned).coerceAtMost(userProfile.maxXp),
                                    coins = userProfile.coins + coinsEarned,
                                    totalCoinsEarned = userProfile.totalCoinsEarned + coinsEarned,
                                    matchesPlayed = userProfile.matchesPlayed + 1,
                                    matchesWon = userProfile.matchesWon + (if (winner == userProfile.username) 1 else 0)
                                )
                                recordMissionProgress(MissionObjectiveType.EAT_PELLETS, s1 / 10)
                                if (winner == userProfile.username) {
                                    recordMissionProgress(MissionObjectiveType.WIN_MATCH, 1)
                                }
                            }
                        )
                    }
                    MiniGameType.SPACE_BLASTER -> {
                        SpaceBlasterGameScreen(
                            isSpectator = flow.isSpectator,
                            isTutorial = false,
                            player1Name = flow.player1,
                            player2Name = flow.player2,
                            onExitGame = { activeGameFlow = null },
                            onMatchFinished = { winner, s1, s2 ->
                                userProfile = userProfile.copy(
                                    xp = (userProfile.xp + 100).coerceAtMost(userProfile.maxXp),
                                    coins = userProfile.coins + 120,
                                    totalCoinsEarned = userProfile.totalCoinsEarned + 120,
                                    matchesPlayed = userProfile.matchesPlayed + 1
                                )
                                recordMissionProgress(MissionObjectiveType.DESTROY_METEORS, (s1 / 50).coerceAtLeast(1))
                            }
                        )
                    }
                    MiniGameType.REFLEX_DUEL -> {
                        ReflexDuelGameScreen(
                            isSpectator = flow.isSpectator,
                            isTutorial = false,
                            player1Name = flow.player1,
                            player2Name = flow.player2,
                            onExitGame = { activeGameFlow = null },
                            onMatchFinished = { winner, s1, s2 ->
                                userProfile = userProfile.copy(
                                    xp = (userProfile.xp + 100).coerceAtMost(userProfile.maxXp),
                                    coins = userProfile.coins + 120,
                                    totalCoinsEarned = userProfile.totalCoinsEarned + 120,
                                    matchesPlayed = userProfile.matchesPlayed + 1
                                )
                                recordMissionProgress(MissionObjectiveType.REACH_COMBO, (s1 / 20).coerceAtLeast(1))
                            }
                        )
                    }
                }
            }
            is ActiveGameFlow.Tutorial -> {
                when (flow.game) {
                    MiniGameType.CYBER_PONG -> {
                        CyberPongGameScreen(
                            isSpectator = false,
                            isTutorial = true,
                            onExitGame = { activeGameFlow = null },
                            onTutorialComplete = {
                                completedTutorials = completedTutorials + MiniGameType.CYBER_PONG
                                userProfile = userProfile.copy(
                                    xp = (userProfile.xp + 150).coerceAtMost(userProfile.maxXp),
                                    coins = userProfile.coins + 200
                                )
                            }
                        )
                    }
                    MiniGameType.SNAKE_ARENA -> {
                        RetroSnakeGameScreen(
                            isSpectator = false,
                            isTutorial = true,
                            onExitGame = { activeGameFlow = null },
                            onTutorialComplete = {
                                completedTutorials = completedTutorials + MiniGameType.SNAKE_ARENA
                                userProfile = userProfile.copy(
                                    xp = (userProfile.xp + 150).coerceAtMost(userProfile.maxXp),
                                    coins = userProfile.coins + 200
                                )
                            }
                        )
                    }
                    MiniGameType.SPACE_BLASTER -> {
                        SpaceBlasterGameScreen(
                            isSpectator = false,
                            isTutorial = true,
                            onExitGame = { activeGameFlow = null },
                            onTutorialComplete = {
                                completedTutorials = completedTutorials + MiniGameType.SPACE_BLASTER
                                userProfile = userProfile.copy(
                                    xp = (userProfile.xp + 150).coerceAtMost(userProfile.maxXp),
                                    coins = userProfile.coins + 200
                                )
                            }
                        )
                    }
                    MiniGameType.REFLEX_DUEL -> {
                        ReflexDuelGameScreen(
                            isSpectator = false,
                            isTutorial = true,
                            onExitGame = { activeGameFlow = null },
                            onTutorialComplete = {
                                completedTutorials = completedTutorials + MiniGameType.REFLEX_DUEL
                                userProfile = userProfile.copy(
                                    xp = (userProfile.xp + 150).coerceAtMost(userProfile.maxXp),
                                    coins = userProfile.coins + 200
                                )
                            }
                        )
                    }
                }
            }
        }
        return
    }

    // ACTIVE LOBBY ROOM SCREEN
    activeLobby?.let { lobby ->
        LobbyRoomScreen(
            lobby = lobby,
            currentUser = userProfile,
            onStartGame = { isSpectator ->
                activeLobby = null
                activeGameFlow = ActiveGameFlow.Playing(
                    game = lobby.gameType,
                    isSpectator = isSpectator,
                    player1 = userProfile.username,
                    player2 = lobby.hostName
                )
            },
            onLeaveLobby = { activeLobby = null },
            onReportPlayer = { targetPlayer ->
                reportingTargetPlayer = targetPlayer
            }
        )
        return
    }

    // MODERATION CENTER SCREEN
    if (showModerationCenter) {
        ModerationCenterScreen(
            reports = moderationReports,
            actionLogs = moderationActionLogs,
            onTakeAction = { reportId, action, targetPlayer, reason, notes ->
                // Update report status
                moderationReports = moderationReports.map {
                    if (it.id == reportId) {
                        it.copy(
                            status = when (action) {
                                ModerationActionType.WARNING -> ReportStatus.RESOLVED_WARNED
                                ModerationActionType.MUTE_1H, ModerationActionType.MUTE_24H -> ReportStatus.RESOLVED_MUTED
                                ModerationActionType.BAN -> ReportStatus.RESOLVED_BANNED
                                ModerationActionType.DISMISS -> ReportStatus.DISMISSED
                            }
                        )
                    } else it
                }
                // Add action log
                moderationActionLogs = listOf(
                    ModerationActionLog(
                        id = "act_${System.currentTimeMillis()}",
                        targetPlayerName = targetPlayer,
                        actionType = action,
                        reason = reason,
                        moderatorNotes = notes,
                        timestamp = "Just now"
                    )
                ) + moderationActionLogs
            },
            onBack = { showModerationCenter = false }
        )
        return
    }

    // PROFILE SCREEN
    if (showProfileScreen) {
        ProfileAndAuthScreen(
            user = userProfile,
            onUpdateProfile = { newName, newAvatar, newBio ->
                userProfile = userProfile.copy(
                    username = newName,
                    avatarEmoji = newAvatar,
                    statusBio = newBio
                )
            },
            onOpenModerationCenter = {
                showProfileScreen = false
                showModerationCenter = true
            },
            onBack = { showProfileScreen = false }
        )
        return
    }

    // MAIN SCAFFOLD & DASHBOARD
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("OmniPlay", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = NeonViolet.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("ARENA", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                },
                actions = {
                    // Quick currency view
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Surface(
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🪙", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${userProfile.coins}", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Profile icon button
                    IconButton(
                        onClick = { showProfileScreen = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .background(DarkSurfaceVariant, CircleShape)
                    ) {
                        Text(userProfile.avatarEmoji, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = NeonCyan
            ) {
                AppDestination.values().forEach { dest ->
                    val isSelected = currentDestination == dest
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentDestination = dest },
                        icon = {
                            Text(dest.iconEmoji, fontSize = if (isSelected) 20.sp else 16.sp)
                        },
                        label = {
                            Text(
                                dest.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) NeonCyan else TextSecondary
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NeonCyan,
                            unselectedIconColor = TextSecondary,
                            indicatorColor = NeonCyan.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.HOME -> {
                    MainArcadeHomeScreen(
                        user = userProfile,
                        dailyMissions = dailyMissions,
                        onOpenDailyMissions = { currentDestination = AppDestination.MISSIONS },
                        onPlayGame = { gameType ->
                            activeGameFlow = ActiveGameFlow.Playing(gameType, isSpectator = false)
                        },
                        onOpenTutorial = { gameType ->
                            activeGameFlow = ActiveGameFlow.Tutorial(gameType)
                        },
                        onSpectateGame = { gameType ->
                            activeGameFlow = ActiveGameFlow.Playing(gameType, isSpectator = true)
                        },
                        onOpenMatchmaking = { currentDestination = AppDestination.LOBBIES },
                        onOpenAcademy = { currentDestination = AppDestination.TUTORIALS },
                        onOpenModeration = { showModerationCenter = true },
                        onClaimDailyReward = {
                            userProfile = userProfile.copy(coins = userProfile.coins + 100)
                        }
                    )
                }
                AppDestination.LOBBIES -> {
                    MatchmakingHubScreen(
                        lobbies = lobbies,
                        onSelectLobby = { selectedLobby ->
                            activeLobby = selectedLobby
                        },
                        onWatchLiveGame = { room ->
                            activeGameFlow = ActiveGameFlow.Playing(
                                game = room.gameType,
                                isSpectator = true,
                                player1 = "CyberViper",
                                player2 = room.hostName
                            )
                        },
                        onCreateLobby = { name, game, isPrivate, allowSpectators ->
                            val newLobby = LobbyRoom(
                                id = "lob_${System.currentTimeMillis()}",
                                code = "ARC" + (100..999).random(),
                                name = name,
                                gameType = game,
                                maxPlayers = 2,
                                isPrivate = isPrivate,
                                hostId = userProfile.id,
                                hostName = userProfile.username,
                                allowedSpectators = allowSpectators,
                                spectatorsCount = 0,
                                pingMs = 22
                            )
                            lobbies = listOf(newLobby) + lobbies
                            activeLobby = newLobby
                        },
                        onQuickMatchFound = { gameType ->
                            activeGameFlow = ActiveGameFlow.Playing(gameType, isSpectator = false)
                        },
                        onBack = { currentDestination = AppDestination.HOME }
                    )
                }
                AppDestination.MISSIONS -> {
                    DailyMissionsScreen(
                        missions = dailyMissions,
                        onClaimMission = { mission ->
                            dailyMissions = dailyMissions.map {
                                if (it.id == mission.id) it.copy(isClaimed = true) else it
                            }
                            val newXp = userProfile.xp + mission.rewardXp
                            val leveledUp = newXp >= userProfile.maxXp
                            userProfile = userProfile.copy(
                                coins = userProfile.coins + mission.rewardCoins,
                                gems = userProfile.gems + mission.rewardGems,
                                totalCoinsEarned = userProfile.totalCoinsEarned + mission.rewardCoins,
                                totalGemsEarned = userProfile.totalGemsEarned + mission.rewardGems,
                                xp = if (leveledUp) newXp - userProfile.maxXp else newXp,
                                level = if (leveledUp) userProfile.level + 1 else userProfile.level
                            )
                        },
                        onRerollMission = { mission ->
                            val alt = alternateMissions.random()
                            dailyMissions = dailyMissions.map {
                                if (it.id == mission.id) alt.copy(id = "dm_${System.currentTimeMillis()}") else it
                            }
                        },
                        onPlayMissionGame = { gameType ->
                            activeGameFlow = ActiveGameFlow.Playing(gameType, isSpectator = false)
                        },
                        onClaimMegaChest = {
                            isMegaChestClaimed = true
                            userProfile = userProfile.copy(
                                coins = userProfile.coins + 500,
                                gems = userProfile.gems + 25,
                                totalCoinsEarned = userProfile.totalCoinsEarned + 500,
                                totalGemsEarned = userProfile.totalGemsEarned + 25,
                                xp = (userProfile.xp + 400).coerceAtMost(userProfile.maxXp)
                            )
                        },
                        isMegaChestClaimed = isMegaChestClaimed,
                        onBack = { currentDestination = AppDestination.HOME }
                    )
                }
                AppDestination.TUTORIALS -> {
                    TutorialHubScreen(
                        completedTutorials = completedTutorials,
                        onStartTutorial = { gameType ->
                            activeGameFlow = ActiveGameFlow.Tutorial(gameType)
                        },
                        onBack = { currentDestination = AppDestination.HOME }
                    )
                }
                AppDestination.FRIENDS -> {
                    FriendsDashboardScreen(
                        friends = friends,
                        onAddFriend = { tag ->
                            val newFriend = Friend(
                                id = "f_${System.currentTimeMillis()}",
                                name = "Gamer_${(10..99).random()}",
                                tag = tag,
                                avatar = "🎮",
                                status = FriendStatus.ONLINE,
                                currentActivity = "Looking for Match",
                                trophies = 1200
                            )
                            friends = friends + newFriend
                        },
                        onInviteFriend = { friend ->
                            activeLobby = lobbies.first()
                        },
                        onSpectateFriend = { friend ->
                            activeGameFlow = ActiveGameFlow.Playing(
                                game = MiniGameType.CYBER_PONG,
                                isSpectator = true,
                                player1 = friend.name,
                                player2 = "NeonPhantom"
                            )
                        },
                        onReportPlayer = { targetPlayer ->
                            reportingTargetPlayer = targetPlayer
                        }
                    )
                }
                AppDestination.REWARDS -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        var shopOrAchievementsTab by remember { mutableIntStateOf(0) }
                        TabRow(
                            selectedTabIndex = shopOrAchievementsTab,
                            containerColor = DarkSurface,
                            contentColor = NeonCyan
                        ) {
                            Tab(selected = shopOrAchievementsTab == 0, onClick = { shopOrAchievementsTab = 0 }, text = { Text("Cosmetics Shop") })
                            Tab(selected = shopOrAchievementsTab == 1, onClick = { shopOrAchievementsTab = 1 }, text = { Text("Achievements (${achievements.count { it.isUnlocked && !it.isClaimed }})") })
                            Tab(selected = shopOrAchievementsTab == 2, onClick = { shopOrAchievementsTab = 2 }, text = { Text("Leaderboard 🏆") })
                        }

                        when (shopOrAchievementsTab) {
                            0 -> {
                                ArcadeShopScreen(
                                    storeItems = storeItems,
                                    userCoins = userProfile.coins,
                                    userGems = userProfile.gems,
                                    onBuyItem = { item ->
                                        if (userProfile.coins >= item.priceCoins && userProfile.gems >= item.priceGems) {
                                            userProfile = userProfile.copy(
                                                coins = userProfile.coins - item.priceCoins,
                                                gems = userProfile.gems - item.priceGems
                                            )
                                            storeItems = storeItems.map {
                                                if (it.id == item.id) it.copy(isUnlocked = true) else it
                                            }
                                        }
                                    },
                                    onEquipItem = { item ->
                                        storeItems = storeItems.map {
                                            if (it.category == item.category) {
                                                it.copy(isEquipped = it.id == item.id)
                                            } else it
                                        }
                                        if (item.category == StoreCategory.TITLE) {
                                            userProfile = userProfile.copy(equippedTitle = item.title)
                                        }
                                    }
                                )
                            }
                            1 -> {
                                AchievementsScreen(
                                    achievements = achievements,
                                    onClaimReward = { ach ->
                                        achievements = achievements.map {
                                            if (it.id == ach.id) it.copy(isClaimed = true) else it
                                        }
                                        userProfile = userProfile.copy(
                                            coins = userProfile.coins + ach.rewardCoins,
                                            xp = (userProfile.xp + ach.rewardXp).coerceAtMost(userProfile.maxXp)
                                        )
                                    }
                                )
                            }
                            2 -> {
                                LeaderboardScreen()
                            }
                        }
                    }
                }
            }
        }
    }

    // Report Player Dialog
    reportingTargetPlayer?.let { target ->
        ReportUserDialog(
            targetPlayerName = target,
            currentLobbyName = activeLobby?.name ?: "Global Arcade",
            onDismiss = { reportingTargetPlayer = null },
            onSubmitReport = { reason, details ->
                moderationReports = listOf(
                    ModerationReport(
                        id = "rep_${System.currentTimeMillis()}",
                        reporterId = userProfile.id,
                        reporterName = userProfile.username,
                        targetPlayerName = target,
                        reason = reason,
                        details = details,
                        timestamp = "Just now",
                        matchOrLobby = activeLobby?.name ?: "Global Match"
                    )
                ) + moderationReports
                reportingTargetPlayer = null
            }
        )
    }
}
