package com.example.wepartyapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent // <-- Added
import android.content.Context
import android.content.Intent // <-- Added
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.home.MainActivity // <-- Added

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // This fires when a push notification is received
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Grab the title and body from the Firebase message
        val title = remoteMessage.notification?.title ?: "New Party Alert!"
        val body = remoteMessage.notification?.body ?: "Tap to see what's new in WeParty."

        Log.d("FCM_MESSAGE", "Received: $title - $body")
        showNotification(title, body)
    }

    // This creates the actual pop-up on the phone screen
    private fun showNotification(title: String, message: String) {
        val channelId = "WePartyChannel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // --- NEW: Make it open the app when tapped! ---
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        // ----------------------------------------------

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
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent) // <-- Attaches the click action

        // Trigger the pop-up
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    // Fires when a new device token is generated
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "My device token is: $token") // Log it so we can test later!
    }
}