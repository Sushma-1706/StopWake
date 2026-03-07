package com.example.stopwake.ui.alerts

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.stopwake.data.local.entity.StopEntity
import com.example.stopwake.ui.home.HomeViewModel
import com.example.stopwake.ui.home.StopWakeBottomNavigation
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAlertsScreen(viewModel: HomeViewModel = hiltViewModel(), navController: NavController) {
    val stops by viewModel.stops.collectAsState()
    var selectedStop by remember { mutableStateOf<StopEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var stopToDelete by remember { mutableStateOf<StopEntity?>(null) }
    var sortBy by remember { mutableStateOf("name") } // name, date
    var showSortMenu by remember { mutableStateOf(false) }
    
     val sortedStops = remember(stops, sortBy) {
        when (sortBy) {
            "date" -> stops.sortedByDescending { it.createdAt }
            else -> stops.sortedBy { it.name.lowercase() }
        }
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
                        "My Alerts",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (sortedStops.isNotEmpty()) {
                            Text(
                                 "${sortedStops.size}",
                                color = Color(0xFF27AE60),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = Color(0xFF27AE60)
                            )
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(Color(0xFF16213E))
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sort by Name", color = Color.White) },
                                onClick = {
                                    sortBy = "name"
                                    showSortMenu = false
                                }
                            )
                            DropdownMenuItem(
                                 text = { Text("Sort by Date", color = Color.White) },
                                onClick = {
                                    sortBy = "date"
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = { 
            StopWakeBottomNavigation(navController = navController) 
        }
    ) { padding ->
       if (sortedStops.isEmpty()) {
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
                        Icons.Default.NotificationsOff,
                        contentDescription = "No Alerts",
                        tint = Color.Gray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No alerts yet",
                        color = Color.Gray,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Create one from the home screen",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(sortedStops, key = { it.id }) { stop ->
                    SwipeableAlertCard(
                        stop = stop,
                        onEdit = { selectedStop = stop },
                        onDelete = {
                            stopToDelete = stop
                            showDeleteDialog = true
                        },
                        onToggle = { viewModel.toggleStop(stop) }
                    )
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }
    }

    // Edit Dialog
    selectedStop?.let { stop ->
        EditAlertDialog(
            stop = stop,
            onDismiss = { selectedStop = null },
            onSave = { updatedStop ->
                viewModel.updateStop(updatedStop)
                selectedStop = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && stopToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF16213E),
            title = {
                Text("Delete Alert?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to delete '${stopToDelete!!.name}'?",
                    color = Color.White
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStop(stopToDelete!!)
                        showDeleteDialog = false
                        stopToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE94560)
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                        stopToDelete = null
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
fun SwipeableAlertCard(
    stop: StopEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val deleteAction = SwipeAction(
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        },
        background = Color(0xFFE94560),
        onSwipe = onDelete
    )

    val editAction = SwipeAction(
        icon = {
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit",
                tint = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        },
        background = Color(0xFF27AE60),
        onSwipe = onEdit
    )

    SwipeableActionsBox(
        startActions = listOf(editAction),
        endActions = listOf(deleteAction),
        swipeThreshold = 100.dp
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
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
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Location Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (stop.isActive) Color(0xFF27AE60).copy(alpha = 0.2f)
                                else Color.Gray.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = if (stop.isActive) Color(0xFF27AE60) else Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Stop Info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            stop.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Circle,
                                contentDescription = null,
                                tint = if (stop.isActive) Color(0xFF27AE60) else Color.Gray,
                                modifier = Modifier.size(8.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (stop.isActive) "Active" else "Inactive",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Toggle Switch
                    Switch(
                        checked = stop.isActive,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF27AE60),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray
                        )
                    )

                    // Expand Icon
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand",
                            tint = Color.Gray
                        )
                    }
                }

                // Expanded Content
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                    ) {
                        Divider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        // Alert Type
                        DetailRow(
                            icon = Icons.Default.Notifications,
                            label = "Alert Type",
                            value = stop.getAlertTypeEnum().name.replace("_", " ")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Radius
                        DetailRow(
                            icon = Icons.Default.Circle,
                            label = "Radius",
                            value = "${stop.radiusMeters.toInt()}m (Effective: ${stop.getEffectiveRadius().toInt()}m)"
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Location
                        DetailRow(
                            icon = Icons.Default.LocationOn,
                            label = "Coordinates",
                            value = "${String.format("%.4f", stop.latitude)}, ${String.format("%.4f", stop.longitude)}"
                        )

                        if (stop.contactNumber != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow(
                                icon = Icons.Default.Phone,
                                label = "Contact",
                                value = stop.contactNumber!!
                            )
                        }

                        if (stop.alertMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            DetailRow(
                                icon = Icons.Default.Message,
                                label = "Message",
                                value = stop.alertMessage!!
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onEdit,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF27AE60)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit")
                            }

                            OutlinedButton(
                                onClick = onDelete,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFE94560)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color(0xFF27AE60),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                label,
                color = Color.Gray,
                fontSize = 12.sp
            )
            Text(
                value,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAlertDialog(stop: StopEntity, onDismiss: () -> Unit, onSave: (StopEntity) -> Unit) {
    var stopName by remember { mutableStateOf(stop.name) }
    var expanded by remember { mutableStateOf(false) }
    
    val alertTypes = listOf(
        "ARRIVAL" to "On Arrival",
        "ONE_STOP_BEFORE" to "1 Stop Before",
        "TWO_STOPS_BEFORE" to "2 Stops Before"
    )
    var selectedAlertType by remember { 
        mutableStateOf(alertTypes.find { it.first == stop.alertType } ?: alertTypes[0]) 
    }
    
    val radiusOptions = listOf(100f, 200f, 300f, 500f, 1000f, 2000f)
    var selectedRadius by remember { mutableStateOf(stop.radiusMeters) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF16213E),
        title = {
            Text(
                "Edit Alert",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Alert Name
                OutlinedTextField(
                    value = stopName,
                    onValueChange = { stopName = it },
                    label = { Text("Alert Name", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF27AE60),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Alert Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedAlertType.second,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Alert Type", color = Color.Gray) },
                        trailingIcon = {
                            Icon(
                                if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color(0xFF27AE60)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF27AE60),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFF1A1A2E))
                    ) {
                        alertTypes.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.second, color = Color.White) },
                                onClick = {
                                    selectedAlertType = option
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Radius Selector
                Text(
                    "Alert Radius: ${selectedRadius.toInt()}m",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    radiusOptions.take(3).forEach { radius ->
                        FilterChip(
                            selected = selectedRadius == radius,
                            onClick = { selectedRadius = radius },
                            label = {
                                Text(
                                    "${radius.toInt()}m",
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF27AE60),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF3B3B58),
                                labelColor = Color.Gray
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    radiusOptions.drop(3).forEach { radius ->
                        FilterChip(
                            selected = selectedRadius == radius,
                            onClick = { selectedRadius = radius },
                            label = {
                                Text(
                                    if (radius >= 1000) "${(radius / 1000).toInt()}km" else "${radius.toInt()}m",
                                    fontSize = 12.sp
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF27AE60),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF3B3B58),
                                labelColor = Color.Gray
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        stop.copy(
                            name = stopName,
                            alertType = selectedAlertType.first,
                            radiusMeters = selectedRadius
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF27AE60)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
