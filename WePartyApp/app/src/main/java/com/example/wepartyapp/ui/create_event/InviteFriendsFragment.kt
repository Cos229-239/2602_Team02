package com.example.wepartyapp.ui.create_event

import android.app.Activity // <-- Added
import android.content.Intent // <-- Added
import android.widget.Toast // <-- Added for dynamic error popup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // <-- Added
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.ui.home.MainActivity // <-- Added

// Invite Friends Screen
@Composable
fun InviteFriendsScreenUI(navController: NavController, viewItemModel: EventViewModel) {

    // --- Updated for FlowLinks Deep Linking ---
    val uniqueEventId = viewItemModel.eventId ?: "temp-id"
    var urlLink = "https://wepartyapp-8a3a7-flowlinks.web.app/$uniqueEventId"

    val context = LocalContext.current // <-- Grab context for the Intent

    // --- Form Validation ---
    val isFormComplete = viewItemModel.eventName.isNotBlank() &&
            viewItemModel.eventDate.isNotBlank() &&
            viewItemModel.eventTime.isNotBlank() &&
            viewItemModel.eventAddress.isNotBlank()

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFC96B6B),
                                Color(0xFFB65C5C),
                                Color(0xFF8E3F3F)
                            )
                        )
                    )
                    .border(3.dp, color = Color.Black)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.Center)
                )
            }
        }
    ) { innerpadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFE9EA))
                .padding(innerpadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                //Spacer(modifier = Modifier.height(40.dp)) // <-- Pushes the whole screen down
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) { //back to add items btn
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            Modifier.size(35.dp)
                        )
                    }
                    Text(
                        text = "Add Items",
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(                                                           //pg icon
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        Modifier.size(60.dp),
                        tint = Color(0xFFBF6363)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(                                                           //pg title
                        text = "Invite Friends",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 30.sp
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Sharing Link",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        //display url only
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = urlLink,
                            onValueChange = { },
                            readOnly = true
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Check out this new event!: $urlLink")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(Color(0xFFFA8989))
                        ) {
                        Text("Share Item Link")
                    }
                }
            }

            // --- Complete Button ---
            Button(
                onClick = {
                    if (isFormComplete) {
                        viewItemModel.saveEventData()
                        // Explicitly return to Main Activity and kill this one
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    } else {
                        // Figure out exactly what is missing to tell the user
                        val missingFields = mutableListOf<String>()
                        if (viewItemModel.eventName.isBlank()) missingFields.add("Name")
                        if (viewItemModel.eventDate.isBlank()) missingFields.add("Date")
                        if (viewItemModel.eventTime.isBlank()) missingFields.add("Time")
                        if (viewItemModel.eventAddress.isBlank()) missingFields.add("Location")

                        val errorMessage =
                            "Please go back and fill out: ${missingFields.joinToString(", ")}"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                },
                // We remove 'enabled = isFormComplete' so the button is always clickable,
                // allowing our Toast to actually fire. Instead, we manually swap the colors below to fake the disabled look!
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormComplete) Color(0xFFFA8989) else Color.LightGray
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Complete Event",
                    color = if (isFormComplete) Color.Black else Color.DarkGray
                )
            }
        }
    }
}