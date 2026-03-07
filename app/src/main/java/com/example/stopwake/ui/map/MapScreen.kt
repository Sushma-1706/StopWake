package com.example.stopwake.ui.map

import android.Manifest
import android.location.Geocoder
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.stopwake.data.local.entity.StopEntity
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.* 
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    navController: NavController
) {
    val state by viewModel.state.collectAsState()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(state.currentLocation ?: LatLng(28.6139, 77.2090), 15f)
    }
    var showDialog by remember { mutableStateOf(false) }
    var selectedPosition by remember { mutableStateOf<LatLng?>(null) }
    var searchText by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    val permissionState = rememberMultiplePermissionsState(permissions)

    LaunchedEffect(Unit) {
        if (!permissionState.allPermissionsGranted) {
            permissionState.launchMultiplePermissionRequest()
        }
    }

    LaunchedEffect(state.currentLocation) {
        state.currentLocation?.let {
            cameraPositionState.move(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(it, 15f))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = {
                selectedPosition = it
                showDialog = true
            },
            uiSettings = MapUiSettings(zoomControlsEnabled = false),
            properties = MapProperties(
                mapType = MapType.NORMAL,
                isMyLocationEnabled = permissionState.allPermissionsGranted
            )
        ) {
            // Current location marker
            state.currentLocation?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Current Location",
                    icon = com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(
                        com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_AZURE
                    )
                )
            }
            
            // Selected location marker
            selectedPosition?.let { position ->
                Marker(
                    state = MarkerState(position = position),
                    title = "Selected Location"
                )
            }
        }

        // Top Bar with Search and Back Button
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF16213E)
            ),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .background(Color(0xFF27AE60), CircleShape)
                        .size(40.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Search location...", color = Color.Gray) },
                    trailingIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                try {
                                    val geocoder = Geocoder(context, Locale.getDefault())
                                    val addresses = geocoder.getFromLocationName(searchText, 1)
                                    if (addresses?.isNotEmpty() == true) {
                                        val location = addresses[0]
                                        val latLng = LatLng(location.latitude, location.longitude)
                                        cameraPositionState.animate(
                                            com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(latLng, 15f),
                                            1000
                                        )
                                        selectedPosition = latLng
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF27AE60))
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF27AE60),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
            }
        }

        // Current Location FAB
        FloatingActionButton(
            onClick = {
                state.currentLocation?.let {
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(it, 15f),
                            1000
                        )
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 80.dp),
            containerColor = Color(0xFF27AE60)
        ) {
            Icon(Icons.Default.MyLocation, contentDescription = "My Location", tint = Color.White)
        }
    }

    if (showDialog && selectedPosition != null) {
        SaveAlertDialog(
            selectedPosition = selectedPosition!!,
            currentLocation = state.currentLocation,
            onDismiss = { 
                showDialog = false
                selectedPosition = null
            },
            onSave = { name, alertType, radius, contactNumber, alertMessage ->
                viewModel.addStop(
                    name = name,
                    position = selectedPosition!!,
                    alertType = alertType,
                    radius = radius,
                    contactNumber = contactNumber,
                    alertMessage = alertMessage
                )
                showDialog = false
                selectedPosition = null
                onNavigateBack()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveAlertDialog(
    selectedPosition: LatLng,
    currentLocation: LatLng?,
    onDismiss: () -> Unit,
    onSave: (name: String, alertType: String, radius: Float, contactNumber: String?, alertMessage: String?) -> Unit
) {
    var stopName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var notifyContact by remember { mutableStateOf(false) }
    var contactNumber by remember { mutableStateOf("") }
    var customMessage by remember { mutableStateOf("I've reached my destination!") }
    
    // Alert type options
    val alertTypes = listOf(
        "ARRIVAL" to "On Arrival",
        "ONE_STOP_BEFORE" to "1 Stop Before",
        "TWO_STOPS_BEFORE" to "2 Stops Before"
    )
    var selectedAlertType by remember { mutableStateOf(alertTypes[0]) }
    
    // Calculate distance and auto-select radius
    val distanceInMeters = remember(currentLocation, selectedPosition) {
        if (currentLocation != null) {
            val results = FloatArray(1)
            Location.distanceBetween(
                currentLocation.latitude,
                currentLocation.longitude,
                selectedPosition.latitude,
                selectedPosition.longitude,
                results
            )
            results[0]
        } else {
            500f
        }
    }
    
    // Smart radius selection based on distance
    val autoRadius = remember(distanceInMeters) {
        when {
            distanceInMeters < 500 -> 100f
            distanceInMeters < 1000 -> 200f
            distanceInMeters < 2000 -> 300f
            distanceInMeters < 5000 -> 500f
            distanceInMeters < 10000 -> 1000f
            else -> 2000f
        }
    }
    
    var selectedRadius by remember(autoRadius) { mutableStateOf(autoRadius) }
    
    val radiusOptions = listOf(100f, 200f, 300f, 500f, 1000f, 2000f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF16213E),
        title = { 
            Text(
                "Save Alert", 
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
                // Distance info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF27AE60).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF27AE60),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Distance: ${(distanceInMeters / 1000).roundToInt()} km away",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Alert Name
                OutlinedTextField(
                    value = stopName,
                    onValueChange = { stopName = it },
                    label = { Text("Alert Name", color = Color.Gray) },
                    placeholder = { Text("e.g., Central Station", color = Color.Gray) },
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
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = Color.White
                                )
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Contact Notification Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = notifyContact,
                        onCheckedChange = { notifyContact = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF27AE60),
                            uncheckedColor = Color.Gray
                        )
                    )
                    Text(
                        "Notify contact when alert triggers",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                
                if (notifyContact) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { contactNumber = it },
                        label = { Text("Phone Number", color = Color.Gray) },
                        placeholder = { Text("+1234567890", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF27AE60),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customMessage,
                        onValueChange = { customMessage = it },
                        label = { Text("Message", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF27AE60),
                            unfocusedBorderColor = Color.Gray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        maxLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onSave(
                        stopName.ifBlank { "Alert ${System.currentTimeMillis() % 1000}" },
                        selectedAlertType.first,
                        selectedRadius,
                        if (notifyContact && contactNumber.isNotBlank()) contactNumber else null,
                        if (notifyContact && customMessage.isNotBlank()) customMessage else null
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF27AE60)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Alert", fontWeight = FontWeight.Bold)
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
