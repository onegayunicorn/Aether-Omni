package com.example

import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AuditLogsPanel
import com.example.ui.DashboardScreen
import com.example.ui.SecureElementHcePanel
import com.example.ui.SystemConsolePanel
import com.example.ui.theme.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AetherViewModel
import com.example.viewmodel.NfcScanState

class MainActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    private val TAG = "AetherMainActivity"
    private var nfcAdapter: NfcAdapter? = null
    private var activeViewModel: AetherViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Probe system for high-frequency contactless NFC coils
        try {
            nfcAdapter = NfcAdapter.getDefaultAdapter(this)
            Log.i(TAG, "Hardware NFC Coils detected. Ready to bind.")
        } catch (e: Exception) {
            Log.w(TAG, "NFC standard unavailable on this hardware system.", e)
        }

        setContent {
            MyApplicationTheme {
                val vm: AetherViewModel = viewModel()
                activeViewModel = vm

                var currentTab by remember { mutableStateOf(0) }
                val nfcScanState by vm.nfcScanState.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = BottomNavBg, // Sophisticated bottom color #1E2022
                            tonalElevation = 8.dp,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
                                label = { Text("Registry") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF003354), // High contrast deep blue
                                    selectedTextColor = CyanAccent, // Sophisticated Ice Blue #D1E4FF
                                    indicatorColor = Color(0xFF3F4759), // Active pill container from HTML
                                    unselectedIconColor = OnSurfaceMuted.copy(alpha = 0.6f),
                                    unselectedTextColor = OnSurfaceMuted.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("nav_tab_registry")
                            )

                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = { Icon(Icons.Default.Lock, contentDescription = "Emulator") },
                                label = { Text("Emulator") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF003354),
                                    selectedTextColor = CyanAccent,
                                    indicatorColor = Color(0xFF3F4759),
                                    unselectedIconColor = OnSurfaceMuted.copy(alpha = 0.6f),
                                    unselectedTextColor = OnSurfaceMuted.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("nav_tab_emulator")
                            )

                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = { Icon(Icons.Default.List, contentDescription = "Compliances") },
                                label = { Text("Audit Logs") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF003354),
                                    selectedTextColor = CyanAccent,
                                    indicatorColor = Color(0xFF3F4759),
                                    unselectedIconColor = OnSurfaceMuted.copy(alpha = 0.6f),
                                    unselectedTextColor = OnSurfaceMuted.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("nav_tab_audits")
                            )

                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                icon = { Icon(Icons.Default.Settings, contentDescription = "System Console") },
                                label = { Text("System") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF003354),
                                    selectedTextColor = CyanAccent,
                                    indicatorColor = Color(0xFF3F4759),
                                    unselectedIconColor = OnSurfaceMuted.copy(alpha = 0.6f),
                                    unselectedTextColor = OnSurfaceMuted.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier.testTag("nav_tab_system")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Display correct pane
                        when (currentTab) {
                            0 -> DashboardScreen(
                                viewModel = vm,
                                onNavigateToHce = { contract ->
                                    vm.mountContractToNfcHce(contract)
                                    currentTab = 1 // transition directly to emulation tab!
                                }
                            )
                            1 -> SecureElementHcePanel(viewModel = vm)
                            2 -> AuditLogsPanel(viewModel = vm)
                            3 -> SystemConsolePanel(viewModel = vm)
                        }

                        // live hardware interaction modal overlay
                        HandshakeProgressModal(
                            scanState = nfcScanState,
                            onClose = { vm.resetScanState() }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Setup ReaderMode so that the phone scans external cards when active
        nfcAdapter?.let { adapter ->
            try {
                val flags = NfcAdapter.FLAG_READER_NFC_A or 
                            NfcAdapter.FLAG_READER_NFC_B or 
                            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
                adapter.enableReaderMode(this, this, flags, null)
                Log.i(TAG, "ReaderMode successfully bound. Awaiting hardware touch events...")
            } catch (e: Exception) {
                Log.e(TAG, "Failed enabling hardware scanner callbacks", e)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.let { adapter ->
            try {
                adapter.disableReaderMode(this)
                Log.i(TAG, "ReaderMode callbacks released.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed releasing hardware scanner callbacks", e)
            }
        }
    }

    // --- NfcAdapter.ReaderCallback implementation ---
    override fun onTagDiscovered(tag: android.nfc.Tag?) {
        if (tag == null) return
        
        // Grab hardware UID in Hex
        val uid = tag.id?.joinToString("") { "%02X".format(it) } ?: "TAG-00000000"
        Log.i(TAG, "Physical Tap Detected! NFC UID: $uid")

        activeViewModel?.let { vm ->
            // Use static card data segment. Matches seed hash for ESC-2026-101.
            vm.scanPhysicalTag(
                tagUid = uid,
                contractHashPayload = "ESC-2026-101-FRUIT-SEED-QLD"
            )
        }
    }
}

@Composable
fun HandshakeProgressModal(
    scanState: NfcScanState,
    onClose: () -> Unit
) {
    if (scanState is NfcScanState.Idle) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .border(2.dp, CyanAccent, RoundedCornerShape(16.dp))
                .testTag("scan_progress_modal"),
            colors = CardDefaults.cardColors(containerColor = Slate800),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                when (scanState) {
                    is NfcScanState.Scanning -> {
                        CircularProgressIndicator(color = CyanAccent, modifier = Modifier.size(48.dp))
                        Text(
                            text = "NFC COILS ENERGIZED",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = CyanGlow,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = "Contactless tag discovered. Executing IsoDep block validation. Maintain connection.",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }

                    is NfcScanState.Verifying -> {
                        CircularProgressIndicator(color = CyanAccent, modifier = Modifier.size(48.dp))
                        Text(
                            text = "SERVER HANDSHAKE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = CyanGlow,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = "Validating block metadata for transaction Contract ID: ${scanState.contractId}.\nAttaching client hardware signature from Knox Keystore...",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }

                    is NfcScanState.Success -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = EmeraldGlow,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "ESCROW AUTHORIZED",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = EmeraldGlow,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = "Validation successfully committed!\n\nContract: ${scanState.contractId}\nAudit reference: ${scanState.auditId.take(16)}...",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                        
                        if (scanState.isOffline) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color.Gray.copy(alpha = 0.2f),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "OFFLINE COMPLIANCE MODE ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Button(
                            onClick = onClose,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGlow),
                            modifier = Modifier.fillMaxWidth().testTag("modal_dismiss_success")
                        ) {
                            Text("Confirm Transaction", color = Slate900)
                        }
                    }

                    is NfcScanState.Error -> {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Error",
                            tint = CrimsonError,
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "VALIDATION REJECTED",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = CrimsonError,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = "Fiduciary handshake failed: ${scanState.reason}",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = onClose,
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonError),
                            modifier = Modifier.fillMaxWidth().testTag("modal_dismiss_error")
                        ) {
                            Text("Acknowledge Defect", color = Color.White)
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text("Hello $name!", modifier = modifier)
}
