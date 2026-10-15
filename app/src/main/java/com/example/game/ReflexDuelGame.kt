package com.example.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.audio.ArcadeSoundManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun ReflexDuelGameScreen(
    isSpectator: Boolean = false,
    isTutorial: Boolean = false,
    player1Name: String = "CyberViper",
    player2Name: String = "NeonPhantom",
    onExitGame: () -> Unit,
    onMatchFinished: (winner: String, score1: Int, score2: Int) -> Unit = { _, _, _ -> },
    onTutorialComplete: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    var activeTargetIndex by remember { mutableIntStateOf(Random.nextInt(4)) }
    var score1 by remember { mutableIntStateOf(0) }
    var score2 by remember { mutableIntStateOf(0) }
    var streak1 by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(if (isTutorial) 45 else 30) }
    var isPaused by remember { mutableStateOf(false) }
    var gameOverWinner by remember { mutableStateOf<String?>(null) }

    // Spectator state
    var spectatorReactions by remember { mutableStateOf(listOf<String>()) }
    var spectatorCount by remember { mutableIntStateOf(29) }

    // Tutorial state
    var tutorialStepIndex by remember { mutableIntStateOf(0) }
    var tutorialHits by remember { mutableIntStateOf(0) }

    // Timer and Bot loop
    LaunchedEffect(isPaused, gameOverWinner) {
        while (!isPaused && gameOverWinner == null && timeLeft > 0) {
            delay(1000)
            timeLeft--

            // Bot scores occasionally
            if (Random.nextFloat() < 0.65f) {
                score2 += 10
            }

            // Spectator mode simulation for Player 1
            if (isSpectator && Random.nextFloat() < 0.70f) {
                score1 += 10
                activeTargetIndex = Random.nextInt(4)
            }
        }
        if (timeLeft <= 0 && gameOverWinner == null) {
            if (isTutorial) {
                gameOverWinner = "Tutorial Complete!"
                onTutorialComplete()
            } else {
                val winner = if (score1 >= score2) player1Name else player2Name
                gameOverWinner = winner
                onMatchFinished(winner, score1, score2)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("reflex_duel_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onExitGame) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Exit", tint = TextPrimary)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (isSpectator) {
                        Surface(
                            color = AccentRed.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(8.dp).background(AccentRed, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SPECTATING BLITZ", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("👁️ $spectatorCount", color = TextPrimary, fontSize = 11.sp)
                            }
                        }
                    } else if (isTutorial) {
                        Surface(
                            color = NeonAmber.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonAmber)
                        ) {
                            Text(
                                "TUTORIAL: REACTION BLITZ",
                                color = NeonAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Text("TAP BLITZ REACTION DUEL", color = NeonMagenta, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("$player1Name: $score1", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("⏳ ${timeLeft}s", color = AccentGold, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                        Text("$player2Name: $score2", color = NeonMagenta, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(onClick = { isPaused = !isPaused }) {
                    Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = "Pause", tint = TextPrimary)
                }
            }

            // Tutorial Banner
            if (isTutorial) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonMagenta.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(28.dp).background(NeonMagenta, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Fast Reaction Drills", color = NeonMagenta, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Tap the highlighted glowing pad immediately when it shines! ($tutorialHits/3 correct hits)", color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }

            // 4-Quadrant Tap Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                val padColors = listOf(NeonCyan, NeonMagenta, NeonLime, NeonAmber)

                Column(
                    modifier = Modifier.fillMaxWidth(0.9f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (i in 0..1) {
                            val isActive = activeTargetIndex == i
                            val padColor = padColors[i]

                            Card(
                                onClick = {
                                    if (!isSpectator) {
                                        if (isActive) {
                                            score1 += (10 + streak1 * 2)
                                            streak1++
                                            activeTargetIndex = Random.nextInt(4)
                                            if (isTutorial) {
                                                tutorialHits++
                                                if (tutorialHits >= 3) {
                                                    gameOverWinner = "Tutorial Complete!"
                                                    onTutorialComplete()
                                                }
                                            }
                                        } else {
                                            streak1 = 0
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(130.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isActive) padColor else DarkSurfaceVariant
                                ),
                                border = if (isActive) androidx.compose.foundation.BorderStroke(3.dp, Color.White) else null
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isActive) {
                                        Text("TAP NOW!", color = DarkBackground, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    } else {
                                        Text("WAIT", color = TextTertiary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        for (i in 2..3) {
                            val isActive = activeTargetIndex == i
                            val padColor = padColors[i]

                            Card(
                                onClick = {
                                    if (!isSpectator) {
                                        if (isActive) {
                                            score1 += (10 + streak1 * 2)
                                            streak1++
                                            activeTargetIndex = Random.nextInt(4)
                                            if (isTutorial) {
                                                tutorialHits++
                                                if (tutorialHits >= 3) {
                                                    gameOverWinner = "Tutorial Complete!"
                                                    onTutorialComplete()
                                                }
                                            }
                                        } else {
                                            streak1 = 0
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(130.dp),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isActive) padColor else DarkSurfaceVariant
                                ),
                                border = if (isActive) androidx.compose.foundation.BorderStroke(3.dp, Color.White) else null
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isActive) {
                                        Text("TAP NOW!", color = DarkBackground, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                                    } else {
                                        Text("WAIT", color = TextTertiary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Streak indicator
            if (!isSpectator) {
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        "🔥 Combo Streak: $streak1 x",
                        color = AccentGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            } else {
                // Spectator Live Reaction Bar
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Live Reactions:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        val emojis = listOf("⚡", "🔥", "💨", "🤯", "👏")
                        emojis.forEach { emoji ->
                            IconButton(
                                onClick = {
                                    spectatorReactions = spectatorReactions + emoji
                                    coroutineScope.launch {
                                        delay(2000)
                                        spectatorReactions = spectatorReactions.drop(1)
                                    }
                                },
                                modifier = Modifier.size(38.dp)
                            ) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }
        }

        // Animated floating reactions overlay
        spectatorReactions.forEachIndexed { index, emoji ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = (30 + (index * 25)).dp, bottom = (110 + (index * 35)).dp)
            ) {
                Text(emoji, fontSize = 32.sp)
            }
        }

        // Game Over / Modal
        if (gameOverWinner != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.75f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonMagenta)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("🏆", fontSize = 48.sp)
                        Text(
                            if (isTutorial) "Tap Blitz Mastered!" else "$gameOverWinner Wins!",
                            color = NeonMagenta,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isTutorial) "Super reflex speed! You earned +150 XP and +200 Coins!" else "Final Scores: $score1 - $score2",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onExitGame,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Continue", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    score1 = 0
                                    score2 = 0
                                    streak1 = 0
                                    timeLeft = if (isTutorial) 45 else 30
                                    gameOverWinner = null
                                    isPaused = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Retry", color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
