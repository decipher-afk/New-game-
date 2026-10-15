package com.example.matchmaking

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchmakingHubScreen(
    lobbies: List<LobbyRoom>,
    onSelectLobby: (LobbyRoom) -> Unit,
    onWatchLiveGame: (LobbyRoom) -> Unit,
    onCreateLobby: (name: String, game: MiniGameType, isPrivate: Boolean, allowSpectators: Boolean) -> Unit,
    onQuickMatchFound: (MiniGameType) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Browse Lobbies, 1: Watch Live (Spectate)
    var showCreateDialog by remember { mutableStateOf(false) }
    var showJoinCodeDialog by remember { mutableStateOf(false) }
    var isQuickSearching by remember { mutableStateOf(false) }
    var selectedQuickGame by remember { mutableStateOf(MiniGameType.CYBER_PONG) }

    // Quick Search Matchmaking Overlay
    if (isQuickSearching) {
        QuickMatchSearchDialog(
            gameType = selectedQuickGame,
            onCancel = { isQuickSearching = false },
            onMatchFound = {
                isQuickSearching = false
                onQuickMatchFound(selectedQuickGame)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Game Matchmaking & Lobbies", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Instant matchmaking or custom rooms", color = TextSecondary, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showJoinCodeDialog = true }) {
                        Icon(Icons.Default.Pin, contentDescription = "Enter Code", tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = NeonViolet,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create Room", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Quick Match Action Hero Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(NeonCyan.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚡", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Global Quick Match", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Auto-match with players across the globe", color = TextSecondary, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            selectedQuickGame = MiniGameType.values().random()
                            isQuickSearching = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Find Rival", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            // Tabs: Public Lobbies vs Spectate Live Matches
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = NeonCyan
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active Lobbies (${lobbies.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(AccentRed, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Watch Live 👁️")
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // Public Lobbies List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(lobbies) { room ->
                        Card(
                            onClick = { onSelectLobby(room) },
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
                                        .size(42.dp)
                                        .background(NeonViolet.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        when (room.gameType) {
                                            MiniGameType.CYBER_PONG -> "🏓"
                                            MiniGameType.SNAKE_ARENA -> "🐍"
                                            MiniGameType.SPACE_BLASTER -> "🚀"
                                            MiniGameType.REFLEX_DUEL -> "⚡"
                                        },
                                        fontSize = 20.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(room.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        if (room.isPrivate) {
                                            Icon(Icons.Default.Lock, contentDescription = "Private", tint = NeonAmber, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Text("${room.gameType.displayName} · Host: ${room.hostName}", color = TextSecondary, fontSize = 12.sp)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(top = 2.dp)
                                    ) {
                                        Text("📶 ${room.pingMs}ms", color = NeonLime, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("👥 ${room.currentPlayers.size}/${room.maxPlayers}", color = TextTertiary, fontSize = 11.sp)
                                        if (room.allowedSpectators) {
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("👁️ ${room.spectatorsCount} watching", color = AccentRed, fontSize = 11.sp)
                                        }
                                    }
                                }

                                Button(
                                    onClick = { onSelectLobby(room) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text("Join", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                // Live Matches (Spectator Mode Directory)
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Surface(
                            color = AccentRed.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = AccentRed)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Watch competitive high-ranked battles in real time without interfering with players. Send live cheers & reactions!",
                                    color = TextPrimary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    items(lobbies) { room ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = AccentRed.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(modifier = Modifier.size(6.dp).background(AccentRed, CircleShape))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("LIVE MATCH", color = AccentRed, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text("👁️ ${room.spectatorsCount + 14} Spectators", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(room.gameType.displayName, color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("Arena: ${room.name} (#${room.code})", color = TextSecondary, fontSize = 12.sp)

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkSurfaceVariant, RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("CyberViper (1420 🏆)", color = NeonViolet, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("3 - 2", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                    Text("NeonPhantom (1380 🏆)", color = NeonMagenta, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { onWatchLiveGame(room) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Watch as Spectator", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Create Lobby Dialog
        if (showCreateDialog) {
            var roomNameInput by remember { mutableStateOf("Arcade Duel") }
            var selectedGame by remember { mutableStateOf(MiniGameType.CYBER_PONG) }
            var isPrivateToggle by remember { mutableStateOf(false) }
            var allowSpectatorsToggle by remember { mutableStateOf(true) }

            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create New Match Room", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = roomNameInput,
                            onValueChange = { roomNameInput = it },
                            label = { Text("Room Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = NeonViolet
                            ),
                            singleLine = true
                        )

                        Text("Select Game:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        MiniGameType.values().forEach { game ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = selectedGame == game,
                                    onClick = { selectedGame = game },
                                    colors = RadioButtonDefaults.colors(selectedColor = NeonCyan)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(game.displayName, color = TextPrimary, fontSize = 13.sp)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Private Room (Passcode)", color = TextPrimary, fontSize = 13.sp)
                            Switch(
                                checked = isPrivateToggle,
                                onCheckedChange = { isPrivateToggle = it }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Allow Live Spectators", color = TextPrimary, fontSize = 13.sp)
                            Switch(
                                checked = allowSpectatorsToggle,
                                onCheckedChange = { allowSpectatorsToggle = it }
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onCreateLobby(roomNameInput, selectedGame, isPrivateToggle, allowSpectatorsToggle)
                            showCreateDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
                    ) {
                        Text("Create", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = DarkSurface
            )
        }

        // Join Code Dialog
        if (showJoinCodeDialog) {
            var codeInput by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showJoinCodeDialog = false },
                title = { Text("Enter 6-Digit Lobby Code", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = codeInput,
                            onValueChange = { if (it.length <= 6) codeInput = it.uppercase() },
                            placeholder = { Text("e.g. ARC924") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = NeonCyan
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showJoinCodeDialog = false
                            if (lobbies.isNotEmpty()) onSelectLobby(lobbies.first())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Join Room", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJoinCodeDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}

@Composable
fun QuickMatchSearchDialog(
    gameType: MiniGameType,
    onCancel: () -> Unit,
    onMatchFound: () -> Unit
) {
    var statusText by remember { mutableStateOf("Scanning matchmaking server...") }
    var countdown by remember { mutableIntStateOf(3) }
    var matchFound by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val radiusAnim by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_anim"
    )

    LaunchedEffect(Unit) {
        delay(1500)
        statusText = "Found opponent: ShadowNinja (Ping: 24ms)!"
        matchFound = true
        delay(1200)
        onMatchFound()
    }

    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (matchFound) "Match Found!" else "Searching Opponent",
                    color = if (matchFound) NeonLime else NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(gameType.displayName, color = TextSecondary, fontSize = 12.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Radar sweep circle
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = NeonCyan.copy(alpha = 0.2f),
                            radius = size.width / 2,
                            style = Stroke(width = 2f)
                        )
                        drawCircle(
                            color = NeonCyan.copy(alpha = (1f - radiusAnim) * 0.6f),
                            radius = (size.width / 2) * radiusAnim
                        )
                    }
                    Text(if (matchFound) "⚔️" else "📡", fontSize = 36.sp)
                }

                Text(statusText, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel Search", color = AccentRed)
            }
        },
        containerColor = DarkSurface
    )
}
