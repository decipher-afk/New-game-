package com.example.lobby

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyRoomScreen(
    lobby: LobbyRoom,
    currentUser: UserProfile,
    onStartGame: (isSpectator: Boolean) -> Unit,
    onLeaveLobby: () -> Unit,
    onReportPlayer: (playerName: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isReady by remember { mutableStateOf(false) }
    var isSpectatorMode by remember { mutableStateOf(false) }

    // Voice Chat State
    var isMicOn by remember { mutableStateOf(false) }
    var isDeafened by remember { mutableStateOf(false) }
    var activeSpeaker by remember { mutableStateOf<String?>(null) }

    // Chat State
    var chatFilterTab by remember { mutableIntStateOf(0) } // 0: All, 1: Spectator
    var chatMessages by remember {
        mutableStateOf(
            listOf(
                ChatMessage("c1", "sys", "System", "🤖", "Welcome to lobby #${lobby.code}! Match starts when all players are ready.", "12:00", isSystem = true),
                ChatMessage("c2", "u_phantom", "NeonPhantom", "🔥", "Hey everyone! Ready for a quick round of ${lobby.gameType.displayName}?", "12:01", isSpectator = false),
                ChatMessage("c3", "u_spectator1", "ObserverPro", "👁️", "Hyped to watch this match!", "12:02", isSpectator = true)
            )
        )
    }
    var currentInputText by remember { mutableStateOf("") }
    val chatListState = rememberLazyListState()

    // Voice chat simulated audio waves animation
    val infiniteTransition = rememberInfiniteTransition(label = "audio_wave")
    val waveAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_anim"
    )

    // Simulate occasional incoming chat messages
    LaunchedEffect(lobby.id) {
        while (true) {
            delay(12000)
            val sampleReplies = listOf(
                "Let's go!",
                "Check out my new neon skin in action!",
                "Good luck have fun!",
                "Who's streaming this?",
                "GG in advance!"
            )
            val randomReply = sampleReplies.random()
            chatMessages = chatMessages + ChatMessage(
                id = "c_${System.currentTimeMillis()}",
                senderId = "u_phantom",
                senderName = "NeonPhantom",
                senderAvatar = "🔥",
                text = randomReply,
                timestamp = "12:03",
                isSpectator = false
            )
            chatListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(lobby.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = DarkCardBorder,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "#${lobby.code}",
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text("${lobby.gameType.displayName} · Ping: ${lobby.pingMs}ms", color = TextSecondary, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onLeaveLobby) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Leave", tint = TextPrimary)
                    }
                },
                actions = {
                    // Spectator Switcher
                    FilterChip(
                        selected = isSpectatorMode,
                        onClick = { isSpectatorMode = !isSpectatorMode },
                        label = {
                            Text(
                                if (isSpectatorMode) "Spectator 👁️" else "Player 🎮",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentRed.copy(alpha = 0.2f),
                            selectedLabelColor = AccentRed,
                            containerColor = DarkSurfaceVariant,
                            labelColor = TextPrimary
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Player Slots Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Combatants (2/2)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("👁️ ${lobby.spectatorsCount + if (isSpectatorMode) 1 else 0} Viewers", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Slot 1 (Current User)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(NeonViolet.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(currentUser.avatarEmoji, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(currentUser.username, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("(You)", color = NeonCyan, fontSize = 11.sp)
                            }
                            Text("Level ${currentUser.level} · ${currentUser.rankTier}", color = TextSecondary, fontSize = 11.sp)
                        }

                        if (!isSpectatorMode) {
                            Surface(
                                color = if (isReady) NeonLime.copy(alpha = 0.2f) else DarkCardBorder,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    if (isReady) "READY" else "NOT READY",
                                    color = if (isReady) NeonLime else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Surface(
                                color = AccentRed.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("SPECTATING", color = AccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Slot 2 (Opponent)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(NeonMagenta.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔥", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("NeonPhantom", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("[HOST]", color = AccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Text("Level 15 · Platinum I · 28ms", color = TextSecondary, fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = { onReportPlayer("NeonPhantom") },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Report, contentDescription = "Report", tint = TextTertiary, modifier = Modifier.size(16.dp))
                        }

                        Surface(
                            color = NeonLime.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "READY",
                                color = NeonLime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Real-Time Voice Chat Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = {
                                if (currentUser.isMuted) return@IconButton
                                isMicOn = !isMicOn
                                activeSpeaker = if (isMicOn) currentUser.username else null
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    if (isMicOn) NeonCyan.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                    CircleShape
                                )
                        ) {
                            Icon(
                                if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Mic Toggle",
                                tint = if (currentUser.isMuted) AccentRed else if (isMicOn) NeonCyan else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Waveform Visualizer
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (currentUser.isMuted) "Mic Muted by Moderation"
                                    else if (isMicOn) "You are speaking..."
                                    else "Voice Room Active",
                                    color = if (currentUser.isMuted) AccentRed else if (isMicOn) NeonCyan else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            // Live wave canvas
                            Canvas(modifier = Modifier.size(width = 80.dp, height = 12.dp)) {
                                val bars = 6
                                val barWidth = 4f
                                val spacing = 8f
                                for (i in 0 until bars) {
                                    val barH = if (isMicOn) (size.height * (0.3f + waveAnim * 0.7f * ((i % 3 + 1) / 3f))) else 3f
                                    drawLine(
                                        color = if (isMicOn) NeonCyan else DarkCardBorder,
                                        start = Offset(i * (barWidth + spacing), size.height / 2 - barH / 2),
                                        end = Offset(i * (barWidth + spacing), size.height / 2 + barH / 2),
                                        strokeWidth = barWidth
                                    )
                                }
                            }
                        }
                    }

                    // Deafen button
                    IconButton(
                        onClick = { isDeafened = !isDeafened },
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (isDeafened) AccentRed.copy(alpha = 0.2f) else DarkSurfaceVariant,
                                CircleShape
                            )
                    ) {
                        Icon(
                            if (isDeafened) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Deafen Toggle",
                            tint = if (isDeafened) AccentRed else TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Chat Channel Tabs (All Chat vs Spectator Only)
            TabRow(
                selectedTabIndex = chatFilterTab,
                containerColor = DarkSurface,
                contentColor = NeonCyan,
                modifier = Modifier.height(38.dp)
            ) {
                Tab(
                    selected = chatFilterTab == 0,
                    onClick = { chatFilterTab = 0 },
                    text = { Text("Match Chat", fontSize = 12.sp) }
                )
                Tab(
                    selected = chatFilterTab == 1,
                    onClick = { chatFilterTab = 1 },
                    text = { Text("Spectator Chat 👁️", fontSize = 12.sp) }
                )
            }

            // Quick Tactical Chat Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val quickPicks = listOf("Ready!", "Good luck!", "Rematch!", "GG!")
                quickPicks.forEach { pick ->
                    Surface(
                        onClick = {
                            if (currentUser.isMuted) return@Surface
                            chatMessages = chatMessages + ChatMessage(
                                id = "msg_${System.currentTimeMillis()}",
                                senderId = currentUser.id,
                                senderName = currentUser.username,
                                senderAvatar = currentUser.avatarEmoji,
                                text = pick,
                                timestamp = "12:04",
                                isSpectator = isSpectatorMode
                            )
                            coroutineScope.launch {
                                chatListState.animateScrollToItem(chatMessages.size - 1)
                            }
                        },
                        color = DarkSurfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            pick,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Chat Messages Feed
            val filteredMessages = if (chatFilterTab == 1) {
                chatMessages.filter { it.isSpectator || it.isSystem }
            } else {
                chatMessages
            }

            LazyColumn(
                state = chatListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(14.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredMessages) { msg ->
                    if (msg.isSystem) {
                        Surface(
                            color = DarkSurfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                msg.text,
                                color = NeonAmber,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(DarkSurfaceVariant, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(msg.senderAvatar, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(msg.senderName, color = if (msg.senderName == currentUser.username) NeonCyan else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    if (msg.isSpectator) {
                                        Surface(
                                            color = AccentRed.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text("SPECTATOR", color = AccentRed, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(msg.timestamp, color = TextTertiary, fontSize = 9.sp)
                                }
                                Text(msg.text, color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Chat Input Box (or Muted Banner)
            if (currentUser.isMuted) {
                Surface(
                    color = AccentRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MicOff, contentDescription = null, tint = AccentRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "🔇 You are currently muted by moderation (${currentUser.muteRemainingMinutes}m left)",
                            color = AccentRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = currentInputText,
                        onValueChange = { currentInputText = it },
                        placeholder = { Text(if (isSpectatorMode) "Send spectator cheer..." else "Chat with lobby...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = DarkCardBorder
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (currentInputText.isNotBlank()) {
                                chatMessages = chatMessages + ChatMessage(
                                    id = "msg_${System.currentTimeMillis()}",
                                    senderId = currentUser.id,
                                    senderName = currentUser.username,
                                    senderAvatar = currentUser.avatarEmoji,
                                    text = currentInputText,
                                    timestamp = "12:05",
                                    isSpectator = isSpectatorMode
                                )
                                currentInputText = ""
                                coroutineScope.launch {
                                    chatListState.animateScrollToItem(chatMessages.size - 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(NeonViolet, CircleShape)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }

            // Bottom Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isSpectatorMode) {
                    OutlinedButton(
                        onClick = { isReady = !isReady },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isReady) NeonLime.copy(alpha = 0.15f) else Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isReady) NeonLime else DarkCardBorder)
                    ) {
                        Text(
                            if (isReady) "Cancel Ready" else "Ready Up!",
                            color = if (isReady) NeonLime else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onStartGame(false) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Text("Start Match 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = { onStartGame(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Join as Spectator 👁️", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
