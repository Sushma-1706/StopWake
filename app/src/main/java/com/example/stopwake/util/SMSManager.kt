package com.example.stopwake.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SMSNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun sendAlertSMS(
        phoneNumber: String,
        message: String,
        locationName: String,
        latitude: Double,
        longitude: Double
    ): Boolean {
        return try {
            // Check if SMS permission is granted
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SEND_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }

            val smsManager = SmsManager.getDefault()
            val locationLink = "https://maps.google.com/?q=$latitude,$longitude"
            val fullMessage = "$message\n\nLocation: $locationName\n$locationLink"

            // If message is too long, split it
            if (fullMessage.length > 160) {
                val parts = smsManager.divideMessage(fullMessage)
                smsManager.sendMultipartTextMessage(
                    phoneNumber,
                    null,
                    parts,
                    null,
                    null
                )
            } else {
                smsManager.sendTextMessage(
                    phoneNumber,
                    null,
                    fullMessage,
                    null,
                    null
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun hasSMSPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
