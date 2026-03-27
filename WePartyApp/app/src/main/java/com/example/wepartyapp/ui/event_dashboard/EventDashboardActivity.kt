package com.example.wepartyapp.ui.event_dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.ui.PartyEvent
import com.example.wepartyapp.ui.create_event.CreateEventActivity
import com.example.wepartyapp.ui.home.MainActivity
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.util.*
import com.example.wepartyapp.utils.BaseActivity
import kotlin.math.roundToInt


class EventDashboardActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                }
            }

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

    // --- The "Party Crasher" Filter ---
    // Filter for current and future events and ensure the user is actually invited
    val sortedEvents = events
        .filter { event ->
            val isUpcoming = event.date == null || event.date >= today

            // Only let it through if they are the Host or on the Guest List
            val isParticipant = currentUserId != null &&
                    (event.hostId == currentUserId || event.invitedGuests.contains(currentUserId))

            isUpcoming && isParticipant // Both conditions must be true
        }
        .sortedBy { it.date }

    Scaffold(
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

            // --- The "Empty Inbox" State ---
            if (sortedEvents.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No active chats yet.",
                        fontSize = 18.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "When you host a party or get invited to one, the chat will appear right here!",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sortedEvents, key = { it.id }) { event ->
                        InboxItem(event, currentUserId, onDelete = {
                            viewModel.deleteEvent(event);
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun InboxItem(event: PartyEvent, currentUserId: String?, onDelete: () -> Unit,) {
    val context = LocalContext.current

    val density = LocalDensity.current
    val maxDrag = with(density) { 80.dp.toPx() }

    var offsetX by remember { mutableFloatStateOf(0f) }

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        label = "swipeOffset"
    )
    
    // Determine if unread dot should show
    // Logic: last message exists AND user is NOT the last sender AND user has not read the latest message
    val lastReadTime = event.readByUsers[currentUserId ?: ""] ?: 0L
    val showUnread = event.lastMessageTime != null && 
                     event.lastMessageTime > lastReadTime && 
                     event.lastSenderId != currentUserId

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Delete Icon
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Red)
                .padding(end = 20.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            IconButton(
                onClick = onDelete
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Event",
                    tint = Color.White
                )
            }
        }

        // Event Inbox
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        val newOffset = offsetX + delta
                        offsetX = newOffset.coerceIn(-maxDrag, 0f)
                    },
                    onDragStopped = {
                        offsetX = if (offsetX < -maxDrag / 2f) -maxDrag else 0f
                    }
                )
                .clickable {
                    if (offsetX != 0f) {
                        offsetX = 0f
                    } else {
                        val intent = Intent(context, ChatRoomActivity::class.java)
                        intent.putExtra("EVENT_ID", event.id)
                        intent.putExtra("EVENT_NAME", event.name)
                        context.startActivity(intent)
                    }
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
                        val rawSnippet = event.lastMessage ?: "Start a chat..."
                        val cleanSnippet = rawSnippet.replace("\n", " ").trim()

                        Text(
                            text = cleanSnippet,
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (!event.lastMessage.isNullOrBlank()) {
                            event.lastMessageTime?.let { time ->
                                Text(
                                    text = "  •  ${formatTimestamp(time)}",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                if (showUnread) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2196F3))
                    )
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = java.text.SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(date).lowercase()
}

// --- Chat Room Activity ---
class ChatRoomActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val eventId = intent.getStringExtra("EVENT_ID") ?: ""
        val eventName = intent.getStringExtra("EVENT_NAME") ?: "Event Chat"

        setContent {
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (this as Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                }
            }

            val viewModel: EventViewModel = viewModel()
            var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }

            // Mark event as read when the user opens the chat
            LaunchedEffect(eventId) {
                viewModel.markEventAsRead(eventId)
                profilePhotoUri = FirebaseAuth.getInstance().currentUser?.photoUrl
            }

            Scaffold(
                containerColor = Color(0xFFFFE9EA),
                modifier = Modifier.border(3.dp, color = Color.Black),
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
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

                        Row(
                            modifier = Modifier.align(Alignment.CenterStart),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(30.dp)
                                    .clickable { finish() }
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .border(2.dp, color = Color.Black, shape = CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { finish() },
                                contentAlignment = Alignment.Center
                            ) {
                                if (profilePhotoUri != null) {
                                    AsyncImage(
                                        model = profilePhotoUri,
                                        contentDescription = "Profile",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(120.dp)
                                .align(Alignment.Center)
                        )

                        Box(
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color.Black, RoundedCornerShape(10.dp))
                                    .clickable {
                                        val intent = Intent(this@ChatRoomActivity, MainActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        startActivity(intent)
                                        overridePendingTransition(0, 0)
                                        finish()
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                },
                bottomBar = {
                    EventBottomNavigationBar(
                        selectedTab = 4,
                        onTabSelected = { tabId ->
                            when (tabId) {
                                4 -> {
                                    finish()
                                    overridePendingTransition(0, 0)
                                }
                                2 -> {
                                    startActivity(Intent(this@ChatRoomActivity, CreateEventActivity::class.java))
                                }
                                else -> {
                                    val intent = Intent(this@ChatRoomActivity, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    intent.putExtra("TARGET_TAB", tabId) // Send the hidden message
                                    startActivity(intent)
                                    overridePendingTransition(0, 0)
                                    finish()
                                }
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Color(0xFFFFE9EA))
                ) {
                    ChatFeedContent(eventId = eventId, viewModel = viewModel)
                }
            }
        }
    }
}

// ======================================================================
// --- Navigation Bar Components ---
// ======================================================================

@Composable
fun EventBottomNavigationBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .height(60.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFC96B6B),
                        Color(0xFFB65C5C),
                        Color(0xFF8E3F3F)
                    )
                )
            )
            .border(3.dp, Color.Black, RoundedCornerShape(20.dp)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        EventNavigationItem(icon = Icons.Default.Home, label = "Home", selected = selectedTab == 0) { onTabSelected(0) }
        EventNavigationItem(icon = Icons.Default.DateRange, label = "Calendar", selected = selectedTab == 1) { onTabSelected(1) }
        EventNavigationItem(icon = Icons.Default.Add, label = "Create Event", selected = selectedTab == 2) { onTabSelected(2) }
        EventNavigationItem(icon = Icons.Default.CheckCircle, label = "Lists", selected = selectedTab == 3) { onTabSelected(3) }
        EventNavigationItem(icon = Icons.Default.Edit, label = "Events", selected = selectedTab == 4) { onTabSelected(4) }
    }
}

@Composable
fun EventNavigationItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color.White else Color.Black,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (selected) Color.White else Color.Black
        )
    }
}