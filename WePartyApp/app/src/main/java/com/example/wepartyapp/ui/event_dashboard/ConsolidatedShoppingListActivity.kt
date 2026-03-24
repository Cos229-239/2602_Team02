package com.example.wepartyapp.ui.event_dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.ui.PartyItem
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate

class ConsolidatedShoppingListActivity : ComponentActivity() {
    private val eventViewModel: EventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            ConsolidatedShoppingListScreenUI(viewModel = eventViewModel)
        }
    }
}

@Composable
fun ConsolidatedShoppingListScreenUI(viewModel: EventViewModel) {

    val events by viewModel.events.observeAsState(emptyList())
    val today = LocalDate.now()

    // --- 1. Grab current user for security filtering ---
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid

    // --- 2. Filter for Date and Participation ---
    val sortedEvents = events
        .filter { event ->
            val isUpcoming = event.date == null || event.date >= today

            val amIParticipating = currentUserId != null &&
                    (event.hostId == currentUserId || event.invitedGuests.contains(currentUserId))

            isUpcoming && amIParticipating
        }
        .sortedBy { it.date }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Page Icon
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    Modifier.size(80.dp),
                    tint = Color(0xFFBF6363)
                )
                //Page Title
                Text(
                    text = "Consolidated",
                    fontSize = 45.sp
                )
                Text(
                    text = "Shopping List",
                    fontSize = 45.sp
                )
            }
            Spacer(modifier = Modifier.height(25.dp))

            // --- The "Ghost Town" Fix ---
            if (sortedEvents.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "You don't have any upcoming parties yet!",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(sortedEvents) { event ->
                        EventDetails(eventID = event.id, eventName = event.name, eventItemsList = event.eventItems)
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // --- The "Hidden Bottom" Fix ---
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun EventDetails(eventID: String, eventName: String, eventItemsList: List<PartyItem>) {
    val context = LocalContext.current
    //Changed the event box background and border to make the box popping more subtle - the white seemed too intense for the colors we currently have
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFBF6363), RoundedCornerShape(5.dp))
            .shadow(elevation = 5.dp, shape = RoundedCornerShape(5.dp))
            .background(color = Color(0xFFFFD4D6)) // Added a white background to make the cards pop against the pink^
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // --- The "Text Collision" Fix ---
            // Changed from a raw text element to a Row so the Event Name never overlaps the Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = eventName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )

                Button(
                    onClick = {
                        val intent = Intent(context, EditItemActivity::class.java)
                        intent.putExtra("Event_ID", eventID)    //passing event id to be able to find the specific event in edit item screen
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA8989)),
                    modifier = Modifier.height(36.dp) // Keeps the button from getting too tall
                ) {
                    Text(text = "Edit", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(5.dp))

            // Items List
            Column {
                if (eventItemsList.isEmpty()) {
                    Text(text = "No items added yet.", fontSize = 16.sp, color = Color.Gray)
                } else {
                    for (item in eventItemsList) {
                        // --- The "Messy Text" Continuity Fix ---
                        val displayName = item.name.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }

                        Row(modifier = Modifier.padding(bottom = 6.dp)) {
                            Text(text = "• ", fontSize = 18.sp, color = Color(0xFFBF6363))
                            Text(
                                text = displayName,
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }
        }
    }
}