package com.example.missions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.model.DailyMission
import com.example.model.MiniGameType
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyMissionsScreen(
    missions: List<DailyMission>,
    onClaimMission: (DailyMission) -> Unit,
    onRerollMission: (DailyMission) -> Unit,
    onPlayMissionGame: (MiniGameType) -> Unit,
    onClaimMegaChest: () -> Unit,
    isMegaChestClaimed: Boolean,
    onBack: () -> Unit
) {
    // Simulated countdown timer till reset
    var secondsRemaining by remember { mutableIntStateOf(58432) } // ~16h 13m 52s

    LaunchedEffect(Unit) {
        while (secondsRemaining > 0) {
            delay(1000)
            secondsRemaining--
        }
    }

    val hours = secondsRemaining / 3600
    val minutes = (secondsRemaining % 3600) / 60
    val seconds = secondsRemaining % 60
    val countdownFormatted = String.format("%02dh %02dm %02ds", hours, minutes, seconds)

    val completedCount = missions.count { it.isCompleted }
    val totalCount = missions.size
    val allCompleted = completedCount == totalCount && totalCount > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Daily Operations", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Rotating objectives for bonus XP & currency", color = TextSecondary, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))

                // Timer & Mega Chest Hero Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⏳", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Rotation Reset in: ", color = TextSecondary, fontSize = 12.sp)
                                Text(countdownFormatted, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Surface(
                                color = DarkSurfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "$completedCount/$totalCount Complete",
                                    color = if (allCompleted) NeonLime else AccentGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Daily Mega Chest Card
                        Surface(
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (allCompleted && !isMegaChestClaimed) AccentGold else DarkCardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(
                                            Brush.radialGradient(listOf(AccentGold.copy(alpha = 0.4f), Color.Transparent)),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("👑", fontSize = 26.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Daily Mega Bounty Chest", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Complete all 3 missions to unlock", color = TextSecondary, fontSize = 11.sp)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text("+500🪙", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("+25💎", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text("+400 XP", color = NeonMagenta, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (isMegaChestClaimed) {
                                    Text("Claimed ✓", color = NeonLime, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                } else if (allCompleted) {
                                    Button(
                                        onClick = onClaimMegaChest,
                                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Open!", color = DarkBackground, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                    }
                                } else {
                                    Surface(
                                        color = DarkCardBorder,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Locked 🔒", color = TextTertiary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { if (totalCount > 0) completedCount.toFloat() / totalCount else 0f },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = if (allCompleted) NeonLime else NeonCyan,
                            trackColor = DarkCardBorder
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Today's Tactical Objectives", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Auto-rotates daily", color = TextSecondary, fontSize = 11.sp)
                }
            }

            items(missions) { mission ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("mission_card_${mission.id}"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (mission.isCompleted && !mission.isClaimed) NeonCyan
                        else if (mission.isClaimed) DarkCardBorder
                        else NeonViolet.copy(alpha = 0.4f)
                    ),
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
                                        .size(42.dp)
                                        .background(DarkSurfaceVariant, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(mission.iconEmoji, fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(mission.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(
                                        mission.gameType?.displayName ?: "Arcade Community",
                                        color = NeonViolet,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Difficulty tag
                            Surface(
                                color = when (mission.difficulty) {
                                    "Easy" -> NeonLime.copy(alpha = 0.15f)
                                    "Hard" -> NeonMagenta.copy(alpha = 0.15f)
                                    else -> NeonAmber.copy(alpha = 0.15f)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    mission.difficulty,
                                    color = when (mission.difficulty) {
                                        "Easy" -> NeonLime
                                        "Hard" -> NeonMagenta
                                        else -> NeonAmber
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(mission.description, color = TextSecondary, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar & Counter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Progress: ${mission.currentProgress} / ${mission.targetProgress}",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("+${mission.rewardCoins}🪙", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                if (mission.rewardGems > 0) {
                                    Text("+${mission.rewardGems}💎", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("+${mission.rewardXp} XP", color = NeonMagenta, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { (mission.currentProgress.toFloat() / mission.targetProgress).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = if (mission.isCompleted) NeonLime else NeonCyan,
                            trackColor = DarkCardBorder
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Reroll button if not completed
                            if (!mission.isCompleted && !mission.isClaimed) {
                                TextButton(
                                    onClick = { onRerollMission(mission) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = TextTertiary)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reroll", fontSize = 11.sp)
                                }
                            } else {
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            if (mission.isClaimed) {
                                Surface(
                                    color = NeonLime.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = NeonLime, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Reward Claimed", color = NeonLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else if (mission.isCompleted) {
                                Button(
                                    onClick = { onClaimMission(mission) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Text("Claim Reward 🎁", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else if (mission.gameType != null) {
                                Button(
                                    onClick = { onPlayMissionGame(mission.gameType) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Play Mission", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
