package com.example.stopwake.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var ringtone: Ringtone? = null
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun startAlarm() {
        // Start Continuous Vibration
        if (vibrator.hasVibrator()) {
            val pattern = longArrayOf(0, 500, 200, 500, 200) // More aggressive pattern
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(pattern, 0) // 0 = repeat indefinitely
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, 0)
            }
        }

        // Start Sound - Override Silent Mode
        try {
            val alarmSound: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            
            ringtone = RingtoneManager.getRingtone(context, alarmSound)
            ringtone?.let {
                // Use USAGE_ALARM to override silent mode
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    it.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED)
                        .build()
                }
                
                // Set to max volume for alarm stream
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    it.volume = 1.0f
                }
                
                it.isLooping = true
                it.play()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopAlarm() {
        vibrator.cancel()
        ringtone?.stop()
        ringtone = null
    }
}

