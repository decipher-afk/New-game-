package com.example.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.example.model.UserProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAndAuthScreen(
    user: UserProfile,
    onUpdateProfile: (name: String, avatar: String, bio: String) -> Unit,
    onOpenModerationCenter: () -> Unit,
    onBack: () -> Unit
) {
    var isEditingProfile by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf(user.username) }
    var selectedAvatar by remember { mutableStateOf(user.avatarEmoji) }
    var editBioText by remember { mutableStateOf(user.statusBio) }

    val availableAvatars = remember {
        listOf(
            "⚡", "🎮", "🤖", "🐉",
            "🥷", "👑", "🔥", "🚀",
            "🦊", "👾", "🥋", "🏆",
            "🦄", "🎯", "💀", "🛸"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Profile & Identity Management", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Customize your public persona & track lifetime stats", color = TextSecondary, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            editNameText = user.username
                            selectedAvatar = user.avatarEmoji
                            editBioText = user.statusBio
                            isEditingProfile = !isEditingProfile
                        }
                    ) {
                        Icon(
                            if (isEditingProfile) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = NeonCyan
                        )
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
                Spacer(modifier = Modifier.height(4.dp))

                // Hero Profile Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_management_hero"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonViolet.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar with animated gradient border
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .background(
                                    Brush.linearGradient(listOf(NeonViolet, NeonCyan, NeonMagenta)),
                                    CircleShape
                                )
                                .padding(3.dp)
                                .background(DarkSurface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user.avatarEmoji, fontSize = 44.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.username, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = NeonCyan.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    user.gamerTag,
                                    color = NeonCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            "${user.equippedTitle} · ${user.rankTier}",
                            color = NeonAmber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        if (user.statusBio.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "\"${user.statusBio}\"",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                editNameText = user.username
                                selectedAvatar = user.avatarEmoji
                                editBioText = user.statusBio
                                isEditingProfile = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile & Avatar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // LEVEL & PROGRESSION DETAILED CARD
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(NeonCyan.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("⭐", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Level ${user.level} Combatant", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Rank Tier: ${user.rankTier}", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                            Text("${user.xp} / ${user.maxXp} XP", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { (user.xp.toFloat() / user.maxXp).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = NeonCyan,
                            trackColor = DarkCardBorder
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Next Level Rewards Preview
                        Surface(
                            color = DarkSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🎁", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Level ${user.level + 1} Unlock Perks", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("+600🪙 Bonus, +20💎 Gems, \"Arcade Titan\" Title", color = TextSecondary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // TOTAL EARNED CURRENCY SECTION
            item {
                Text("Financial Career & Total Earnings", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Lifetime Coins
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AccentGold.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🪙", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Coins Career", color = TextSecondary, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${user.totalCoinsEarned}", color = AccentGold, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                            Text("Total Earned", color = TextTertiary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = DarkSurfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Wallet: ${user.coins}🪙", color = TextSecondary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }

                    // Total Lifetime Gems
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💎", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gems Career", color = TextSecondary, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("${user.totalGemsEarned}", color = NeonCyan, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                            Text("Total Earned", color = TextTertiary, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = DarkSurfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Vault: ${user.gems}💎", color = TextSecondary, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                    }
                }
            }

            // CAREER MATCH STATS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Win Rate", color = TextSecondary, fontSize = 11.sp)
                            Text("${user.winRate}%", color = NeonLime, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${user.matchesWon} Won / ${user.matchesPlayed - user.matchesWon} Lost", color = TextTertiary, fontSize = 10.sp)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Matches", color = TextSecondary, fontSize = 11.sp)
                            Text("${user.matchesPlayed}", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Favorite: ${user.favoriteGame.name.take(6)}", color = TextTertiary, fontSize = 10.sp)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Trophies", color = TextSecondary, fontSize = 11.sp)
                            Text("${user.trophies} 🏆", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Top 5% Rank", color = TextTertiary, fontSize = 10.sp)
                        }
                    }
                }
            }

            // ACCOUNT STANDING & MODERATION
            item {
                Card(
                    onClick = onOpenModerationCenter,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = AccentRed, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Moderation & Safety Center", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                if (user.isMuted) "Active Penalty: Chat Muted (${user.muteRemainingMinutes}m left)"
                                else "Account Status: Good Standing (Verified)",
                                color = if (user.isMuted) AccentRed else NeonLime,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // FULL PROFILE & AVATAR EDIT MODAL DIALOG
    if (isEditingProfile) {
        AlertDialog(
            onDismissRequest = { isEditingProfile = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✏️ Edit Gamer Profile", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Avatar Selection Gallery
                    Text("Choose Player Avatar:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    // Selected Avatar Preview
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(NeonViolet.copy(alpha = 0.3f), CircleShape)
                                .border(2.dp, NeonCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(selectedAvatar, fontSize = 32.sp)
                        }
                    }

                    // 4x4 Grid of Avatars
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (row in 0 until 4) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                for (col in 0 until 4) {
                                    val index = row * 4 + col
                                    if (index < availableAvatars.size) {
                                        val avatar = availableAvatars[index]
                                        val isChosen = selectedAvatar == avatar
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(
                                                    if (isChosen) NeonCyan.copy(alpha = 0.3f) else DarkSurfaceVariant,
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .border(
                                                    if (isChosen) 2.dp else 1.dp,
                                                    if (isChosen) NeonCyan else DarkCardBorder,
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .clickable { selectedAvatar = avatar },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(avatar, fontSize = 22.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Custom Display Name Input
                    Column {
                        Text("Custom Display Name:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editNameText,
                            onValueChange = {
                                if (it.length <= 16) editNameText = it
                            },
                            placeholder = { Text("Enter gamer name") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = DarkCardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text("${editNameText.length}/16 characters", color = TextTertiary, fontSize = 10.sp)
                    }

                    // Status Bio Message
                    Column {
                        Text("Gamer Status Message:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = editBioText,
                            onValueChange = {
                                if (it.length <= 60) editBioText = it
                            },
                            placeholder = { Text("e.g. Ready for cyber clash!") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = DarkCardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalName = if (editNameText.isNotBlank()) editNameText.trim() else user.username
                        onUpdateProfile(finalName, selectedAvatar, editBioText.trim())
                        isEditingProfile = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Save Changes", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditingProfile = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
