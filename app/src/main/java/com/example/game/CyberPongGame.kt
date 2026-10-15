package com.example.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.audio.ArcadeSoundManager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TutorialStep
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun CyberPongGameScreen(
    isSpectator: Boolean = false,
    isTutorial: Boolean = false,
    player1Name: String = "CyberViper",
    player2Name: String = "NeonPhantom",
    onExitGame: () -> Unit,
    onMatchFinished: (winner: String, score1: Int, score2: Int) -> Unit = { _, _, _ -> },
    onTutorialComplete: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    // Game state
    var player1Y by remember { mutableFloatStateOf(0.5f) } // normalized 0f..1f
    var player2Y by remember { mutableFloatStateOf(0.5f) }
    var ballX by remember { mutableFloatStateOf(0.5f) }
    var ballY by remember { mutableFloatStateOf(0.5f) }
    var ballVx by remember { mutableFloatStateOf(0.012f) }
    var ballVy by remember { mutableFloatStateOf(0.008f) }
    var score1 by remember { mutableIntStateOf(0) }
    var score2 by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var gameOverWinner by remember { mutableStateOf<String?>(null) }
    var hitFlash by remember { mutableStateOf(false) }
    var powerUpActive by remember { mutableStateOf(false) }
    var powerUpX by remember { mutableFloatStateOf(0.5f) }
    var powerUpY by remember { mutableFloatStateOf(0.3f) }

    // Spectator state
    var spectatorReactions by remember { mutableStateOf(listOf<String>()) }
    var spectatorCount by remember { mutableIntStateOf(18) }

    // Tutorial state
    var tutorialStepIndex by remember { mutableIntStateOf(0) }
    val tutorialSteps = remember {
        listOf(
            TutorialStep(1, "Paddle Control", "Touch and drag anywhere on the left side to move your paddle up and down.", "Move paddle up and down"),
            TutorialStep(2, "Deflect the Energy Orb", "Position your paddle in front of the incoming ball to deflect it back.", "Deflect incoming ball"),
            TutorialStep(3, "Score a Goal", "Score 1 point past the training dummy to master Cyber Pong!", "Score 1 goal")
        )
    }
    var tutorialCompletedSteps by remember { mutableStateOf(setOf<Int>()) }
    var hasMovedPaddleInTutorial by remember { mutableStateOf(false) }
    var hasDeflectedBallInTutorial by remember { mutableStateOf(false) }

    // Main 60fps simulation loop
    LaunchedEffect(isPaused, gameOverWinner, tutorialStepIndex) {
        val paddleHeight = 0.22f
        val paddleWidth = 0.035f

        while (!isPaused && gameOverWinner == null) {
            delay(16) // ~60fps

            // Ball simulation speed (slower in tutorial)
            val speedFactor = if (isTutorial && tutorialStepIndex < 2) 0.5f else 1.0f

            // Move ball
            ballX += ballVx * speedFactor
            ballY += ballVy * speedFactor

            // Top and bottom wall collisions
            if (ballY <= 0.05f) {
                ballY = 0.05f
                ballVy = abs(ballVy)
                ArcadeSoundManager.playPongWallHit()
            } else if (ballY >= 0.95f) {
                ballY = 0.95f
                ballVy = -abs(ballVy)
                ArcadeSoundManager.playPongWallHit()
            }

            // AI movement for Player 2
            val targetY2 = ballY + (if (isTutorial) 0.05f else 0.02f)
            val aiSpeed = if (isTutorial) 0.007f else 0.011f
            if (player2Y < targetY2 - 0.02f) {
                player2Y = (player2Y + aiSpeed).coerceAtMost(0.9f)
            } else if (player2Y > targetY2 + 0.02f) {
                player2Y = (player2Y - aiSpeed).coerceAtLeast(0.1f)
            }

            // Spectator mode AI for Player 1
            if (isSpectator) {
                val targetY1 = ballY
                val p1Speed = 0.012f
                if (player1Y < targetY1 - 0.02f) {
                    player1Y = (player1Y + p1Speed).coerceAtMost(0.9f)
                } else if (player1Y > targetY1 + 0.02f) {
                    player1Y = (player1Y - p1Speed).coerceAtLeast(0.1f)
                }
            }

            // Collision with Player 1 (Left paddle, X ~ 0.06f)
            val p1Left = 0.05f
            val p1Right = p1Left + paddleWidth
            val p1Top = player1Y - paddleHeight / 2
            val p1Bottom = player1Y + paddleHeight / 2

            if (ballX in p1Left..p1Right && ballY in p1Top..p1Bottom && ballVx < 0) {
                ballVx = abs(ballVx) * 1.04f
                val deltaY = (ballY - player1Y) / (paddleHeight / 2)
                ballVy = deltaY * 0.012f
                hitFlash = true
                ArcadeSoundManager.playPongPaddleHit()

                if (isTutorial && tutorialStepIndex == 1) {
                    hasDeflectedBallInTutorial = true
                    tutorialCompletedSteps = tutorialCompletedSteps + 1
                    tutorialStepIndex = 2
                }
            }

            // Collision with Player 2 (Right paddle, X ~ 0.94f)
            val p2Right = 0.95f
            val p2Left = p2Right - paddleWidth
            val p2Top = player2Y - paddleHeight / 2
            val p2Bottom = player2Y + paddleHeight / 2

            if (ballX in p2Left..p2Right && ballY in p2Top..p2Bottom && ballVx > 0) {
                ballVx = -abs(ballVx) * 1.04f
                val deltaY = (ballY - player2Y) / (paddleHeight / 2)
                ballVy = deltaY * 0.012f
                hitFlash = true
                ArcadeSoundManager.playPongPaddleHit()
            }

            // Check goal Left
            if (ballX < 0.01f) {
                score2++
                ArcadeSoundManager.playPongGoalScored()
                ballX = 0.5f
                ballY = 0.5f
                ballVx = 0.012f
                ballVy = (Random.nextFloat() - 0.5f) * 0.012f
                if (score2 >= (if (isTutorial) 2 else 5)) {
                    gameOverWinner = player2Name
                    ArcadeSoundManager.playVictoryFanfare()
                    onMatchFinished(player2Name, score1, score2)
                }
            }

            // Check goal Right
            if (ballX > 0.99f) {
                score1++
                ArcadeSoundManager.playPongGoalScored()
                ballX = 0.5f
                ballY = 0.5f
                ballVx = -0.012f
                ballVy = (Random.nextFloat() - 0.5f) * 0.012f

                if (isTutorial) {
                    tutorialCompletedSteps = tutorialCompletedSteps + 2
                    gameOverWinner = "Tutorial Complete!"
                    ArcadeSoundManager.playVictoryFanfare()
                    onTutorialComplete()
                } else if (score1 >= 5) {
                    gameOverWinner = player1Name
                    ArcadeSoundManager.playVictoryFanfare()
                    onMatchFinished(player1Name, score1, score2)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("cyber_pong_container")
    ) {
        // Upper status cluster
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onExitGame,
                    modifier = Modifier.testTag("exit_game_button")
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Exit Game", tint = TextPrimary)
                }

                // Center Match Score / Status
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
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(AccentRed, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("LIVE SPECTATOR", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                "INTERACTIVE TUTORIAL",
                                color = NeonAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Text("CYBER PONG CLASH", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    // Scoreboard
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(player1Name, color = NeonViolet, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("$score1 - $score2", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        Text(player2Name, color = NeonMagenta, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    var soundEnabled by remember { mutableStateOf(!ArcadeSoundManager.isMuted) }
                    IconButton(
                        onClick = {
                            soundEnabled = !soundEnabled
                            ArcadeSoundManager.isMuted = !soundEnabled
                        }
                    ) {
                        Icon(
                            if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Sound Toggle",
                            tint = if (soundEnabled) NeonCyan else TextTertiary
                        )
                    }
                    IconButton(
                        onClick = { isPaused = !isPaused },
                        modifier = Modifier.testTag("pause_button")
                    ) {
                        Icon(
                            if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = TextPrimary
                        )
                    }
                }
            }

            // Tutorial Banner Overlay
            if (isTutorial && tutorialStepIndex < tutorialSteps.size) {
                val step = tutorialSteps[tutorialStepIndex]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(NeonCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${step.stepNumber}", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(step.title, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(step.instruction, color = TextPrimary, fontSize = 12.sp)
                        }
                        if (tutorialStepIndex == 0) {
                            Button(
                                onClick = {
                                    tutorialCompletedSteps = tutorialCompletedSteps + 0
                                    tutorialStepIndex = 1
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Got it!", color = DarkBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Playfield Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .background(DarkSurface, RoundedCornerShape(16.dp))
                    .pointerInput(isSpectator) {
                        if (!isSpectator) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                player1Y = (player1Y + dragAmount.y / size.height).coerceIn(0.12f, 0.88f)
                                if (isTutorial && tutorialStepIndex == 0) {
                                    hasMovedPaddleInTutorial = true
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val paddleH = h * 0.22f
                    val paddleW = w * 0.035f

                    // Center line
                    drawLine(
                        color = DarkCardBorder,
                        start = Offset(w / 2, 0f),
                        end = Offset(w / 2, h),
                        strokeWidth = 2f
                    )

                    // Center circle
                    drawCircle(
                        color = DarkCardBorder.copy(alpha = 0.5f),
                        radius = w * 0.14f,
                        center = Offset(w / 2, h / 2),
                        style = Stroke(width = 2f)
                    )

                    // Player 1 Paddle (Violet / Cyan gradient)
                    val p1Top = (player1Y * h) - paddleH / 2
                    drawRoundRect(
                        brush = Brush.verticalGradient(listOf(NeonCyan, NeonViolet)),
                        topLeft = Offset(w * 0.05f, p1Top),
                        size = Size(paddleW, paddleH),
                        cornerRadius = CornerRadius(12f, 12f)
                    )

                    // Player 2 Paddle (Magenta / Violet gradient)
                    val p2Top = (player2Y * h) - paddleH / 2
                    drawRoundRect(
                        brush = Brush.verticalGradient(listOf(NeonMagenta, NeonViolet)),
                        topLeft = Offset(w * 0.95f - paddleW, p2Top),
                        size = Size(paddleW, paddleH),
                        cornerRadius = CornerRadius(12f, 12f)
                    )

                    // Glowing Energy Ball
                    val ballPos = Offset(ballX * w, ballY * h)
                    drawCircle(
                        color = NeonCyan.copy(alpha = 0.35f),
                        radius = 22f,
                        center = ballPos
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 12f,
                        center = ballPos
                    )
                }

                // Control hint for active players
                if (!isSpectator && !isTutorial) {
                    Text(
                        "Drag anywhere to move paddle",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }
            }

            // Spectator Live Reaction Bar
            if (isSpectator) {
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
                        Text("Cheer Live:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        val emojis = listOf("🔥", "👏", "⚡", "😱", "🏆", "❤️")
                        emojis.forEach { emoji ->
                            IconButton(
                                onClick = {
                                    spectatorReactions = spectatorReactions + emoji
                                    ArcadeSoundManager.playCheerSound()
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
                    .padding(end = (30 + (index * 25)).dp, bottom = (90 + (index * 35)).dp)
            ) {
                Text(emoji, fontSize = 32.sp)
            }
        }

        // Game Over / Victory Modal
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("🏆", fontSize = 48.sp)
                        Text(
                            if (isTutorial) "Tutorial Mastered!" else "$gameOverWinner Wins!",
                            color = NeonCyan,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isTutorial) "You've earned +150 XP and +200 Coins for mastering Cyber Pong!" else "Final Match Score: $score1 - $score2",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onExitGame,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Continue", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    score1 = 0
                                    score2 = 0
                                    ballX = 0.5f
                                    ballY = 0.5f
                                    gameOverWinner = null
                                    isPaused = false
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Rematch", color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}
