package com.example.moderation

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun ReportUserDialog(
    targetPlayerName: String,
    currentLobbyName: String,
    onDismiss: () -> Unit,
    onSubmitReport: (reason: ReportReason, details: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf(ReportReason.HARASSMENT) }
    var detailsText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = AccentRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Report Player", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Reporting: $targetPlayerName in $currentLobbyName",
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text("Violation Reason:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                ReportReason.values().forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(selectedColor = AccentRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(reason.displayName, color = TextPrimary, fontSize = 13.sp)
                    }
                }

                OutlinedTextField(
                    value = detailsText,
                    onValueChange = { detailsText = it },
                    label = { Text("Details or chat excerpt (Optional)") },
                    placeholder = { Text("Describe what happened...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentRed,
                        unfocusedBorderColor = DarkCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isSubmitting = true
                    onSubmitReport(selectedReason, detailsText)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
            ) {
                Text("Submit Report", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = DarkSurface
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationCenterScreen(
    reports: List<ModerationReport>,
    actionLogs: List<ModerationActionLog>,
    onTakeAction: (reportId: String, action: ModerationActionType, targetPlayer: String, reason: String, notes: String) -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var actionDialogReport by remember { mutableStateOf<ModerationReport?>(null) }
    var actionNotes by remember { mutableStateOf("") }
    var chosenActionType by remember { mutableStateOf(ModerationActionType.WARNING) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Moderation & Trust Center", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Enforce community guidelines & review violations", color = TextSecondary, fontSize = 12.sp)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkSurface,
                contentColor = NeonCyan
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Pending Reports (${reports.count { it.status == ReportStatus.PENDING }})")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Audit Action Log (${actionLogs.size})") }
                )
            }

            if (selectedTab == 0) {
                // Pending reports list
                val pending = reports.filter { it.status == ReportStatus.PENDING }
                if (pending.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🛡️", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("No Pending Reports", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("The arcade community is clean and orderly!", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(pending) { report ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = AccentRed.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                report.reason.displayName,
                                                color = AccentRed,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Text(report.timestamp, color = TextTertiary, fontSize = 11.sp)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Accused: ${report.targetPlayerName}",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        "Reported by: ${report.reporterName} in ${report.matchOrLobby}",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )

                                    if (report.details.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            color = DarkSurfaceVariant,
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                "\"${report.details}\"",
                                                color = TextPrimary,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                onTakeAction(
                                                    report.id,
                                                    ModerationActionType.DISMISS,
                                                    report.targetPlayerName,
                                                    report.reason.displayName,
                                                    "Insufficient evidence"
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                                        ) {
                                            Text("Dismiss", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                actionDialogReport = report
                                                chosenActionType = ModerationActionType.WARNING
                                                actionNotes = ""
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                                            modifier = Modifier.weight(1.5f)
                                        ) {
                                            Text("Review & Act", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Action Log Tab
                if (actionLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No moderation actions recorded yet.", color = TextSecondary, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(actionLogs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            color = when (log.actionType) {
                                                ModerationActionType.WARNING -> NeonAmber.copy(alpha = 0.2f)
                                                ModerationActionType.MUTE_1H, ModerationActionType.MUTE_24H -> AccentRed.copy(alpha = 0.2f)
                                                ModerationActionType.BAN -> Color.Red.copy(alpha = 0.3f)
                                                ModerationActionType.DISMISS -> DarkCardBorder
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                log.actionType.displayName,
                                                color = when (log.actionType) {
                                                    ModerationActionType.WARNING -> NeonAmber
                                                    ModerationActionType.MUTE_1H, ModerationActionType.MUTE_24H -> AccentRed
                                                    ModerationActionType.BAN -> Color.Red
                                                    ModerationActionType.DISMISS -> TextSecondary
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                        Text(log.timestamp, color = TextTertiary, fontSize = 11.sp)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        "Target: ${log.targetPlayerName} · Reason: ${log.reason}",
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        "Note: ${log.moderatorNotes}",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Decision Dialog
        actionDialogReport?.let { report ->
            AlertDialog(
                onDismissRequest = { actionDialogReport = null },
                title = {
                    Text("Apply Moderation Penalty", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Accused Player: ${report.targetPlayerName}", color = NeonCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)

                        Text("Select Enforcement Action:", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        listOf(
                            ModerationActionType.WARNING,
                            ModerationActionType.MUTE_1H,
                            ModerationActionType.MUTE_24H,
                            ModerationActionType.BAN
                        ).forEach { action ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                            ) {
                                RadioButton(
                                    selected = chosenActionType == action,
                                    onClick = { chosenActionType = action },
                                    colors = RadioButtonDefaults.colors(selectedColor = AccentRed)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(action.displayName, color = TextPrimary, fontSize = 13.sp)
                            }
                        }

                        OutlinedTextField(
                            value = actionNotes,
                            onValueChange = { actionNotes = it },
                            label = { Text("Moderator Note") },
                            placeholder = { Text("e.g. Chat harassment confirmed") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            onTakeAction(
                                report.id,
                                chosenActionType,
                                report.targetPlayerName,
                                report.reason.displayName,
                                actionNotes.ifBlank { "Action enforced per community policy" }
                            )
                            actionDialogReport = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                    ) {
                        Text("Enforce Penalty", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { actionDialogReport = null }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}
