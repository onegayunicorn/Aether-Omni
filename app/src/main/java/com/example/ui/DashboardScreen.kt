package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.EscrowContract
import com.example.ui.theme.*
import com.example.viewmodel.AetherViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: AetherViewModel,
    onNavigateToHce: (EscrowContract) -> Unit,
    modifier: Modifier = Modifier
) {
    val contracts by viewModel.contracts.collectAsState()
    val securityLevel by viewModel.securityLevel.collectAsState()
    val mountedContractId by viewModel.mountedContractId.collectAsState()

    val totalAssets = remember(contracts) {
        contracts.sumOf { it.amountAud }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Header ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Aether-Omni",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "v2.0",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = CyanGlow,
                                fontWeight = FontWeight.Light,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                    }
                    Text(
                        text = "Escrow Validation Node • Brisbane, QLD",
                        style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceMuted.copy(alpha = 0.7f))
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (securityLevel.level == "UNPROVISIONED") CrimsonError.copy(alpha = 0.15f) else EmeraldGlow.copy(alpha = 0.15f),
                    modifier = Modifier.border(
                        width = 1.dp,
                        color = if (securityLevel.level == "UNPROVISIONED") CrimsonError.copy(alpha = 0.3f) else EmeraldGlow.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(20.dp)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (securityLevel.level == "UNPROVISIONED") CrimsonError else EmeraldGlow)
                        )
                        Text(
                            text = if (securityLevel.level == "UNPROVISIONED") "MUTED" else "SECURE NODE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (securityLevel.level == "UNPROVISIONED") CrimsonError else EmeraldGlow,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                    }
                }
            }
        }

        // --- Balance Display & Canvas Chart ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("escrow_pool_balance"),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate700)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOTAL ESCROW UNDER MANAGEMENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.Gray,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale("en", "AU")).format(totalAssets),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = CyanGlow,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Fiduciary Registry Activity (Brisbane Hub)",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Custom neon chart
                    EscrowActivityChart()
                }
            }
        }

        // --- Hardware Security Band ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (securityLevel.isHardwareBacked) Slate800 else Slate800.copy(alpha = 0.8f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(
                    1.dp,
                    if (securityLevel.level == "UNPROVISIONED") CrimsonError.copy(alpha = 0.5f)
                    else if (securityLevel.isHardwareBacked) EmeraldGlow.copy(alpha = 0.5f)
                    else AmberAlert.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (securityLevel.level == "UNPROVISIONED") Icons.Default.Warning
                            else if (securityLevel.isHardwareBacked) Icons.Default.Lock
                            else Icons.Default.Face,
                            contentDescription = "Shield Guard",
                            tint = if (securityLevel.level == "UNPROVISIONED") CrimsonError
                            else if (securityLevel.isHardwareBacked) EmeraldGlow
                            else AmberAlert
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "KNOX TEE TIER: ${securityLevel.level}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = securityLevel.description,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.provisionSecureKey() },
                            modifier = Modifier.weight(1f).testTag("provision_key_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Sync", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prov. Key", fontSize = 12.sp, color = Slate900)
                        }
                        
                        if (securityLevel.level != "UNPROVISIONED") {
                            Button(
                                onClick = { viewModel.destroySecureKey() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonError),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Wipe", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Wipe Key", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // --- Section Title ---
        item {
            Text(
                text = "ACTIVE FIDUCIARY CONTRACTS",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // --- Contracts Registry List ---
        if (contracts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = CyanAccent)
                }
            }
        } else {
            items(contracts) { contract ->
                ContractItemRow(
                    contract = contract,
                    isMounted = mountedContractId == contract.contractId,
                    isPrerequisiteMet = securityLevel.level != "UNPROVISIONED",
                    onMountClick = { onNavigateToHce(contract) },
                    onSimulateNfcScan = {
                        // Simulate a physical card scanning against this token
                        viewModel.scanPhysicalTag(
                            tagUid = "NFC-TAG-${contract.contractId}",
                            contractHashPayload = if (contract.contractId == "ESC-2026-101") "ESC-2026-101-FRUIT-SEED-QLD"
                            else if (contract.contractId == "ESC-2026-102") "ESC-2026-102-RIVER-CORRIDOR"
                            else "ESC-2026-103-VENTURE-ACQUISITION"
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ContractItemRow(
    contract: EscrowContract,
    isMounted: Boolean,
    isPrerequisiteMet: Boolean,
    onMountClick: () -> Unit,
    onSimulateNfcScan: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("contract_card_${contract.contractId}"),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (isMounted) CyanAccent else Slate700
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1.0f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = contract.contractId,
                            style = MaterialTheme.typography.titleSmall.copy(
                                color = CyanGlow,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (contract.status == "RELEASE_AUTHORIZED" || contract.status == "COMPLETED") EmeraldGlow.copy(alpha = 0.2f)
                            else Slate700
                        ) {
                            Text(
                                text = if (contract.status == "RELEASE_AUTHORIZED") "RELEASED" else contract.status,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = if (contract.status == "RELEASE_AUTHORIZED" || contract.status == "COMPLETED") EmeraldGlow
                                    else Color.White,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = contract.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Text(
                    text = NumberFormat.getCurrencyInstance(Locale("en", "AU")).format(contract.amountAud),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Slate700)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = "Jurisdiction", modifier = Modifier.size(12.dp), tint = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${contract.userRole} • ${contract.jurisdiction}",
                    fontSize = 11.sp,
                    color = Color.LightGray,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // HCE Mount action
                Button(
                    onClick = onMountClick,
                    enabled = isPrerequisiteMet && contract.status == "PENDING",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isMounted) EmeraldGlow else Slate700,
                        disabledContainerColor = Slate700.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isMounted) Icons.Default.CheckCircle else Icons.Default.Lock,
                        contentDescription = "HCE Coil Mount",
                        modifier = Modifier.size(14.dp),
                        tint = if (isMounted) Slate900 else Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isMounted) "EMULATING" else "MOUNT HCE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isMounted) Slate900 else Color.White
                    )
                }

                // Simulation Reader Mode action
                Button(
                    onClick = onSimulateNfcScan,
                    enabled = contract.status == "PENDING",
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f).testTag("simulate_nfc_scan_${contract.contractId}")
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Simulate Scan", modifier = Modifier.size(14.dp), tint = CyanGlow)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("TAP TAG", fontSize = 10.sp, color = CyanGlow, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EscrowActivityChart() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(vertical = 4.dp)
    ) {
        val width = size.width
        val height = size.height

        val points = listOf(
            Offset(0.0f, height * 0.8f),
            Offset(width * 0.2f, height * 0.65f),
            Offset(width * 0.4f, height * 0.72f),
            Offset(width * 0.6f, height * 0.35f),
            Offset(width * 0.8f, height * 0.48f),
            Offset(width, height * 0.1f)
        )

        // Draw background gradient area
        val fillPath = Path().apply {
            moveTo(0.0f, height)
            lineTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
            lineTo(width, height)
            close()
        }
        
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(CyanAccent.copy(alpha = 0.25f), Color.Transparent)
            )
        )

        // Draw line paths
        val linePath = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        drawPath(
            path = linePath,
            color = CyanAccent,
            style = Stroke(width = 2.5.dp.toPx())
        )

        // Draw nodes/points
        points.forEach { point ->
            drawCircle(
                color = CyanGlow,
                radius = 4.dp.toPx(),
                center = point
            )
            drawCircle(
                color = Slate900,
                radius = 2.dp.toPx(),
                center = point
            )
        }
    }
}
