package com.example.wepartyapp.ui.event_dashboard

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MapContent() {
    // Context to fire off Intents (actions that leave our app)
    val context = LocalContext.current
    val address = "123 Party Ln, Winter Park, FL 32789"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, Color.Black)
            // Simulating a map background color here.
            // Eventually will replace this with a real MapView or Image
            .background(Color(0xFFE0E0E0))
    ) {
        // --- LOCATION CARD ---
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth()
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
            colors = CardDefaults.cardColors(containerColor = PinkBackground)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Party Location", fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))
                Spacer(modifier = Modifier.height(4.dp))
                Text(address, modifier = Modifier.align(Alignment.Start))

                Spacer(modifier = Modifier.height(16.dp))

                // Get Directions Button
                Button(
                    onClick = {
                        // This creates an "Intent" to open an external map app using geo coordinates or a query
                        val uri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        context.startActivity(mapIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ButtonPink),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                ) {
                    Text("Get Directions", color = Color.Black)
                }
            }
        }
    }
}

