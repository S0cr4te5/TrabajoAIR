package com.sendaurjc.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.sendaurjc.MainActivity

class AlertForegroundService : Service(), LocationListener {

    private lateinit var locationManager: LocationManager
    private var lastLocation: Location? = null
    private var stationaryStartAt: Long? = null
    private var preAlertTriggered = false

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(11, baseNotification("Tu ubicación está siendo compartida"))
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 0f, this)
        } catch (_: SecurityException) {
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_PREALERT -> {
                showPreAlertNotification()
                return START_STICKY
            }
            ACTION_OK -> {
                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(PREALERT_ID)
                sendBroadcast(Intent(ACTION_PREALERT_DEACTIVATED))
                return START_STICKY
            }
            ACTION_EMERGENCY -> {
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(PREALERT_ID)
                manager.notify(FINAL_ID,
                    NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle("[SIMULACIÓN] Alerta enviada")
                        .setContentText("Seguridad y Contacto URJC han sido notificados")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .build()
                )
                return START_STICKY
            }
        }
        return START_STICKY
    }

    override fun onLocationChanged(location: Location) {
        val previous = lastLocation
        val now = System.currentTimeMillis()
        if (previous != null) {
            val distance = previous.distanceTo(location)
            if (distance < 5f) {
                if (stationaryStartAt == null) stationaryStartAt = now
                if (!preAlertTriggered && now - (stationaryStartAt ?: now) >= 30_000) {
                    preAlertTriggered = true
                    launchPreAlert()
                }
            } else {
                stationaryStartAt = null
                preAlertTriggered = false
            }
        }
        lastLocation = location
    }

    private fun launchPreAlert() {
        showPreAlertNotification()

        handler.postDelayed({
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(FINAL_ID,
                NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_alert)
                    .setContentTitle("[SIMULACIÓN] Alerta enviada")
                    .setContentText("Seguridad y Contacto URJC han sido notificados")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()
            )
        }, 30000) // Cambiado a 30s para coincidir con PreAlertScreen
    }

    private fun showPreAlertNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val openIntent = PendingIntent.getActivity(
            this, 1,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val okIntent = PendingIntent.getService(
            this,
            2,
            Intent(this, AlertForegroundService::class.java).setAction(ACTION_OK),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val emergencyIntent = PendingIntent.getService(
            this,
            3,
            Intent(this, AlertForegroundService::class.java).setAction(ACTION_EMERGENCY),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Prealerta activada")
            .setContentText("Pulsa para desactivar si es una falsa alarma")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
            .setContentIntent(openIntent)
            .addAction(0, "Desactivar", okIntent)
            .addAction(0, "Emergencia", emergencyIntent)
            .build()

        manager.notify(PREALERT_ID, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(this)
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    private fun baseNotification(content: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("SendaURJC alerta")
            .setContentText(content)
            .setOngoing(true)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Senda Alerts", NotificationManager.IMPORTANCE_HIGH)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "senda_alert_channel"
        private const val PREALERT_ID = 1001
        private const val FINAL_ID = 1002
        const val ACTION_OK = "action_ok"
        const val ACTION_EMERGENCY = "action_emergency"
        const val ACTION_START_PREALERT = "action_start_prealert"
        const val ACTION_PREALERT_DEACTIVATED = "com.sendaurjc.PREALERT_DEACTIVATED"
    }
}
