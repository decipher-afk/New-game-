package com.example.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.UserProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileAndAuthScreen(
    user: UserProfile,
    onUpdateUsername: (String) -> Unit,
    onOpenModerationCenter: () -> Unit,
    onBack: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editNameText by remember { mutableStateOf(user.username) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gamer Profile & Credentials", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identity Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(NeonViolet.copy(alpha = 0.25f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(user.avatarEmoji, fontSize = 38.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(user.username, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            IconButton(onClick = { showEditDialog = true }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = NeonCyan, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(user.gamerTag, color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${user.equippedTitle} · ${user.rankTier}", color = TextSecondary, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Account Standing
                        Surface(
                            color = if (user.isMuted) AccentRed.copy(alpha = 0.2f) else NeonLime.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(if (user.isMuted) AccentRed else NeonLime, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    if (user.isMuted) "Status: Chat Muted (${user.muteRemainingMinutes}m left)" else "Status: Good Standing (Verified)",
                                    color = if (user.isMuted) AccentRed else NeonLime,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Stats Grid
            item {
                Text("Match Career Statistics", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Win Rate", color = TextSecondary, fontSize = 11.sp)
                            Text("${user.winRate}%", color = NeonLime, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("${user.matchesWon}W / ${user.matchesPlayed - user.matchesWon}L", color = TextTertiary, fontSize = 10.sp)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Matches", color = TextSecondary, fontSize = 11.sp)
                            Text("${user.matchesPlayed}", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("Favorite: ${user.favoriteGame.name.take(6)}", color = TextTertiary, fontSize = 10.sp)
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Trophies", color = TextSecondary, fontSize = 11.sp)
                            Text("${user.trophies}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("Top 5% Global", color = TextTertiary, fontSize = 10.sp)
                        }
                    }
                }
            }

            // Quick Access & Moderation Tools
            item {
                Card(
                    onClick = onOpenModerationCenter,
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = AccentRed, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Moderation & Safety Center", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Review community reports, warnings & appeals", color = TextSecondary, fontSize = 11.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary)
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Gamer Tag", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = {
                OutlinedTextField(
                    value = editNameText,
                    onValueChange = { editNameText = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = NeonCyan
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNameText.isNotBlank()) onUpdateUsername(editNameText)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Text("Save", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }
}
