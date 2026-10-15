package com.example.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt
import kotlin.random.Random

data class Meteor(val id: Int, var x: Float, var y: Float, var speed: Float, var size: Float, var hp: Int = 1)
data class Laser(val id: Int, var x: Float, var y: Float, val isEnemy: Boolean = false)

@Composable
fun SpaceBlasterGameScreen(
    isSpectator: Boolean = false,
    isTutorial: Boolean = false,
    player1Name: String = "CyberViper",
    player2Name: String = "NeonPhantom",
    onExitGame: () -> Unit,
    onMatchFinished: (winner: String, score1: Int, score2: Int) -> Unit = { _, _, _ -> },
    onTutorialComplete: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    var shipX by remember { mutableFloatStateOf(0.5f) }
    var shipY by remember { mutableFloatStateOf(0.85f) }
    var shipHp by remember { mutableIntStateOf(100) }
    var score by remember { mutableIntStateOf(0) }
    var enemyHp by remember { mutableIntStateOf(100) }

    var meteors by remember { mutableStateOf(listOf<Meteor>()) }
    var lasers by remember { mutableStateOf(listOf<Laser>()) }
    var isPaused by remember { mutableStateOf(false) }
    var gameOverWinner by remember { mutableStateOf<String?>(null) }

    // Spectator state
    var spectatorReactions by remember { mutableStateOf(listOf<String>()) }
    var spectatorCount by remember { mutableIntStateOf(32) }

    // Tutorial state
    var tutorialStepIndex by remember { mutableIntStateOf(0) }
    var meteorsDestroyedInTutorial by remember { mutableIntStateOf(0) }

    var laserCounter by remember { mutableIntStateOf(0) }
    var meteorCounter by remember { mutableIntStateOf(0) }

    // Game loop
    LaunchedEffect(isPaused, gameOverWinner) {
        var tick = 0
        while (!isPaused && gameOverWinner == null) {
            delay(20)
            tick++

            // Auto-fire laser every 10 ticks (200ms)
            if (tick % 10 == 0) {
                laserCounter++
                lasers = lasers + Laser(laserCounter, shipX, shipY - 0.05f)
                ArcadeSoundManager.playSpaceLaser()
            }

            // Spawn meteor every 35 ticks
            if (tick % 35 == 0 && meteors.size < 6) {
                meteorCounter++
                meteors = meteors + Meteor(
                    id = meteorCounter,
                    x = Random.nextFloat() * 0.8f + 0.1f,
                    y = -0.05f,
                    speed = if (isTutorial) 0.005f else 0.009f,
                    size = 0.05f,
                    hp = 1
                )
            }

            // Spectator mode AI movement
            if (isSpectator) {
                val nearest = meteors.minByOrNull { it.y }
                if (nearest != null) {
                    if (shipX < nearest.x) shipX = (shipX + 0.015f).coerceAtMost(0.9f)
                    else shipX = (shipX - 0.015f).coerceAtLeast(0.1f)
                }
            }

            // Update lasers
            val nextLasers = mutableListOf<Laser>()
            lasers.forEach { laser ->
                laser.y -= 0.035f
                if (laser.y > -0.1f) nextLasers.add(laser)
            }

            // Update meteors and collision with lasers
            val nextMeteors = mutableListOf<Meteor>()
            meteors.forEach { meteor ->
                meteor.y += meteor.speed

                // Check collision with player lasers
                var hit = false
                val laserIterator = nextLasers.iterator()
                while (laserIterator.hasNext()) {
                    val l = laserIterator.next()
                    val dx = l.x - meteor.x
                    val dy = l.y - meteor.y
                    if (sqrt((dx * dx + dy * dy).toDouble()) < 0.06) {
                        laserIterator.remove()
                        hit = true
                        break
                    }
                }

                if (hit) {
                    score += 50
                    ArcadeSoundManager.playMeteorExplosion()
                    if (isTutorial) {
                        meteorsDestroyedInTutorial++
                        if (meteorsDestroyedInTutorial >= 3) {
                            gameOverWinner = "Tutorial Complete!"
                            ArcadeSoundManager.playVictoryFanfare()
                            onTutorialComplete()
                        }
                    } else if (score >= 400) {
                        gameOverWinner = player1Name
                        ArcadeSoundManager.playVictoryFanfare()
                        onMatchFinished(player1Name, score, 200)
                    }
                } else if (meteor.y > 0.8f && meteor.y < 0.95f && kotlin.math.abs(meteor.x - shipX) < 0.08f) {
                    // Ship collision
                    shipHp -= 25
                    ArcadeSoundManager.playShipDamaged()
                    if (shipHp <= 0 && !isTutorial) {
                        gameOverWinner = player2Name
                        onMatchFinished(player2Name, score, 500)
                    }
                } else if (meteor.y < 1.1f) {
                    nextMeteors.add(meteor)
                }
            }

            lasers = nextLasers
            meteors = nextMeteors
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("space_blaster_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 12.dp)
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
                                Text("SPECTATING DOGFIGHT", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                "TUTORIAL: COMBAT FLIGHT",
                                color = NeonAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Text("SPACE BLASTER SURVIVORS", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Shield: $shipHp%", color = if (shipHp > 40) NeonLime else AccentRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Score: $score", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
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
                    IconButton(onClick = { isPaused = !isPaused }) {
                        Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = "Pause", tint = TextPrimary)
                    }
                }
            }

            // Tutorial Banner
            if (isTutorial) {
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
                            modifier = Modifier.size(28.dp).background(NeonCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("1", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Destroy Incoming Meteors", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Drag your spaceship horizontally to aim your plasma lasers at 3 asteroids ($meteorsDestroyedInTutorial/3 destroyed)", color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Starfield Space Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .background(Color(0xFF070514), RoundedCornerShape(16.dp))
                    .pointerInput(isSpectator) {
                        if (!isSpectator) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                shipX = (shipX + dragAmount.x / size.width).coerceIn(0.08f, 0.92f)
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Star dust dots
                    val starPositions = listOf(
                        Offset(w * 0.2f, h * 0.1f), Offset(w * 0.8f, h * 0.25f),
                        Offset(w * 0.45f, h * 0.4f), Offset(w * 0.15f, h * 0.7f),
                        Offset(w * 0.85f, h * 0.85f), Offset(w * 0.65f, h * 0.55f)
                    )
                    starPositions.forEach { star ->
                        drawCircle(color = Color.White.copy(alpha = 0.4f), radius = 2f, center = star)
                    }

                    // Draw Lasers
                    lasers.forEach { laser ->
                        val lx = laser.x * w
                        val ly = laser.y * h
                        drawLine(
                            color = NeonCyan,
                            start = Offset(lx, ly),
                            end = Offset(lx, ly + 25f),
                            strokeWidth = 4f
                        )
                    }

                    // Draw Meteors
                    meteors.forEach { meteor ->
                        val mx = meteor.x * w
                        val my = meteor.y * h
                        val mr = meteor.size * w
                        drawCircle(
                            color = AccentRed.copy(alpha = 0.25f),
                            radius = mr * 1.3f,
                            center = Offset(mx, my)
                        )
                        drawCircle(
                            brush = Brush.radialGradient(listOf(NeonAmber, AccentRed)),
                            radius = mr,
                            center = Offset(mx, my)
                        )
                    }

                    // Draw Player Ship (Stylized Neon Fighter Jet)
                    val sx = shipX * w
                    val sy = shipY * h
                    val shipPath = Path().apply {
                        moveTo(sx, sy - 30f) // tip
                        lineTo(sx - 24f, sy + 25f) // left wing
                        lineTo(sx, sy + 15f) // engine core
                        lineTo(sx + 24f, sy + 25f) // right wing
                        close()
                    }
                    drawPath(
                        path = shipPath,
                        brush = Brush.verticalGradient(listOf(NeonCyan, NeonViolet))
                    )
                    // Engine plume glow
                    drawCircle(
                        color = NeonCyan.copy(alpha = 0.7f),
                        radius = 8f,
                        center = Offset(sx, sy + 18f)
                    )
                }

                if (!isSpectator) {
                    Text(
                        "Drag horizontally to steer & aim auto-lasers",
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
                        Text("Live Reactions:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        val emojis = listOf("🚀", "💥", "⚡", "🔥", "🛡️")
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("🏆", fontSize = 48.sp)
                        Text(
                            if (isTutorial) "Space Combat Mastered!" else "$gameOverWinner Wins!",
                            color = NeonCyan,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isTutorial) "Excellent marksmanship! You earned +150 XP and +200 Coins!" else "Final Mission Score: $score",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onExitGame,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Continue", color = DarkBackground, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    shipHp = 100
                                    score = 0
                                    lasers = emptyList()
                                    meteors = emptyList()
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
