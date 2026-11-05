package com.example.skysyncsa

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/** Receives real-time weather alerts sent through Firebase Cloud Messaging (FCM). */
class SkySyncMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data["title"] ?: "SkySync weather alert"
        val body = message.notification?.body ?: message.data["body"] ?: "A weather update is available."
        showAlert(title, body)
    }

    private fun saveToken(token: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        FirebaseFirestore.getInstance().collection("users").document(user.uid)
            .set(mapOf("fcmToken" to token, "notificationTokenUpdatedAt" to System.currentTimeMillis()), SetOptions.merge())
    }

    private fun showAlert(title: String, body: String) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) manager.createNotificationChannel(
            NotificationChannel("weather_alerts", "SkySync weather alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Real-time weather warnings and forecast updates"
            }
        )
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(this, "weather_alerts") else Notification.Builder(this)
        manager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .build())
    }
}
