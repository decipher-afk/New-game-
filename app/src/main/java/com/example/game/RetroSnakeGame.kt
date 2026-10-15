package com.example.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TutorialStep
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class GridPoint(val x: Int, val y: Int)
enum class Direction { UP, DOWN, LEFT, RIGHT }

@Composable
fun RetroSnakeGameScreen(
    isSpectator: Boolean = false,
    isTutorial: Boolean = false,
    player1Name: String = "CyberViper",
    player2Name: String = "NeonPhantom",
    onExitGame: () -> Unit,
    onMatchFinished: (winner: String, score1: Int, score2: Int) -> Unit = { _, _, _ -> },
    onTutorialComplete: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val gridSize = 24

    // Game states
    var snake1 by remember {
        mutableStateOf(listOf(GridPoint(5, 12), GridPoint(4, 12), GridPoint(3, 12)))
    }
    var dir1 by remember { mutableStateOf(Direction.RIGHT) }

    var snake2 by remember {
        mutableStateOf(listOf(GridPoint(18, 12), GridPoint(19, 12), GridPoint(20, 12)))
    }
    var dir2 by remember { mutableStateOf(Direction.LEFT) }

    var foods by remember {
        mutableStateOf(listOf(GridPoint(12, 12), GridPoint(8, 6), GridPoint(16, 18)))
    }

    var score1 by remember { mutableIntStateOf(0) }
    var score2 by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var gameOverWinner by remember { mutableStateOf<String?>(null) }
    var isBoosting by remember { mutableStateOf(false) }

    // Spectator state
    var spectatorReactions by remember { mutableStateOf(listOf<String>()) }
    var spectatorCount by remember { mutableIntStateOf(24) }

    // Tutorial state
    var tutorialStepIndex by remember { mutableIntStateOf(0) }
    var foodEatenInTutorial by remember { mutableIntStateOf(0) }

    // Simulation loop
    LaunchedEffect(isPaused, gameOverWinner, dir1, dir2, isBoosting) {
        while (!isPaused && gameOverWinner == null) {
            val stepDelay = if (isBoosting) 110L else 180L
            delay(stepDelay)

            // Calculate next head for Snake 1
            val head1 = snake1.first()
            val nextHead1 = when (dir1) {
                Direction.UP -> GridPoint(head1.x, (head1.y - 1 + gridSize) % gridSize)
                Direction.DOWN -> GridPoint(head1.x, (head1.y + 1) % gridSize)
                Direction.LEFT -> GridPoint((head1.x - 1 + gridSize) % gridSize, head1.y)
                Direction.RIGHT -> GridPoint((head1.x + 1) % gridSize, head1.y)
            }

            // AI for Snake 2 (find nearest food or wander safely)
            val head2 = snake2.first()
            val targetFood = foods.minByOrNull {
                kotlin.math.abs(it.x - head2.x) + kotlin.math.abs(it.y - head2.y)
            } ?: GridPoint(12, 12)

            val possibleDirs = mutableListOf<Direction>()
            if (targetFood.x > head2.x && dir2 != Direction.LEFT) possibleDirs.add(Direction.RIGHT)
            if (targetFood.x < head2.x && dir2 != Direction.RIGHT) possibleDirs.add(Direction.LEFT)
            if (targetFood.y > head2.y && dir2 != Direction.UP) possibleDirs.add(Direction.DOWN)
            if (targetFood.y < head2.y && dir2 != Direction.DOWN) possibleDirs.add(Direction.UP)

            if (possibleDirs.isNotEmpty()) {
                dir2 = possibleDirs.first()
            }

            // Spectator mode AI for Snake 1
            if (isSpectator) {
                val targetFood1 = foods.minByOrNull {
                    kotlin.math.abs(it.x - head1.x) + kotlin.math.abs(it.y - head1.y)
                } ?: GridPoint(12, 12)
                if (targetFood1.x > head1.x && dir1 != Direction.LEFT) dir1 = Direction.RIGHT
                else if (targetFood1.x < head1.x && dir1 != Direction.RIGHT) dir1 = Direction.LEFT
                else if (targetFood1.y > head1.y && dir1 != Direction.UP) dir1 = Direction.DOWN
                else if (targetFood1.y < head1.y && dir1 != Direction.DOWN) dir1 = Direction.UP
            }

            val nextHead2 = when (dir2) {
                Direction.UP -> GridPoint(head2.x, (head2.y - 1 + gridSize) % gridSize)
                Direction.DOWN -> GridPoint(head2.x, (head2.y + 1) % gridSize)
                Direction.LEFT -> GridPoint((head2.x - 1 + gridSize) % gridSize, head2.y)
                Direction.RIGHT -> GridPoint((head2.x + 1) % gridSize, head2.y)
            }

            // Snake 1 collision with self or Snake 2
            if (snake1.contains(nextHead1) || snake2.contains(nextHead1)) {
                if (!isTutorial) {
                    gameOverWinner = player2Name
                    onMatchFinished(player2Name, score1, score2)
                    break
                }
            }

            // Snake 2 collision
            if (snake2.contains(nextHead2) || snake1.contains(nextHead2)) {
                if (!isTutorial) {
                    gameOverWinner = player1Name
                    onMatchFinished(player1Name, score1, score2)
                    break
                }
            }

            // Check food for Snake 1
            val ateFood1 = foods.contains(nextHead1)
            val newSnake1 = mutableListOf(nextHead1)
            newSnake1.addAll(if (ateFood1) snake1 else snake1.dropLast(1))
            snake1 = newSnake1

            if (ateFood1) {
                score1 += 10
                foods = foods.filter { it != nextHead1 } + GridPoint(Random.nextInt(gridSize), Random.nextInt(gridSize))
                if (isTutorial) {
                    foodEatenInTutorial++
                    if (foodEatenInTutorial >= 2) {
                        gameOverWinner = "Tutorial Complete!"
                        onTutorialComplete()
                        break
                    }
                }
            }

            // Check food for Snake 2
            val ateFood2 = foods.contains(nextHead2)
            val newSnake2 = mutableListOf(nextHead2)
            newSnake2.addAll(if (ateFood2) snake2 else snake2.dropLast(1))
            snake2 = newSnake2

            if (ateFood2) {
                score2 += 10
                foods = foods.filter { it != nextHead2 } + GridPoint(Random.nextInt(gridSize), Random.nextInt(gridSize))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("retro_snake_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 12.dp)
        ) {
            // Header Bar
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
                                Text("SPECTATING ARENA", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                "TUTORIAL: SLITHER PRACTICE",
                                color = NeonAmber,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Text("RETRO SNAKE ARENA", color = NeonLime, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("$player1Name: $score1", color = NeonLime, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("VS", color = TextTertiary, fontSize = 11.sp)
                        Text("$player2Name: $score2", color = NeonMagenta, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonLime.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(28.dp).background(NeonLime, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("1", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Collect Glow Orbs", color = NeonLime, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Use the directional buttons below to guide your snake into 2 energy pellets ($foodEatenInTutorial/2 collected)", color = TextPrimary, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Grid Canvas Arena
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .background(DarkSurface, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellSize = size.width.coerceAtMost(size.height) / gridSize
                    val startX = (size.width - cellSize * gridSize) / 2
                    val startY = (size.height - cellSize * gridSize) / 2

                    // Arena boundary grid
                    for (x in 0..gridSize) {
                        drawLine(
                            color = DarkCardBorder.copy(alpha = 0.15f),
                            start = Offset(startX + x * cellSize, startY),
                            end = Offset(startX + x * cellSize, startY + gridSize * cellSize)
                        )
                    }
                    for (y in 0..gridSize) {
                        drawLine(
                            color = DarkCardBorder.copy(alpha = 0.15f),
                            start = Offset(startX, startY + y * cellSize),
                            end = Offset(startX + gridSize * cellSize, startY + y * cellSize)
                        )
                    }

                    // Draw Food pellets (Glowing Orbs)
                    foods.forEach { food ->
                        val foodCenter = Offset(startX + (food.x + 0.5f) * cellSize, startY + (food.y + 0.5f) * cellSize)
                        drawCircle(color = AccentGold.copy(alpha = 0.3f), radius = cellSize * 0.7f, center = foodCenter)
                        drawCircle(color = AccentGold, radius = cellSize * 0.4f, center = foodCenter)
                    }

                    // Draw Snake 1 (Player - Neon Lime)
                    snake1.forEachIndexed { index, seg ->
                        val color = if (index == 0) Color.White else NeonLime
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(startX + seg.x * cellSize + 2f, startY + seg.y * cellSize + 2f),
                            size = Size(cellSize - 4f, cellSize - 4f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )
                    }

                    // Draw Snake 2 (Opponent - Neon Magenta)
                    snake2.forEachIndexed { index, seg ->
                        val color = if (index == 0) Color.White else NeonMagenta
                        drawRoundRect(
                            color = color,
                            topLeft = Offset(startX + seg.x * cellSize + 2f, startY + seg.y * cellSize + 2f),
                            size = Size(cellSize - 4f, cellSize - 4f),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                        )
                    }
                }
            }

            // Controls (Only visible for player)
            if (!isSpectator) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // D-Pad
                    IconButton(
                        onClick = { if (dir1 != Direction.DOWN) dir1 = Direction.UP },
                        modifier = Modifier
                            .size(48.dp)
                            .background(DarkSurfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = NeonLime)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(40.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (dir1 != Direction.RIGHT) dir1 = Direction.LEFT },
                            modifier = Modifier
                                .size(48.dp)
                                .background(DarkSurfaceVariant, CircleShape)
                        ) {
                            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = NeonLime)
                        }

                        // Boost Button
                        Button(
                            onClick = { isBoosting = !isBoosting },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isBoosting) NeonAmber else DarkSurfaceVariant
                            ),
                            shape = CircleShape,
                            modifier = Modifier.size(54.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(if (isBoosting) "⚡⚡" else "⚡", fontSize = 18.sp)
                        }

                        IconButton(
                            onClick = { if (dir1 != Direction.LEFT) dir1 = Direction.RIGHT },
                            modifier = Modifier
                                .size(48.dp)
                                .background(DarkSurfaceVariant, CircleShape)
                        ) {
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = NeonLime)
                        }
                    }
                    IconButton(
                        onClick = { if (dir1 != Direction.UP) dir1 = Direction.DOWN },
                        modifier = Modifier
                            .size(48.dp)
                            .background(DarkSurfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = NeonLime)
                    }
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
                        val emojis = listOf("🐍", "🔥", "⚡", "😱", "👏")
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonLime)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text("🏆", fontSize = 48.sp)
                        Text(
                            if (isTutorial) "Snake Arena Mastered!" else "$gameOverWinner Wins!",
                            color = NeonLime,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (isTutorial) "Great slithering! You've completed the tutorial and earned +150 XP!" else "Scores: $player1Name ($score1) - $player2Name ($score2)",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(
                                onClick = onExitGame,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonLime),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Continue", color = DarkBackground, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    snake1 = listOf(GridPoint(5, 12), GridPoint(4, 12), GridPoint(3, 12))
                                    snake2 = listOf(GridPoint(18, 12), GridPoint(19, 12), GridPoint(20, 12))
                                    score1 = 0
                                    score2 = 0
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
