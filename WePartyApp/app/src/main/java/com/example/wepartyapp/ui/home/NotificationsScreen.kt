package com.example.wepartyapp.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging // <-- Added FCM Import
import com.example.wepartyapp.ui.EventViewModel // <-- Added ViewModel Import
import com.example.wepartyapp.ui.PartyNotification // <-- Added Data Class Import

// The "Container" for notification data (Moved to EventViewModel as PartyNotification)

@Composable
fun NotificationsScreenUI(viewModel: EventViewModel, onBack: () -> Unit) { // <-- Added ViewModel Parameter
    val context = LocalContext.current

    // Observe the Real list of notifications from Firestore
    val realNotifications by viewModel.notificationsList.collectAsState()

    // 1. Check if the app already has permission (Required for Android 13+)
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Android 12 and below automatically grant this at install
            }
        )
    }

    // 2. State for the UI Toggle Switch
    var isPushEnabled by remember { mutableStateOf(hasNotificationPermission) }

    // 3. The Launcher that actually pops up the system permission dialog
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        isPushEnabled = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notifications Enabled!", Toast.LENGTH_SHORT).show()
            // Hooked up FCM Subscription for when they accept the permission popup
            FirebaseMessaging.getInstance().subscribeToTopic("party_alerts")
        } else {
            Toast.makeText(context, "Permission Denied. You can enable them in your phone settings.", Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .padding(24.dp)
    ) {
        // Header with Back Button
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Notifications",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB65C5C)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- SETTINGS SECTION ---
        Text(
            text = "Preferences",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Push Notifications", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                    Text(text = "Receive event alerts on your phone", fontSize = 12.sp, color = Color.Gray)
                }

                // The Toggle Switch
                Switch(
                    checked = isPushEnabled,
                    onCheckedChange = { isChecked ->
                        if (isChecked) {
                            // If they try to turn it on, check if we need to ask for permission
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                isPushEnabled = true
                                // Hooked up FCM Subscription for when they already have permission
                                FirebaseMessaging.getInstance().subscribeToTopic("party_alerts")
                            }
                        } else {
                            // If they turn it off
                            isPushEnabled = false
                            // Hooked up FCM Unsubscribe
                            FirebaseMessaging.getInstance().unsubscribeFromTopic("party_alerts")
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFFB65C5C)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- HISTORY SECTION ---
        Text(
            text = "Recent Alerts",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Check if the real list is empty and show a placeholder if it is
        if (realNotifications.isEmpty()) {
            Text(
                text = "No recent alerts.",
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else {
            // The List Display
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(realNotifications) { notification ->
                    NotificationCard(notification)
                }
            }
        }
    }
}

// The Reusable Card UI
@Composable
fun NotificationCard(notification: PartyNotification) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = notification.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(text = notification.time, fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = notification.message, fontSize = 14.sp, color = Color.DarkGray)
        }
    }
}