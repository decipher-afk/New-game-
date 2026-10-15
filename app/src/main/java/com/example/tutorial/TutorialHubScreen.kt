package com.example.tutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MiniGameType
import com.example.ui.theme.*

data class TutorialCardInfo(
    val gameType: MiniGameType,
    val title: String,
    val description: String,
    val objectives: List<String>,
    val iconEmoji: String,
    val rewardXp: Int = 150,
    val rewardCoins: Int = 200,
    val isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorialHubScreen(
    completedTutorials: Set<MiniGameType>,
    onStartTutorial: (MiniGameType) -> Unit,
    onBack: () -> Unit
) {
    val tutorials = listOf(
        TutorialCardInfo(
            gameType = MiniGameType.CYBER_PONG,
            title = "Cyber Pong Masterclass",
            description = "Master vertical paddle guidance, angled ball spins, and scoring past defense.",
            objectives = listOf("Paddle drag guidance", "Deflect energy orb", "Score against training dummy"),
            iconEmoji = "🏓",
            isCompleted = completedTutorials.contains(MiniGameType.CYBER_PONG)
        ),
        TutorialCardInfo(
            gameType = MiniGameType.SNAKE_ARENA,
            title = "Snake Slither & Boost",
            description = "Learn grid cornering, energy orb collection, and turbo boost activation.",
            objectives = listOf("Directional turns", "Consume 2 glow orbs", "Avoid self-collision"),
            iconEmoji = "🐍",
            isCompleted = completedTutorials.contains(MiniGameType.SNAKE_ARENA)
        ),
        TutorialCardInfo(
            gameType = MiniGameType.SPACE_BLASTER,
            title = "Space Fighter Marksmanship",
            description = "Pilot your starship, dodge incoming asteroid debris, and aim plasma lasers.",
            objectives = listOf("Steer starship", "Shoot down 3 meteors", "Maintain shield integrity"),
            iconEmoji = "🚀",
            isCompleted = completedTutorials.contains(MiniGameType.SPACE_BLASTER)
        ),
        TutorialCardInfo(
            gameType = MiniGameType.REFLEX_DUEL,
            title = "Tap Blitz Reflex Drill",
            description = "Sharpen fast-finger reaction speed and build multi-tap combo streaks.",
            objectives = listOf("Instant target detection", "Score 3-hit combo", "Outspeed countdown"),
            iconEmoji = "⚡",
            isCompleted = completedTutorials.contains(MiniGameType.REFLEX_DUEL)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Interactive Academy", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Practice in safe sandboxes before real matches", color = TextSecondary, fontSize = 12.sp)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonViolet.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(NeonViolet.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎓", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cadet Training Progress", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            val completedCount = completedTutorials.size
                            Text("$completedCount / ${tutorials.size} Tutorials Mastered", color = NeonCyan, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { completedCount.toFloat() / tutorials.size },
                                modifier = Modifier.fillMaxWidth().height(6.dp),
                                color = NeonCyan,
                                trackColor = DarkCardBorder
                            )
                        }
                    }
                }
            }

            items(tutorials) { tutorial ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tutorial_card_${tutorial.gameType.name}"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (tutorial.isCompleted) NeonLime.copy(alpha = 0.6f) else DarkCardBorder
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(tutorial.iconEmoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(tutorial.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(tutorial.gameType.displayName, color = NeonViolet, fontSize = 12.sp)
                                }
                            }
                            if (tutorial.isCompleted) {
                                Surface(
                                    color = NeonLime.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonLime, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Mastered", color = NeonLime, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(tutorial.description, color = TextSecondary, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Practice Checklist:", color = TextTertiary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        tutorial.objectives.forEach { obj ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (tutorial.isCompleted) NeonLime else NeonCyan, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(obj, color = TextPrimary, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🎁 +${tutorial.rewardXp} XP", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("💰 +${tutorial.rewardCoins} Coins", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onStartTutorial(tutorial.gameType) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (tutorial.isCompleted) DarkSurfaceVariant else NeonViolet
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    if (tutorial.isCompleted) "Replay Drill" else "Start Practice",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
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
