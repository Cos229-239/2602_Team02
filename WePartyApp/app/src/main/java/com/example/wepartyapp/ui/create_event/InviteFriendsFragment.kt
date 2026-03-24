package com.example.wepartyapp.ui.create_event

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.ui.home.MainActivity

// --- Mode-Aware Invite Friends Screen ---
@Composable
fun InviteFriendsScreenUI(
    navController: NavController? = null,
    viewItemModel: EventViewModel,
    existingEventId: String? = null // <-- Null for new events, ID for existing ones
) {

    // --- Updated for FlowLinks Deep Linking ---
    val uniqueEventId = existingEventId ?: viewItemModel.eventId ?: "temp-id"
    var urlLink = "https://wepartyapp-8a3a7-flowlinks.web.app/$uniqueEventId"

    val context = LocalContext.current

    // --- New: State to hold friends and selections ---
    val friendsList by viewItemModel.friendsList.collectAsState()
    var selectedFriendUids by remember { mutableStateOf(setOf<String>()) }

    // Fetch friends when the screen opens
    LaunchedEffect(Unit) {
        viewItemModel.fetchFriends()
    }

    // --- Form Validation (Only required if creating new event) ---
    val isFormComplete = existingEventId != null || (
            viewItemModel.eventName.isNotBlank() &&
                    viewItemModel.eventDate.isNotBlank() &&
                    viewItemModel.eventTime.isNotBlank() &&
                    viewItemModel.eventAddress.isNotBlank()
            )

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
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (navController != null) navController.popBackStack()
                        else (context as? Activity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            Modifier.size(35.dp)
                        )
                    }
                    Text(
                        text = if (existingEventId != null) "Edit Event" else "Add Items",
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FamilyRestroom,
                        contentDescription = null,
                        Modifier.size(65.dp),
                        tint = Color(0xFFBF6363)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (existingEventId != null) "Invite More" else "Invite Friends",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 30.sp
                    )
                }
                Spacer(modifier = Modifier.height(30.dp))

                // --- In-App Friends Selection ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Invite In-App Friends",
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    if (friendsList.isEmpty()) {
                        Text("You haven't added any friends yet.", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(friendsList, key = { it.uid }) { friend ->
                                val isSelected = selectedFriendUids.contains(friend.uid)

                                Surface(
                                    modifier = Modifier.clickable {
                                        selectedFriendUids = if (isSelected) {
                                            selectedFriendUids - friend.uid
                                        } else {
                                            selectedFriendUids + friend.uid
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) Color(0xFFBF6363) else Color.White,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBF6363))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp).padding(end = 4.dp)
                                            )
                                        }
                                        Text(
                                            text = friend.name,
                                            color = if (isSelected) Color.White else Color(0xFFBF6363),
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Sharing Link Section ---
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
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        colors = ButtonDefaults.buttonColors(Color(0xFFFA8989))
                        ) {
                        Text("Share Item Link")
                    }
                }
            }

            // --- Complete / Update Button ---
            Button(
                onClick = {
                    if (isFormComplete) {
                        if (existingEventId != null) {
                            // MODE: Update existing guest list
                            viewItemModel.inviteMoreGuests(existingEventId, selectedFriendUids.toList())
                            Toast.makeText(context, "Invites sent!", Toast.LENGTH_SHORT).show()
                            (context as? Activity)?.finish()
                        } else {
                            // MODE: Creating new event
                            viewItemModel.eventInvitedGuests = selectedFriendUids.toList()
                            viewItemModel.saveEventData()
                            context.startActivity(Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            })
                            (context as? Activity)?.finish()
                        }
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFormComplete) Color(0xFFFA8989) else Color.LightGray
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text(
                    text = if (existingEventId != null) "Send Invites" else "Complete Event",
                    color = if (isFormComplete) Color.Black else Color.DarkGray
                )
            }
        }
    }
}