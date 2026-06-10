package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.database.EscrowContract
import com.example.ui.theme.*
import com.example.viewmodel.AetherViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SecureElementHcePanel(
    viewModel: AetherViewModel,
    modifier: Modifier = Modifier
) {
    val mountedContractId by viewModel.mountedContractId.collectAsState()
    val contracts by viewModel.contracts.collectAsState()
    val securityLevel by viewModel.securityLevel.collectAsState()

    val mountedContract = remember(mountedContractId, contracts) {
        contracts.find { it.contractId == mountedContractId }
    }

    var isBiometricallyUnlocked by remember { mutableStateOf(false) }
    var isScanningBiometric by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Slate900)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- Tab Title ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Secure Element",
                style = MaterialTheme.typography.titleLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                )
            )
            Text(
                text = "Samsung Knox TEE Card Emulation (HCE)",
                style = MaterialTheme.typography.bodySmall.copy(color = OnSurfaceMuted.copy(alpha = 0.7f))
            )
        }

        if (mountedContract == null) {
            // --- Empty State: No Contract Mounted ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Empty Mount",
                        tint = Slate600,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "EMULATOR ENVELOPE EMPTY",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    Text(
                        text = "Go to the Dashboard in Aether-Omni, select a pending transaction contract, and tap \"MOUNT HCE\" to connect the digital tokens to Android's high-frequency coils.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                }
            }
        } else {
            // --- Active HCE Mount ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("active_mount_panel"),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Slate700)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CURRENTLY MOUNTED ON COILS",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, letterSpacing = 1.sp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mountedContract.contractId,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = CyanGlow,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = mountedContract.title,
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale("en", "AU")).format(mountedContract.amountAud),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            isBiometricallyUnlocked = false
                            viewModel.unmountContractFromNfcHce()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonError.copy(alpha = 0.2f)),
                        shape = RoundedCornerShape(4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Text("Dismount Coils", color = CrimsonError, fontSize = 11.sp)
                    }
                }
            }

            // --- Visual NFC Loop Emulator ---
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val pulseRadius by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 0.0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )

                // Copper induction coil drawings
                Canvas(modifier = Modifier.size(200.dp)) {
                    val center = size / 2.0f
                    val maxRadius = size.width / 2.0f

                    // Draw static coils (NFC induction loop look-alike)
                    for (i in 1..4) {
                        drawCircle(
                            color = Slate600.copy(alpha = 0.3f),
                            radius = maxRadius * (i * 0.2f),
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }

                    // Draw glowing broadcasting pulses when biometrically unlocked
                    if (isBiometricallyUnlocked) {
                        drawCircle(
                            color = CyanAccent.copy(alpha = pulseAlpha),
                            radius = maxRadius * pulseRadius,
                            style = Stroke(width = 3.dp.toPx())
                        )
                    }
                }

                // Central Fingerprint sensor or Active Shield Button
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = when {
                                    isScanningBiometric -> listOf(AmberAlert, Slate800)
                                    isBiometricallyUnlocked -> listOf(EmeraldGlow, Slate800)
                                    else -> listOf(CyanAccent.copy(alpha = 0.4f), Slate800)
                                }
                            )
                        )
                        .border(
                            2.dp,
                            when {
                                isScanningBiometric -> AmberAlert
                                isBiometricallyUnlocked -> EmeraldGlow
                                else -> CyanAccent
                            },
                            CircleShape
                        )
                        .clickable(enabled = !isScanningBiometric) {
                            if (isBiometricallyUnlocked) {
                                isBiometricallyUnlocked = false
                            } else {
                                coroutineScope.launch {
                                    isScanningBiometric = true
                                    delay(1500) // Pulse audit biometric scan
                                    isScanningBiometric = false
                                    isBiometricallyUnlocked = true
                                }
                            }
                        }
                        .testTag("biometric_verification_area"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(
                            imageVector = when {
                                isScanningBiometric -> Icons.Default.Refresh
                                isBiometricallyUnlocked -> Icons.Default.Check
                                else -> Icons.Default.Face
                            },
                            contentDescription = "Scan target",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                isScanningBiometric -> "SCANNING..."
                                isBiometricallyUnlocked -> "BROADCASTING"
                                else -> "KNOX BIOMETRIC"
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // --- Status notice ---
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Slate800),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isBiometricallyUnlocked) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = "Coil Status",
                        tint = if (isBiometricallyUnlocked) EmeraldGlow else AmberAlert,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isBiometricallyUnlocked) {
                            "Hardware private signature unlocked. Tapping this device against an ACR122U reader will authorize escrow contract funds release automatically."
                        } else {
                            "SECURE SECTOR LOCKED. Secure elements prevent transaction leakages. Hold finger on the Knox Biometric button to decrypt escrow tokens."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.LightGray),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
