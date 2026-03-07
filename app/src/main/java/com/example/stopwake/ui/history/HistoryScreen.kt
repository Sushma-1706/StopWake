package com.example.stopwake.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.stopwake.data.local.entity.StopEntity
import com.example.stopwake.ui.home.HomeViewModel
import com.example.stopwake.ui.home.StopWakeBottomNavigation
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HomeViewModel = hiltViewModel(), navController: NavController) {
    val stops by viewModel.stops.collectAsState()
    var showReuseDialog by remember { mutableStateOf(false) }
    var selectedStop by remember { mutableStateOf<StopEntity?>(null) }
    
    // Group stops by date (for now, using creation timestamp)
    val groupedStops = stops
        .sortedByDescending { it.createdAt }
        .groupBy { stop ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = stop.createdAt
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(calendar.time)
        }

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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "History",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (stops.isNotEmpty()) {
                        Text(
                            "${stops.size} trips",
                            color = Color(0xFF27AE60),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        bottomBar = { 
            StopWakeBottomNavigation(navController = navController) 
        }
    ) { padding ->
        if (stops.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "No History",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No history yet",
                        color = Color.Gray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your completed trips will appear here",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                
                groupedStops.forEach { (date, stopsForDate) ->
                    item {
                        Text(
                            date,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                        )
                    }
                    
                    items(stopsForDate) { stop ->
                        HistoryTimelineItem(
                            stop = stop,
                            onReuse = {
                                selectedStop = stop
                                showReuseDialog = true
                            }
                        )
                    }
                }
                
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }
    }

    // Re-use Stop Dialog
    if (showReuseDialog && selectedStop != null) {
        AlertDialog(
            onDismissRequest = { showReuseDialog = false },
            containerColor = Color(0xFF16213E),
            title = {
                Text("Re-use This Location?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Create a new alert for:",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        selectedStop!!.name,
                        color = Color(0xFF27AE60),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Radius: ${selectedStop!!.radiusMeters.toInt()}m",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                    Text(
                        "Alert Type: ${selectedStop!!.getAlertTypeEnum().name.replace("_", " ")}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Create new alert with same parameters
                        viewModel.updateStop(selectedStop!!.copy(
                            id = 0, // New ID will be auto-generated
                            isActive = true,
                            createdAt = System.currentTimeMillis()
                        ))
                        showReuseDialog = false
                        selectedStop = null
                        // Navigate to alerts screen
                        navController.navigate("alerts")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF27AE60)
                    )
                ) {
                    Text("Create Alert")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showReuseDialog = false
                        selectedStop = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun HistoryTimelineItem(stop: StopEntity, onReuse: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Timeline indicator
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(
                        color = if (stop.isActive) Color(0xFF27AE60) else Color.Gray,
                        shape = CircleShape
                    )
            )
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(80.dp)
                    .background(Color.Gray.copy(alpha = 0.3f))
            )
        }

        // Content Card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 8.dp),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stop.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                formatTimestamp(stop.createdAt),
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (stop.isActive) 
                            Color(0xFF27AE60).copy(alpha = 0.2f) 
                        else 
                            Color.Gray.copy(alpha = 0.2f)
                    ) {
                        Text(
                            if (stop.isActive) "Active" else "Completed",
                            color = if (stop.isActive) Color(0xFF27AE60) else Color.Gray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DetailChip(
                        icon = Icons.Default.Circle,
                        text = "${stop.radiusMeters.toInt()}m"
                    )
                    DetailChip(
                        icon = Icons.Default.Notifications,
                        text = stop.getAlertTypeEnum().name.replace("_", " ")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Re-use Button
                OutlinedButton(
                    onClick = onReuse,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF27AE60)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Replay,
                        contentDescription = "Re-use",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Re-use This Location", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun DetailChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF27AE60),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

fun formatTimestamp(timestamp: Long): String {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    val now = Calendar.getInstance()
    
    return when {
        calendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) &&
        calendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) -> {
            SimpleDateFormat("h:mm a", Locale.getDefault()).format(calendar.time)
        }
        else -> {
            SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(calendar.time)
        }
    }
}
