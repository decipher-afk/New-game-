package com.example.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainArcadeHomeScreen(
    user: UserProfile,
    onPlayGame: (MiniGameType) -> Unit,
    onOpenTutorial: (MiniGameType) -> Unit,
    onSpectateGame: (MiniGameType) -> Unit,
    onOpenMatchmaking: () -> Unit,
    onOpenAcademy: () -> Unit,
    onOpenModeration: () -> Unit,
    onOpenDailyMissions: () -> Unit = {},
    dailyMissions: List<DailyMission> = emptyList(),
    onClaimDailyReward: () -> Unit
) {
    var showDailyRewardDialog by remember { mutableStateOf(false) }
    var dailyRewardClaimed by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Player Hero Card with Level & Economy
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_profile_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonViolet.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(
                                        Brush.linearGradient(listOf(NeonViolet, NeonCyan)),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user.avatarEmoji, fontSize = 26.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.username, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = NeonViolet.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(user.gamerTag, color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                                Text("Title: ${user.equippedTitle} · ${user.rankTier}", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        // Daily Reward Button
                        IconButton(
                            onClick = { showDailyRewardDialog = true },
                            modifier = Modifier
                                .size(42.dp)
                                .background(DarkSurfaceVariant, CircleShape)
                        ) {
                            Text("🎁", fontSize = 20.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // XP Level Progress Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Level ${user.level}", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("${user.xp} / ${user.maxXp} XP", color = TextSecondary, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (user.xp.toFloat() / user.maxXp).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = NeonCyan,
                        trackColor = DarkCardBorder
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // In-Game Currencies & Trophies Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🪙", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${user.coins}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(DarkCardBorder))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💎", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${user.gems}", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Box(modifier = Modifier.width(1.dp).height(20.dp).background(DarkCardBorder))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏆", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${user.trophies}", color = NeonMagenta, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // Action Hub Quick Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    onClick = onOpenMatchmaking,
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚔️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Matchmaking", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Lobbies & Queue", color = TextSecondary, fontSize = 10.sp)
                    }
                }

                Card(
                    onClick = onOpenAcademy,
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonLime.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎓", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Academy", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("In-Game Tutorials", color = TextSecondary, fontSize = 10.sp)
                    }
                }

                Card(
                    onClick = onOpenModeration,
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🛡️", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Moderation", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Trust & Safety", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }

        // Daily Rotating Missions Preview Card
        item {
            val completedMissions = dailyMissions.count { it.isCompleted }
            val totalMissions = dailyMissions.size.coerceAtLeast(1)

            Card(
                onClick = onOpenDailyMissions,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_missions_hub_card"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(NeonAmber.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎯", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Daily Operations", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = NeonAmber.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text("ROTATING", color = NeonAmber, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                                Text("$completedMissions/$totalMissions Completed · Claim Bonus XP & Coins", color = TextSecondary, fontSize = 11.sp)
                            }
                        }

                        Icon(Icons.Default.ChevronRight, contentDescription = "View Missions", tint = NeonAmber)
                    }

                    if (dailyMissions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val nextMission = dailyMissions.firstOrNull { !it.isClaimed } ?: dailyMissions.first()
                        Surface(
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(nextMission.iconEmoji, fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(nextMission.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Text(
                                        "Progress: ${nextMission.currentProgress}/${nextMission.targetProgress} · +${nextMission.rewardCoins}🪙 +${nextMission.rewardXp}XP",
                                        color = AccentGold,
                                        fontSize = 10.sp
                                    )
                                }
                                if (nextMission.isCompleted && !nextMission.isClaimed) {
                                    Text("CLAIM 🎁", color = NeonLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("VIEW >", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mini-Games Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Arcade Mini-Games", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("4 Games Active", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // 4 Featured Mini-Games Cards
        val games = listOf(
            MiniGameType.CYBER_PONG to Triple("🏓", "High-intensity curved paddle duel", NeonViolet),
            MiniGameType.SNAKE_ARENA to Triple("🐍", "Multiplayer slither & glow orb combat", NeonLime),
            MiniGameType.SPACE_BLASTER to Triple("🚀", "Top-down space fighter survival", NeonCyan),
            MiniGameType.REFLEX_DUEL to Triple("⚡", "Rapid reflex reaction tap clash", NeonMagenta)
        )

        items(games) { (gameType, info) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("game_card_${gameType.name}"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(info.third.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(info.first, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(gameType.displayName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(info.second, color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Play button
                        Button(
                            onClick = { onPlayGame(gameType) },
                            colors = ButtonDefaults.buttonColors(containerColor = info.third),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Tutorial button
                        OutlinedButton(
                            onClick = { onOpenTutorial(gameType) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                        ) {
                            Text("Tutorial", fontSize = 11.sp)
                        }

                        // Spectate button
                        IconButton(
                            onClick = { onSpectateGame(gameType) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(DarkSurfaceVariant, CircleShape)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = "Spectate", tint = AccentRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Daily Reward Calendar Dialog
    if (showDailyRewardDialog) {
        AlertDialog(
            onDismissRequest = { showDailyRewardDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎁 Daily Login Streak", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Log in every day to claim exclusive coins, gems, and rare skins!", color = TextSecondary, fontSize = 12.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val days = listOf("Day 1\n+100🪙", "Day 2\n+200🪙", "Day 3\n+10💎", "Day 4\n+300🪙", "Day 5\n+25💎")
                        days.forEachIndexed { idx, dayText ->
                            Surface(
                                color = if (idx == 0) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = if (idx == 0) androidx.compose.foundation.BorderStroke(1.dp, NeonCyan) else null,
                                modifier = Modifier.weight(1f).padding(2.dp)
                            ) {
                                Text(
                                    dayText,
                                    color = if (idx == 0) NeonCyan else TextSecondary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!dailyRewardClaimed) {
                            dailyRewardClaimed = true
                            onClaimDailyReward()
                        }
                        showDailyRewardDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text(if (dailyRewardClaimed) "Already Claimed" else "Claim Day 1 (+100🪙)", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDailyRewardDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun FriendsDashboardScreen(
    friends: List<Friend>,
    onAddFriend: (tag: String) -> Unit,
    onInviteFriend: (Friend) -> Unit,
    onSpectateFriend: (Friend) -> Unit,
    onReportPlayer: (String) -> Unit
) {
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var friendTagInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Friend Network (${friends.size})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Join friends' lobbies or spectate live matches", color = TextSecondary, fontSize = 12.sp)
                }

                Button(
                    onClick = { showAddFriendDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(friends) { friend ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(DarkSurfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(friend.avatar, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(friend.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(friend.tag, color = TextTertiary, fontSize = 11.sp)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(
                                        when (friend.status) {
                                            FriendStatus.ONLINE -> NeonLime
                                            FriendStatus.IN_MATCH -> AccentRed
                                            FriendStatus.IN_LOBBY -> NeonCyan
                                            FriendStatus.OFFLINE -> TextTertiary
                                        },
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "${friend.status.label}: ${friend.currentActivity}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Action buttons depending on status
                    if (friend.status == FriendStatus.IN_MATCH) {
                        Button(
                            onClick = { onSpectateFriend(friend) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Watch 👁️", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (friend.status == FriendStatus.ONLINE || friend.status == FriendStatus.IN_LOBBY) {
                        Button(
                            onClick = { onInviteFriend(friend) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Invite 🎮", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    IconButton(
                        onClick = { onReportPlayer(friend.name) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = TextTertiary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    if (showAddFriendDialog) {
        AlertDialog(
            onDismissRequest = { showAddFriendDialog = false },
            title = { Text("Add Rival by Gamer Tag", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your friend's unique gamer tag:", color = TextSecondary, fontSize = 12.sp)
                    OutlinedTextField(
                        value = friendTagInput,
                        onValueChange = { friendTagInput = it },
                        placeholder = { Text("e.g. #ARC-9842") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (friendTagInput.isNotBlank()) onAddFriend(friendTagInput)
                        showAddFriendDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Send Request", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFriendDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
fun AchievementsScreen(
    achievements: List<Achievement>,
    onClaimReward: (Achievement) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text("Personal Achievements", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Unlock milestone medals & claim coin rewards", color = TextSecondary, fontSize = 12.sp)
            }
        }

        items(achievements) { ach ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (ach.isUnlocked && !ach.isClaimed) NeonCyan else DarkCardBorder
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(if (ach.isUnlocked) AccentGold.copy(alpha = 0.2f) else DarkSurfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(ach.iconEmoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ach.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(ach.description, color = TextSecondary, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (ach.currentProgress.toFloat() / ach.maxProgress).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(5.dp),
                            color = if (ach.isUnlocked) NeonLime else NeonCyan,
                            trackColor = DarkCardBorder
                        )
                        Text(
                            "${ach.currentProgress}/${ach.maxProgress} · Reward: +${ach.rewardCoins}🪙 +${ach.rewardXp}XP",
                            color = TextTertiary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    if (ach.isClaimed) {
                        Text("Claimed ✓", color = NeonLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else if (ach.isUnlocked) {
                        Button(
                            onClick = { onClaimReward(ach) },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Claim!", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    } else {
                        Text("Locked", color = TextTertiary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ArcadeShopScreen(
    storeItems: List<StoreItem>,
    userCoins: Int,
    userGems: Int,
    onBuyItem: (StoreItem) -> Unit,
    onEquipItem: (StoreItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Arcade Customization Shop", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Equip neon skins, trails & gamer titles", color = TextSecondary, fontSize = 12.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🪙 $userCoins", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("💎 $userGems", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        items(storeItems) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (item.isEquipped) NeonLime else DarkCardBorder
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(DarkSurfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(item.iconEmoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = DarkSurfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(item.category.name, color = TextTertiary, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                            }
                        }
                        Text(item.description, color = TextSecondary, fontSize = 11.sp)
                        Text(
                            if (item.priceCoins > 0) "🪙 ${item.priceCoins}" else "💎 ${item.priceGems}",
                            color = if (item.priceCoins > 0) AccentGold else NeonCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    if (item.isEquipped) {
                        Surface(
                            color = NeonLime.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("EQUIPPED", color = NeonLime, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    } else if (item.isUnlocked) {
                        OutlinedButton(
                            onClick = { onEquipItem(item) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Equip", color = TextPrimary, fontSize = 11.sp)
                        }
                    } else {
                        Button(
                            onClick = { onBuyItem(item) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text("Unlock", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardScreen(
    userRank: Int = 14
) {
    var leaderboardPeriod by remember { mutableIntStateOf(0) } // 0: Daily, 1: Weekly, 2: All-Time

    val topPlayers = remember {
        listOf(
            Triple("ApexStrike", "Master I", 3890),
            Triple("NeonPhantom", "Diamond II", 3420),
            Triple("PixelOverlord", "Diamond I", 3110),
            Triple("VortexSurge", "Platinum III", 2980),
            Triple("CyberViper (You)", "Platinum II", 2840),
            Triple("QuantumGlitch", "Platinum I", 2650),
            Triple("RetroBlaze", "Gold III", 2410)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text("Global Live Leaderboard", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Real-time trophy rankings across all mini-games", color = TextSecondary, fontSize = 12.sp)
        }

        TabRow(
            selectedTabIndex = leaderboardPeriod,
            containerColor = DarkSurface,
            contentColor = NeonCyan
        ) {
            Tab(selected = leaderboardPeriod == 0, onClick = { leaderboardPeriod = 0 }, text = { Text("Daily") })
            Tab(selected = leaderboardPeriod == 1, onClick = { leaderboardPeriod = 1 }, text = { Text("Weekly") })
            Tab(selected = leaderboardPeriod == 2, onClick = { leaderboardPeriod = 2 }, text = { Text("All-Time") })
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(topPlayers.size) { index ->
                val (name, rank, trophies) = topPlayers[index]
                val isMe = name.contains("You")

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMe) DarkSurfaceVariant else DarkSurface
                    ),
                    border = if (isMe) androidx.compose.foundation.BorderStroke(1.dp, NeonCyan) else null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            when (index) {
                                0 -> "🥇"
                                1 -> "🥈"
                                2 -> "🥉"
                                else -> "#${index + 1}"
                            },
                            fontSize = if (index < 3) 20.sp else 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (index < 3) Color.Unspecified else TextSecondary,
                            modifier = Modifier.width(36.dp)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Text(name, color = if (isMe) NeonCyan else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(rank, color = TextSecondary, fontSize = 11.sp)
                        }

                        Text("🏆 $trophies", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
