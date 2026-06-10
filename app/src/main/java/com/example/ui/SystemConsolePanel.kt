package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.AetherViewModel
import kotlinx.coroutines.launch

@Composable
fun SystemConsolePanel(viewModel: AetherViewModel) {
    val isReaderConnected by viewModel.isReaderConnected.collectAsState()
    val apiLatencyMs by viewModel.apiLatencyMs.collectAsState()
    val errorRatePct by viewModel.errorRatePct.collectAsState()
    val cpuLoadPct by viewModel.cpuLoadPct.collectAsState()
    val ramLoadPct by viewModel.ramLoadPct.collectAsState()
    val provisioningSteps by viewModel.provisioningSteps.collectAsState()
    val isProvisioning by viewModel.isProvisioningProgress.collectAsState()

    var showPrometheusRaw by remember { mutableStateOf(false) }
    var inputContractId by remember { mutableStateOf("ESC-2026-104") }
    var inputAmount by remember { mutableStateOf("520000.00") }
    var inputLabel by remember { mutableStateOf("Great Barrier Marine Survey") }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Scroll to bottom when new steps are logged
    LaunchedEffect(provisioningSteps.size) {
        if (provisioningSteps.isNotEmpty()) {
            listState.animateScrollToItem(provisioningSteps.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Slate900)
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "System Telemetry",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                )
                            )
                            Text(
                                text = "Prometheus Metrics & Provisioning Agent",
                                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceMuted.copy(alpha = 0.7f))
                            )
                        }
                        
                        IconButton(
                            onClick = { showPrometheusRaw = !showPrometheusRaw },
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate800)
                                .border(1.dp, Slate700, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = if (showPrometheusRaw) Icons.Default.Info else Icons.Default.Build,
                                contentDescription = "Toggle Raw Prometheus Output",
                                tint = CyanAccent
                            )
                        }
                    }
                }
            }
        },
        containerColor = Slate900
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // PROMETHEUS CRITICAL ALERTS FIRE BANNER
            if (!isReaderConnected) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = AmberAlert.copy(alpha = 0.15f)),
                        border = RowDefaults.CardBorder(AmberAlert.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("prometheus_alert_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Alert Alert",
                                tint = AmberAlert,
                                modifier = Modifier.size(28.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "PROMETHEUS FIRE DECLARED",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AmberAlert,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                )
                                Text(
                                    text = "Alert \"NFCReaderDisconnected\" is firing actively. Reader latency reporting suspended; status flagged CRITICAL to central cluster.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceLight)
                                )
                            }
                        }
                    }
                }
            }

            // PROMETHEUS METRIC STATS GRID OR RAW TERMINAL
            item {
                AnimatedContent(
                    targetState = showPrometheusRaw,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "MetricViewSwitch"
                ) { raw ->
                    if (raw) {
                        // RAW EXPOSITION FORMAT TERMINAL
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Slate800),
                            border = RowDefaults.CardBorder(Slate700),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("prometheus_raw_view")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "PROMETHEUS ENDPOINT: /metrics",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = CyanAccent,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "PORT 8080",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = OnSurfaceMuted,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                val metricsString = """
                                    # HELP aether_reader_connected NFC reader connection status (1 = Connected, 0 = Lost)
                                    # TYPE aether_reader_connected gauge
                                    aether_reader_connected ${if (isReaderConnected) "1" else "0"}
                                    
                                    # HELP aether_api_latency_seconds Response latency for Aether Sydney API validations
                                    # TYPE aether_api_latency_seconds summary
                                    aether_api_latency_seconds_count 154
                                    aether_api_latency_seconds_sum ${String.format(java.util.Locale.US, "%.3f", (apiLatencyMs / 1000f) * 154)}
                                    
                                    # HELP aether_escrow_validations_errors_total Fiduciary validation errors count
                                    # TYPE aether_escrow_validations_errors_total counter
                                    aether_escrow_validations_errors_total ${if (isReaderConnected) "5" else "154"}
                                    
                                    # HELP aether_cpu_utilization_percent Knox CryptoProcessor CPU load percentage
                                    # TYPE aether_cpu_utilization_percent gauge
                                    aether_cpu_utilization_percent $cpuLoadPct
                                    
                                    # HELP aether_memory_utilization_percent Knox Secure Element Heap heap RAM usage %
                                    # TYPE aether_memory_utilization_percent gauge
                                    aether_memory_utilization_percent $ramLoadPct
                                """.trimIndent()

                                Text(
                                    text = metricsString,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = OnSurfaceMuted,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 16.sp
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                        .padding(12.dp)
                                )
                            }
                        }
                    } else {
                        // VISUAL INSTRUMENT PANELS
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // First Row: Connectivity & Latency
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Connectivity Panel
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Slate800),
                                    border = RowDefaults.CardBorder(Slate700),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("metric_panel_connectivity")
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "READER ANTENNA",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = OnSurfaceMuted,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(if (isReaderConnected) EmeraldGlow else CrimsonError)
                                                )
                                                Text(
                                                    text = if (isReaderConnected) "CONNECTED" else "OFFLINE",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = if (isReaderConnected) EmeraldGlow else CrimsonError,
                                                        fontWeight = FontWeight.Bold,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                            Switch(
                                                checked = isReaderConnected,
                                                onCheckedChange = { viewModel.setReaderConnected(it) },
                                                colors = SwitchDefaults.colors(
                                                    checkedThumbColor = EmeraldGlow,
                                                    checkedTrackColor = EmeraldGlow.copy(alpha = 0.2f),
                                                    uncheckedThumbColor = CrimsonError,
                                                    uncheckedTrackColor = CrimsonError.copy(alpha = 0.2f)
                                                ),
                                                modifier = Modifier.scaleScale(0.85f).testTag("reader_toggle_switch")
                                            )
                                        }
                                    }
                                }

                                // Latency Panel
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Slate800),
                                    border = RowDefaults.CardBorder(Slate700),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("metric_panel_latency")
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "API RESPONSE (LAG)",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = OnSurfaceMuted,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (isReaderConnected) "${apiLatencyMs}ms" else "N/A (SLEEP)",
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                color = if (isReaderConnected) (if (apiLatencyMs > 250) AmberAlert else CyanGlow) else OnSurfaceMuted.copy(alpha = 0.5f),
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = if (isReaderConnected) "Sydney Gateway Handshake" else "Central server offline",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = OnSurfaceMuted.copy(alpha = 0.7f),
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                }
                            }

                            // Second Row: Error Rates & TEE cryptoprocessor load
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Slate800),
                                border = RowDefaults.CardBorder(Slate700),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("metric_panel_resources")
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "KNOX TEE OPERATIONS MONITOR",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = OnSurfaceMuted,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.8.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    
                                    // Error Rate indicator bar
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Scan Handshake Error Ratio", style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceLight))
                                            Text(
                                                text = "${errorRatePct}%", 
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = if (errorRatePct > 15) CrimsonError else EmeraldGlow,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace
                                                )
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { errorRatePct / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = if (errorRatePct > 15) CrimsonError else CyanGlow,
                                            trackColor = Slate700
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // CPU / RAM loads
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // CPU
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = "TEE Secure CPU", style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceMuted, fontSize = 10.sp))
                                                Text(text = "$cpuLoadPct%", style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceLight, fontFamily = FontFamily.Monospace))
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = { cpuLoadPct / 100f },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp)),
                                                color = CyanAccent,
                                                trackColor = Slate700
                                            )
                                        }

                                        // RAM
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = "Enclave RAM Heap", style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceMuted, fontSize = 10.sp))
                                                Text(text = "$ramLoadPct%", style = MaterialTheme.typography.labelSmall.copy(color = OnSurfaceLight, fontFamily = FontFamily.Monospace))
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = { ramLoadPct / 100f },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(4.dp)
                                                    .clip(RoundedCornerShape(2.dp)),
                                                color = CyanAccent,
                                                trackColor = Slate700
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // TAG PROVISIONING INTERFACE HEADER
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Fiduciary Tag Provisioner",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    )
                )
                Text(
                    text = "Automated cryptographic loading onto the Secure Element chip. Derived hardware keys are synchronized with the central Registry database.",
                    style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceMuted)
                )
            }

            // TAG PROVISIONER INPUT FORM
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate800),
                    border = RowDefaults.CardBorder(Slate700),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tag_provisioner_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = inputContractId,
                            onValueChange = { inputContractId = it },
                            label = { Text("Regulatory Contract ID") },
                            placeholder = { Text("e.g. ESC-2026-104") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = Slate700
                            ),
                            singleLine = true,
                            enabled = !isProvisioning,
                            modifier = Modifier.fillMaxWidth().testTag("provision_input_id")
                        )

                        OutlinedTextField(
                            value = inputAmount,
                            onValueChange = { inputAmount = it },
                            label = { Text("Asset Value (AUD)") },
                            placeholder = { Text("e.g. 520000.00") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = Slate700
                            ),
                            singleLine = true,
                            enabled = !isProvisioning,
                            modifier = Modifier.fillMaxWidth().testTag("provision_input_amount")
                        )

                        OutlinedTextField(
                            value = inputLabel,
                            onValueChange = { inputLabel = it },
                            label = { Text("Escrow Lease Label") },
                            placeholder = { Text("e.g. Brisbane River Reserve") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = Slate700
                            ),
                            singleLine = true,
                            enabled = !isProvisioning,
                            modifier = Modifier.fillMaxWidth().testTag("provision_input_label")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (provisioningSteps.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = { viewModel.clearAuditLogs(); viewModel.clearProvisionSteps() },
                                    enabled = !isProvisioning,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = OnSurfaceLight
                                    ),
                                    border = RowDefaults.CardBorder(Slate700),
                                    modifier = Modifier.height(48.dp).testTag("provision_clear_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Clear logs")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear")
                                }
                            }

                            Button(
                                onClick = {
                                    val amt = inputAmount.toDoubleOrNull() ?: 0.0
                                    viewModel.provisionNewTag(inputContractId, inputLabel, amt)
                                },
                                enabled = !isProvisioning && inputContractId.isNotBlank() && inputLabel.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyanAccent,
                                    contentColor = Color(0xFF003354)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("provision_execute_btn")
                            ) {
                                if (isProvisioning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color(0xFF003354),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("WRITING TO TEE...")
                                } else {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run provisioner")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("PROVISION ESCROW TAG")
                                }
                            }
                        }
                    }
                }
            }

            // LIVE PROVISIONING CONSOLE LOGS
            if (provisioningSteps.isNotEmpty() || isProvisioning) {
                item {
                    Text(
                        text = "Hardware Provisioning Console",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = OnSurfaceMuted,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
                        border = RowDefaults.CardBorder(Slate700),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 160.dp, max = 300.dp)
                            .testTag("tag_provisioner_terminal")
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(provisioningSteps) { step ->
                                val color = when {
                                    step.contains("ERROR") -> CrimsonError
                                    step.contains("SUCCESS") || step.contains("Success") -> EmeraldGlow
                                    else -> OnSurfaceLight
                                }
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = color,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                )
                            }
                            if (isProvisioning) {
                                item {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(CyanGlow, RoundedCornerShape(3.dp))
                                        )
                                        Text(
                                            text = "Awaiting contactless response frames...",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = CyanGlow,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp
                                            )
                                        )
                                    }
                                }
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

// Extension to scale elements beautifully in code
private fun Modifier.scaleScale(scale: Float): Modifier = this.then(
    Modifier.padding(vertical = ((1f - scale) * 8).dp)
)

object RowDefaults {
    fun CardBorder(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)
}
