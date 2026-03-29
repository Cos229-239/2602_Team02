package com.example.wepartyapp.ui.event_dashboard

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close // <-- Added for the full-screen close button
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog // <-- Added for full-screen viewer
import androidx.compose.ui.window.DialogProperties // <-- Added for full-screen viewer
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.ui.ItemPriceViewModel
import com.example.wepartyapp.ui.PartyItem
import com.example.wepartyapp.ui.api.NetworkResponse
import com.google.firebase.auth.FirebaseAuth
import com.example.wepartyapp.ui.ChatMessage
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

// Andy: Represents the three main view modes of the Dashboard.
enum class EventDashboardView {
    CHAT, CHECKLIST, PHOTOS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatFeedContent(eventId: String, viewModel: EventViewModel) {
    // --- Chat & Event State ---
    val messages by viewModel.messages.collectAsState()
    val events by viewModel.events.observeAsState(emptyList())
    val currentEvent = events.find { it.id == eventId }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var textInput by remember { mutableStateOf("") }

    // Track current view state
    var currentView by remember { mutableStateOf(EventDashboardView.CHAT) }

    val context = LocalContext.current

    // Start listening to messages and photos for this specific event
    LaunchedEffect(eventId) {
        viewModel.listenToMessages(eventId)
        viewModel.listenToEventPhotos(eventId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
    ) {
        // --- Header Section: Event Name, Info Icon, and Checklist Toggle ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currentEvent?.name ?: "Event Chat",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.width(8.dp))
            
            // Information icon for event details - Moved next to the name
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Event Info",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { 
                        val intent = Intent(context, EventInfoActivity::class.java)
                        intent.putExtra("EVENT_ID", eventId)
                        context.startActivity(intent)
                    }
            )

            Spacer(modifier = Modifier.weight(1f))

            // --- Navigation Toggle Icons ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 1. Photos Toggle
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Photos",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable {
                            currentView = if (currentView == EventDashboardView.PHOTOS) EventDashboardView.CHAT else EventDashboardView.PHOTOS
                        }
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 2. Checklist Toggle with Text
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        currentView = if (currentView == EventDashboardView.CHECKLIST) EventDashboardView.CHAT else EventDashboardView.CHECKLIST
                    }
                ) {
                    Text(
                        text = "Checklist",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Checklist",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // --- View Switcher Logic ---
        Box(modifier = Modifier.weight(1f)) {
            when (currentView) {
                EventDashboardView.PHOTOS -> {
                    EventGalleryUI(eventId, viewModel)
                }
                EventDashboardView.CHECKLIST -> {
                    ItemChecklistUI(eventId, viewModel)
                }
                EventDashboardView.CHAT -> {
                    // --- Chat Message Feed Section ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .background(Color(0xFFFFE9EA), RoundedCornerShape(12.dp))
                            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        // List of chat bubbles
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(messages) { msg ->
                                ChatBubble(message = msg, isCurrentUser = msg.senderId == currentUserId)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // --- Message Input Area ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = textInput,
                                onValueChange = { textInput = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFFFFE9EA), RoundedCornerShape(8.dp)),
                                placeholder = { Text("Type your message...", color = Color.DarkGray) },
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Black,
                                    unfocusedBorderColor = Color.Black,
                                    focusedContainerColor = Color(0xFFFFE9EA),
                                    unfocusedContainerColor = Color(0xFFFFE9EA)
                                )
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Send message button
                            IconButton(
                                onClick = {
                                    if (textInput.isNotBlank()) {
                                        viewModel.sendMessage(eventId, textInput)
                                        textInput = ""
                                    }
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.Black,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

// Andy: This component handles the photo grid and upload button.
@Composable
fun EventGalleryUI(eventId: String, viewModel: EventViewModel) {
    val photos by viewModel.eventPhotos.collectAsState()
    val context = LocalContext.current

    // --- NEW: State to hold the currently selected full-screen photo ---
    var fullScreenPhotoUrl by remember { mutableStateOf<String?>(null) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.uploadPhotoToEvent(eventId, it, context)
        }
    }

    // --- The Main Gallery UI ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(Color(0xFFFFE9EA), RoundedCornerShape(12.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Event Photos", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Button(
                onClick = { photoPicker.launch("image/*") },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA8989)),
                border = BorderStroke(1.dp, Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.Black)
                Spacer(Modifier.width(8.dp))
                Text("Post Photo", color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (photos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No photos yet. Be the first to post!", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(photos) { photoUrl ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                            .clickable { fullScreenPhotoUrl = photoUrl }
                    ) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = "Event Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(photoUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(topStart = 8.dp))
                        ) {
                            Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }

    // --- Full Screen Photo Viewer Dialog ---
    if (fullScreenPhotoUrl != null) {
        Dialog(
            onDismissRequest = { fullScreenPhotoUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = fullScreenPhotoUrl,
                    contentDescription = "Full Screen Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )

                // Close Button (Top Right)
                IconButton(
                    onClick = { fullScreenPhotoUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }

                // Big Download Button (Bottom Right)
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullScreenPhotoUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .size(56.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

// Andy: Displays the checklist of items needed for the event.
@Composable
fun ItemChecklistUI(eventId: String, viewModel: EventViewModel, modifier: Modifier = Modifier) {
    val events by viewModel.events.observeAsState(emptyList())
    val currentEvent = events.find { it.id == eventId }
    val items = currentEvent?.eventItems ?: emptyList()

    val priceViewModel: ItemPriceViewModel = viewModel()
    val priceResult = priceViewModel.priceResult.observeAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }

    // Logic to update price in Firestore once API responds
    LaunchedEffect(priceResult.value) {
        when (val result = priceResult.value) {
            is NetworkResponse.Success -> {
                // Added a safe fallback string just in case 'result.data' is unexpectedly null
                val exactPrice = result.data?.toString() ?: "Not Found"
                // Find the item that was just added with "Loading..." price
                val itemToUpdate = items.find { it.price == "Loading..." }
                if (itemToUpdate != null) {
                    viewModel.updateItemPriceInFirestore(eventId, itemToUpdate.name, exactPrice)
                }
            }
            is NetworkResponse.Error -> {
                val itemToUpdate = items.find { it.price == "Loading..." }
                if (itemToUpdate != null) {
                    viewModel.updateItemPriceInFirestore(eventId, itemToUpdate.name, "Not Found")
                }
            }
            else -> {}
        }
    }

    // --- Main Checklist Container ---
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(Color(0xFFFFE9EA), RoundedCornerShape(12.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        // List of party items to be acquired
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(items) { item ->
                ChecklistItemRow(eventId, item, viewModel)
                HorizontalDivider(color = Color.Black, thickness = 1.dp)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (showAddDialog) {
            // --- Add Item Input Area (matches Create Event logic) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newItemName,
                    onValueChange = { newItemName = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Item Name") },
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        if (newItemName.isNotBlank()) {
                            // Add item with placeholder price
                            viewModel.addItemToExistingEvent(eventId, PartyItem(name = newItemName, price = "Loading..."))
                            // Fetch real price via API
                            priceViewModel.getData(newItemName)
                            newItemName = ""
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA8989)),
                    border = BorderStroke(1.dp, Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add", color = Color.Black)
                }
            }
        }
        
        // Toggle button to show/hide the add item input
        Button(
            onClick = { showAddDialog = !showAddDialog },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA8989)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (showAddDialog) "Cancel" else "Add Items", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

// Andy: Represents a single row in the checklist.
@Composable
fun ChecklistItemRow(eventId: String, item: PartyItem, viewModel: EventViewModel) {
    val user = FirebaseAuth.getInstance().currentUser
    val isChecked = item.boughtBy != null

    // --- Individual Item Row ---
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox to mark item as acquired
        Checkbox(
            checked = isChecked,
            onCheckedChange = { viewModel.toggleItemCheck(eventId, item) },
            colors = CheckboxDefaults.colors(
                checkedColor = Color.Black,
                uncheckedColor = Color.Black,
                checkmarkColor = Color.White
            )
        )
        
        // Item name with strikethrough if acquired
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                fontSize = 16.sp,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
            )
            // Show price if available
            if (item.price.isNotBlank()) {
                Text(
                    text = item.price,
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }
        
        // Display name of the attendee who acquired the item
        if (isChecked) {
            Text(
                text = item.boughtByName ?: "Someone",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

// Andy: Styles the message bubble based on sender identity.
@Composable
fun ChatBubble(message: ChatMessage, isCurrentUser: Boolean) {
    // --- Individual Chat Bubble ---
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        // Show sender's name if they are not the current user
        if (!isCurrentUser) {
            Text(
                text = buildAnnotatedString {
                    append(message.senderName)
                    withStyle(style = SpanStyle(fontSize = 10.sp)) {
                        append(" (@${message.appUserId})")
                    }
                },
                fontSize = 13.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
            )
        }

        // Message bubble with different colors for self vs others
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isCurrentUser) Color(0xFFFA8989) else Color(0xFFF1F1F1),
                    shape = RoundedCornerShape(50)
                )
                .border(1.dp, Color.Black, RoundedCornerShape(50))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = Color.Black,
                fontSize = 15.sp
            )
        }
    }
}
