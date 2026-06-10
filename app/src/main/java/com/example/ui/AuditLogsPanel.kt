package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.AuditEvent
import com.example.ui.theme.*
import com.example.viewmodel.AetherViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogsPanel(
    viewModel: AetherViewModel,
    modifier: Modifier = Modifier
) {
    val auditLogs by viewModel.auditLogs.collectAsState()
    val backendUrl by viewModel.backendUrl.collectAsState()
    val jwtToken by viewModel.jwtToken.collectAsState()
    val userId by viewModel.userId.collectAsState()

    var showConfigDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Tab Header ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Compliance & Audits",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                )
                Text(
                    text = "Queensland Privacy Act 2009 Registry",
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceMuted.copy(alpha = 0.7f))
                )
            }

            IconButton(
                onClick = { showConfigDialog = true },
                modifier = Modifier.testTag("show_configuration_button")
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Config App", tint = CyanGlow)
            }
        }

        // --- Queensland Banner Card ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Slate700)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Sec Info",
                        tint = CyanGlow,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "OFFICIAL FIDUCIARY STATUTE NOTICE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "In accordance with Section 33 of the Queensland Privacy Act 2009, all escrow validation, tag scanning, and cryptographic operations executed on this Samsung mobile node are recorded into a secure local ledger. Each event block carries a hardware digital signature generating tamper evidence to safeguard client data.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.LightGray,
                                lineHeight = 15.sp
                            )
                        )
                    }
                }
            }
        }

        // --- Log Ledger Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "IMMUTABLE AUDIT LOGS [${auditLogs.size}]",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.5.sp
                )
            )

            if (auditLogs.isNotEmpty()) {
                TextButton(
                    onClick = { viewModel.clearAuditLogs() },
                    colors = ButtonDefaults.textButtonColors(contentColor = CrimsonError),
                    modifier = Modifier.testTag("wipe_db_logs_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Purge logs", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Purge Logs", fontSize = 11.sp)
                }
            }
        }

        // --- Audit List ---
        if (auditLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Slate800)
                    .border(1.dp, Slate700, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recorded compliance audits in log space.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(auditLogs) { log ->
                    AuditItemView(log = log)
                }
            }
        }
    }

    // --- Configuration Dialog ---
    if (showConfigDialog) {
        var tempUrl by remember { mutableStateOf(backendUrl) }
        var tempUser by remember { mutableStateOf(userId) }
        var tempJwt by remember { mutableStateOf(jwtToken) }

        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = {
                Text(
                    "Aether-Omni System Parameters",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Configure connection values to align client with external FastAPI or local provisioning servers.",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    
                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        label = { Text("FastAPI Server URL") },
                        modifier = Modifier.fillMaxWidth().testTag("config_url_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = tempUser,
                        onValueChange = { tempUser = it },
                        label = { Text("User Identifier") },
                        modifier = Modifier.fillMaxWidth().testTag("config_user_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = tempJwt,
                        onValueChange = { tempJwt = it },
                        label = { Text("Client Handshake JWT Bearer") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = Slate700,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setBackendUrl(tempUrl)
                        viewModel.setUserId(tempUser)
                        viewModel.setJwtToken(tempJwt)
                        showConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                    modifier = Modifier.testTag("apply_configuration_button")
                ) {
                    Text("Apply Sync", color = Slate900)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = Slate800,
            tonalElevation = 6.dp
        )
    }
}

@Composable
fun AuditItemView(log: AuditEvent) {
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US) }
    val dateString = remember(log.timestamp) { formatter.format(Date(log.timestamp)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        border = BorderStroke(1.dp, Slate700)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Event Type, Status, Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.eventType,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = CyanGlow,
                        fontFamily = FontFamily.Monospace
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateString,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontFamily = FontFamily.Monospace
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(2.dp),
                        color = if (log.status == "SUCCESS") EmeraldGlow.copy(alpha = 0.2f)
                        else CrimsonError.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = log.status,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (log.status == "SUCCESS") EmeraldGlow else CrimsonError,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            
            // Details message
            Text(
                text = log.details,
                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Signature footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${log.auditId}",
                    fontSize = 8.sp,
                    color = Color.LightGray,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(0.4f)
                )
                Text(
                    text = "SHA-256 SIGN: ${log.signatureHash.take(24)}...",
                    fontSize = 8.sp,
                    color = Color.LightGray,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(0.6f)
                )
            }
        }
    }
}
