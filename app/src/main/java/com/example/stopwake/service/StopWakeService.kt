package com.example.stopwake.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.stopwake.R
import com.example.stopwake.data.local.entity.HistoryEntity
import com.example.stopwake.data.local.entity.StopEntity
import com.example.stopwake.domain.location.LocationClient
import com.example.stopwake.domain.repository.HistoryRepository
import com.example.stopwake.domain.repository.StopRepository
import com.example.stopwake.ui.alarm.AlarmActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StopWakeService : Service() {

    @Inject
    lateinit var locationClient: LocationClient

    @Inject
    lateinit var repository: StopRepository

    @Inject
    lateinit var historyRepository: HistoryRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var notificationManager: NotificationManager
    private val triggeredStopIds = mutableSetOf<Long>()
    private val lastDistancesByStopId = mutableMapOf<Long, Float>()
    private var activeStops: List<StopEntity> = emptyList()


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return START_STICKY
    }

    private fun start() {
        val notification = createNotification("Initializing tracking...")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(1, notification)
        }
        
        startLocationTracking()
    }



    private fun startLocationTracking() {
        // 1. Observe active stops continuously in a separate job
        repository.getActiveStops()
            .onEach { stops ->
                activeStops = stops
                val activeStopIds = stops.map { it.id }.toSet()
                triggeredStopIds.retainAll(activeStopIds)
                lastDistancesByStopId.keys.retainAll(activeStopIds)
                if (stops.isEmpty()) {
                    updateNotification("Tracking stopped. No active stops.")
                    stop()
                } else {
                    updateNotification("Tracking active. Monitoring ${stops.size} stops.")
                }
            }
            .launchIn(serviceScope)

        // 2. Observe location and check against the latest activeStops
        locationClient.getLocationUpdates(5000L)
            .catch { e -> e.printStackTrace() }
            .onEach { location -> checkStops(location) }
            .launchIn(serviceScope)
    }

    private fun checkStops(location: Location) {
        val stops = activeStops
        stops.forEach { stop ->
            if (triggeredStopIds.contains(stop.id)) {
                return@forEach
            }

            val results = FloatArray(1)
            Location.distanceBetween(
                location.latitude,
                location.longitude,
                stop.latitude,
                stop.longitude,
                results
            )
            val distanceInMeters = results[0]
            val effectiveRadius = stop.getEffectiveRadius()

            if (shouldTrigger(stop, distanceInMeters, effectiveRadius)) {
                triggeredStopIds.add(stop.id)
                triggerAlarm(stop)
            }
        }

        if (stops.isNotEmpty()) {
            val closest = stops.minByOrNull { stop ->
                val results = FloatArray(1)
                Location.distanceBetween(
                    location.latitude,
                    location.longitude,
                    stop.latitude,
                    stop.longitude,
                    results
                )
                results[0]
            }
            closest?.let {
                val results = FloatArray(1)
                Location.distanceBetween(
                    location.latitude,
                    location.longitude,
                    it.latitude,
                    it.longitude,
                    results
                )
                val alertTypeDisplay = it.getAlertTypeEnum().name.replace("_", " ")
                updateNotification("Nearest: ${it.name} (${results[0].toInt()}m) - $alertTypeDisplay")
            }
        }
    }

    private fun shouldTrigger(stop: StopEntity, distanceInMeters: Float, effectiveRadius: Float): Boolean {
        val previousDistance = lastDistancesByStopId[stop.id]
        lastDistancesByStopId[stop.id] = distanceInMeters

        if (distanceInMeters > effectiveRadius) {
            return false
        }

        return previousDistance == null || previousDistance > effectiveRadius
    }

    private fun triggerAlarm(stop: StopEntity) {
        val intent = Intent(applicationContext, AlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("STOP_ID", stop.id)
            putExtra("STOP_NAME", stop.name)
        }
        startActivity(intent)
        
        serviceScope.launch {
            repository.updateStop(
                stop.copy(
                    isActive = false,
                    triggeredAt = System.currentTimeMillis()
                )
            )
            historyRepository.insertHistory(
                HistoryEntity(
                    stopName = stop.name,
                    latitude = stop.latitude,
                    longitude = stop.longitude,
                    alertType = stop.alertType,
                    triggeredAt = System.currentTimeMillis(),
                    userId = stop.userId
                )
            )
        }
        stop.contactNumber?.let { number ->
            if (number.isNotBlank()) {
                sendSMS(number, stop.alertMessage ?: "I have reached my stop: ${stop.name}")
            }
        }
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                applicationContext.getSystemService(android.telephony.SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                android.telephony.SmsManager.getDefault()
            }
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            updateNotification("Alarm Triggered! SMS sent to $phoneNumber")
        } catch (e: Exception) {
            e.printStackTrace()
            updateNotification("Alarm Triggered! Failed to send SMS.")
        }
    }

    private fun stop() {
        triggeredStopIds.clear()
        lastDistancesByStopId.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    
    // UI Helpers
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location",
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(content: String): Notification {
        return NotificationCompat.Builder(this, "location")
            .setContentTitle("StopWake Tracking")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher) // Use launcher icon for now
            .setOngoing(true)
            .build()
    }
    
    private fun updateNotification(content: String) {
        notificationManager.notify(1, createNotification(content))
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}
