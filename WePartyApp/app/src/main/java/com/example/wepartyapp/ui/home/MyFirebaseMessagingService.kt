package com.example.wepartyapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.home.MainActivity

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // This fires when a push notification is received
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // --- Data vs Notification Payload ---
        // Firebase can send messages two ways. This safe fallback checks both
        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "New Party Alert!"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Tap to see what's new in WeParty."

        Log.d("FCM_MESSAGE", "Received: $title - $body")
        showNotification(title, body)
    }

    // This creates the actual pop-up on the phone screen
    private fun showNotification(title: String, message: String) {
        val channelId = "WePartyChannel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // --- Make it open the app directly to the Notifications Tab ---
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("TARGET_TAB", 8) // <-- Tells MainActivity to jump to the Inbox
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

    // --- Background Token Sync ---
    // Fires when a new device token is generated
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "My device token refreshed: $token")

        // Save the new token directly to the database so we never lose connection to this phone
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val tokenData = hashMapOf("fcmToken" to token)

            db.collection("users").document(currentUser.uid)
                .set(tokenData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("FCM", "Background token successfully synced for user: ${currentUser.uid}")
                }
        }
    }
}