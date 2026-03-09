package com.example.wepartyapp.ui.event_dashboard

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.ui.ItemPriceViewModel
import com.example.wepartyapp.ui.PartyItem
import com.example.wepartyapp.ui.api.NetworkResponse
import com.google.firebase.auth.FirebaseAuth
import com.example.wepartyapp.ui.ChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatFeedContent(eventId: String, viewModel: EventViewModel) {
    // --- Chat & Event State ---
    val messages by viewModel.messages.collectAsState()
    val events by viewModel.events.observeAsState(emptyList())
    val currentEvent = events.find { it.id == eventId }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var textInput by remember { mutableStateOf("") }
    var showChecklist by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Start listening to messages for this specific event
    LaunchedEffect(eventId) {
        viewModel.listenToMessages(eventId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA)) // Pink background matching the theme
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
            
            // Toggle button to switch between Chat and Checklist views
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Checklist",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { showChecklist = !showChecklist }
            )
        }

        if (showChecklist) {
            // --- Item Checklist View ---
            ItemChecklistUI(eventId, viewModel)
        } else {
            // --- Chat Message Feed Section ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .background(Color(0xFFFFE9EA), RoundedCornerShape(12.dp)) // Updated to have rounded borders
                    .border(1.dp, Color.Black, RoundedCornerShape(12.dp)) // Applied rounded borders
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
                            .background(Color(0xFFFFE9EA), RoundedCornerShape(8.dp)), // Pink background
                        placeholder = { Text("Type your message...", color = Color.DarkGray) },
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black,
                            focusedContainerColor = Color(0xFFFFE9EA), // Pink background
                            unfocusedContainerColor = Color(0xFFFFE9EA) // Pink background
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
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ItemChecklistUI(eventId: String, viewModel: EventViewModel) {
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
                val exactPrice = result.data
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
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .background(Color(0xFFFFE9EA), RoundedCornerShape(12.dp)) // Pink background
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
                text = message.senderName,
                fontSize = 12.sp,
                color = Color.Black,
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
