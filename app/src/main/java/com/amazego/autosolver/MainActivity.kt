package com.amazego.autosolver

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amazego.autosolver.service.AutoTapAccessibilityService
import com.amazego.autosolver.service.FloatingOverlayService
import com.amazego.autosolver.ui.theme.AmazeGoTheme

/**
 * Amaze GO Auto Solver - Main Dashboard & Permission Manager
 * Official Developer & Creator: MD. SABBIR RAHMAN
 * Project Owner: MD. SABBIR RAHMAN
 * Attribution: Developed by MD. SABBIR RAHMAN
 * Handles runtime permissions for Floating Overlay, MediaProjection, and Accessibility.
 */
class MainActivity : ComponentActivity() {

    private lateinit var mediaProjectionManager: MediaProjectionManager

    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            FloatingOverlayService.setMediaProjectionIntent(result.resultCode, result.data!!)
            startFloatingOverlayService()
            Toast.makeText(this, "Screen Capture Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Screen Capture Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        setContent {
            AmazeGoTheme {
                MainScreen(
                    onStartService = { checkPermissionsAndStart() },
                    onRequestOverlayPermission = { requestOverlayPermission() },
                    onRequestAccessibility = { requestAccessibilityPermission() }
                )
            }
        }
    }

    private fun checkPermissionsAndStart() {
        if (!Settings.canDrawOverlays(this)) {
            requestOverlayPermission()
            return
        }
        if (!AutoTapAccessibilityService.isServiceRunning) {
            Toast.makeText(this, "Please enable Accessibility Service for auto-tap", Toast.LENGTH_LONG).show()
            requestAccessibilityPermission()
            return
        }
        // Launch Screen Capture request
        val captureIntent = mediaProjectionManager.createScreenCaptureIntent()
        screenCaptureLauncher.launch(captureIntent)
    }

    private fun startFloatingOverlayService() {
        val intent = Intent(this, FloatingOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        finish() // Minimize to background to let user open Amaze GO!
    }

    private fun requestOverlayPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun requestAccessibilityPermission() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onStartService: () -> Unit,
    onRequestOverlayPermission: () -> Unit,
    onRequestAccessibility: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Amaze GO! Auto Solver", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Universal Arrow Puzzle Assistant", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Scans Amaze GO! puzzles in real time, computes unobstructed arrow clearance sequences using dependency graphs, and automatically taps solutions.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Permission Status Cards
            Text("Required Permissions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

            PermissionCard(
                title = "Floating Overlay (SYSTEM_ALERT_WINDOW)",
                description = "Enables draggable solver panel above the game.",
                icon = Icons.Default.Layers,
                onConfigure = onRequestOverlayPermission
            )

            PermissionCard(
                title = "Accessibility Service",
                description = "Simulates precise screen taps on arrows.",
                icon = Icons.Default.TouchApp,
                onConfigure = onRequestAccessibility
            )

            PermissionCard(
                title = "Screen Capture (MediaProjection)",
                description = "Captures puzzle frames for OpenCV arrow detection.",
                icon = Icons.Default.Screenshot,
                onConfigure = onStartService
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Launch Button
            Button(
                onClick = onStartService,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Floating Assistant", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onConfigure: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            TextButton(onClick = onConfigure) {
                Text("Enable")
            }
        }
    }
}
