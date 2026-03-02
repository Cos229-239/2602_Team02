package com.example.wepartyapp.ui.event_dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.ui.PartyEvent
import com.example.wepartyapp.ui.chat.ChatRoomActivity
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class EventDashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: EventViewModel = viewModel()
            EventInboxScreen(viewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventInboxScreen(viewModel: EventViewModel) {
    val events by viewModel.events.observeAsState(emptyList())
    val today = LocalDate.now()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    // Filter for current and future events and sort chronologically
    val sortedEvents = events
        .filter { it.date == null || it.date >= today }
        .sortedBy { it.date }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFB65C5C))
                    .border(1.dp, Color.Black)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Profile placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color.Black, CircleShape)
                        .align(Alignment.CenterStart)
                )
                
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .height(50.dp)
                        .align(Alignment.Center)
                )

                // Menu Icon
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp).align(Alignment.CenterEnd)
                )
            }
        },
        containerColor = Color(0xFFFFE9EA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Upcoming Events",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(sortedEvents) { event ->
                    InboxItem(event, currentUserId)
                }
            }
        }
    }
}

@Composable
fun InboxItem(event: PartyEvent, currentUserId: String?) {
    val context = LocalContext.current
    
    // Determine if unread dot should show
    // Condition: there is a last message AND user is not the last sender
    val showUnread = event.lastMessage != null && event.lastSenderId != currentUserId

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, ChatRoomActivity::class.java)
                intent.putExtra("EVENT_ID", event.id)
                intent.putExtra("EVENT_NAME", event.name)
                context.startActivity(intent)
            }
            .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFA8989)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val snippet = if (event.lastMessage.isNullOrBlank()) "Start a chat..." else event.lastMessage
                    Text(
                        text = if (snippet.length > 20) snippet.take(20) + "..." else snippet,
                        fontSize = 14.sp,
                        color = Color.Black.copy(alpha = 0.7f)
                    )
                    
                    if (!event.lastMessage.isNullOrBlank() && event.lastMessageTime != null) {
                        Text("  •  ", fontSize = 14.sp)
                        Text(
                            text = formatTimestamp(event.lastMessageTime),
                            fontSize = 14.sp,
                            color = Color.Black.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (showUnread) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2196F3)) // Blue dot
                )
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = java.text.SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(date).lowercase()
}
