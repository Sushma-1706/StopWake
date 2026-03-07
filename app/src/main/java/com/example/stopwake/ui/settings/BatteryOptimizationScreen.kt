package com.example.stopwake.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryOptimizationScreen(navController: NavController) {
    val context = LocalContext.current
    val manufacturer = Build.MANUFACTURER.lowercase()

    Scaffold(
        containerColor = Color(0xFF1A1A2E),
        topBar = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF16213E)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        "Battery Optimization",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE94560).copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFE94560),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Important!",
                                color = Color(0xFFE94560),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Disable battery optimization to ensure alarms work reliably",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Detected Device: ${Build.MANUFACTURER} ${Build.MODEL}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            // General Settings
            item {
                Text(
                    "General Settings",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                OptimizationCard(
                    title = "Disable Battery Optimization",
                    description = "Allow StopWake to run in background",
                    icon = Icons.Default.BatteryChargingFull,
                    onClick = {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                )
            }

            item {
                OptimizationCard(
                    title = "App Settings",
                    description = "Open app settings for permissions",
                    icon = Icons.Default.Settings,
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                )
            }

            // Manufacturer-Specific
            if (manufacturer in listOf("xiaomi", "redmi", "poco")) {
                item {
                    Text(
                        "Xiaomi/MIUI Specific",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    XiaomiGuideCard()
                }
            }

            if (manufacturer == "oneplus") {
                item {
                    Text(
                        "OnePlus Specific",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    OnePlusGuideCard()
                }
            }

            if (manufacturer in listOf("oppo", "realme")) {
                item {
                    Text(
                        "Oppo/Realme Specific",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    OppoGuideCard()
                }
            }

            if (manufacturer == "samsung") {
                item {
                    Text(
                        "Samsung Specific",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    SamsungGuideCard()
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun OptimizationCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3B3B58)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = Color(0xFF27AE60),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    description,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Go",
                tint = Color.Gray
            )
        }
    }
}

@Composable
fun XiaomiGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3B3B58)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Steps for MIUI:",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            GuideStep("1", "Go to Settings → Apps → Manage apps")
            GuideStep("2", "Find and tap StopWake")
            GuideStep("3", "Enable 'Autostart'")
            GuideStep("4", "Tap 'Battery saver' → No restrictions")
            GuideStep("5", "Enable 'Display pop-up windows while running in background'")
        }
    }
}

@Composable
fun OnePlusGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3B3B58)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Steps for OnePlus:",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            GuideStep("1", "Go to Settings → Battery → Battery optimization")
            GuideStep("2", "Tap 'All apps' → Find StopWake")
            GuideStep("3", "Select 'Don't optimize'")
            GuideStep("4", "Go to Settings → Apps → StopWake")
            GuideStep("5", "Enable 'Autolaunch'")
        }
    }
}

@Composable
fun OppoGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3B3B58)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Steps for Oppo/Realme:",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            GuideStep("1", "Go to Settings → Battery → Power saving mode")
            GuideStep("2", "Disable power saving mode")
            GuideStep("3", "Go to Settings → Privacy → Startup manager")
            GuideStep("4", "Enable StopWake")
            GuideStep("5", "Go to Settings → Battery → App battery management")
            GuideStep("6", "Find StopWake → Don't optimize")
        }
    }
}

@Composable
fun SamsungGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3B3B58)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Steps for Samsung:",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            GuideStep("1", "Go to Settings → Apps → StopWake")
            GuideStep("2", "Tap 'Battery' → Allow background activity")
            GuideStep("3", "Go to Settings → Device care → Battery")
            GuideStep("4", "Tap 'Background usage limits'")
            GuideStep("5", "Ensure StopWake is NOT in 'Sleeping apps' or 'Deep sleeping apps'")
        }
    }
}

@Composable
fun GuideStep(number: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = Color(0xFF27AE60).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number,
                color = Color(0xFF27AE60),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
