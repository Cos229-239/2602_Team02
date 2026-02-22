package com.example.wepartyapp.ui.event_dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Data class to hold message data
data class ChatMessage(val sender: String, val message: String, val isMe: Boolean)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatFeedContent() {
    // The list of messages in the chat
    val messages = remember {
        mutableStateListOf(
            ChatMessage("Ryan", "Hey everyone!", false),
            ChatMessage("Ryan", "I picked up the Oreos and Doritos", false),
            ChatMessage("Me", "Awesome, thanks!", true),
            ChatMessage("Me", "Anyone grabbing the ice cream?", true),
            ChatMessage("Jane", "I can grab the ice cream tomorrow", false),
            ChatMessage("Suzy", "I already have the M&Ms but lets make sure to check off items we have", false),
            ChatMessage("Me", "Ok, sounds good", true)
        )
    }

    // State to hold whatever the user is currently typing in the text box
    var currentMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .border(1.dp, Color.Black) // Border around the chat area
            .background(Color.White) // Inside of chat is white
    ) {
        // LazyColumn for scrolling messages. weight(1f) tells it to take up all available space
        // EXCEPT the space needed for the text box at the bottom
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            items(messages) { msg ->
                ChatBubble(msg)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // --- MESSAGE INPUT ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentMessage,
                onValueChange = { currentMessage = it }, // Updates the state as you type
                placeholder = { Text("Type your message...") },
                modifier = Modifier.weight(1f), // Takes up available width
                shape = RoundedCornerShape(12.dp)
            )
            IconButton(
                onClick = {
                    // Logic to send a message
                    if (currentMessage.isNotBlank()) {
                        messages.add(ChatMessage("Me", currentMessage, true))
                        currentMessage = "" // Clear the text box after sending
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

// Reusable component for drawing the individual message bubbles
@Composable
fun ChatBubble(msg: ChatMessage) {
    // If it's my message, push to the end (right). Otherwise, start (left).
    val alignment = if (msg.isMe) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (msg.isMe) ButtonPink else Color(0xFFF1F1F1) // Pink for me, gray for others

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            horizontalAlignment = if (msg.isMe) Alignment.End else Alignment.Start
        ) {
            // Only show the sender's name if it's not "Me"
            if (!msg.isMe) {
                Text(text = msg.sender, fontSize = 10.sp, color = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .background(bgColor, RoundedCornerShape(16.dp))
                    .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                Text(text = msg.message, color = Color.Black)
            }
        }
    }
}