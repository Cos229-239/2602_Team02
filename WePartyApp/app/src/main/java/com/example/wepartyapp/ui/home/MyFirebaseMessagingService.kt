package com.example.wepartyapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.wepartyapp.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // This fires when a push notification is received
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Grab the title and body from the Firebase message
        remoteMessage.notification?.let {
            showNotification(it.title, it.body)
        }
    }

    // This creates the actual pop-up on the phone screen
    private fun showNotification(title: String?, message: String?) {
        val channelId = "WePartyChannel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0 and up requires a "Notification Channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "WeParty Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.app_logo2)
            .setContentTitle(title ?: "New Party Alert!")
            .setContentText(message ?: "Tap to see what's new in WeParty.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        // Trigger the pop-up
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    // Fires when a new device token is generated
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Save this token to your Firebase Realtime Database or Firestore later
    }
}