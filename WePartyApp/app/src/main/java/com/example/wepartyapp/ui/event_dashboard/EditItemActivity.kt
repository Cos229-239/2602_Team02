package com.example.wepartyapp.ui.event_dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.ui.ItemPriceViewModel
import com.example.wepartyapp.ui.api.NetworkResponse
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wepartyapp.ui.PartyItem
import androidx.core.view.WindowCompat
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.home.MainActivity
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate


class EditItemActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventID = intent.getStringExtra("Event_ID") ?: ""

        setContent {
            // --- Status Bar Fix ---
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                        true
                }
            }
            val eventViewModel: EventViewModel by viewModels()
            val priceViewModel: ItemPriceViewModel = viewModel()
            EditItemsScreen(eventID = eventID, viewPriceModel = priceViewModel, viewItemModel = eventViewModel)
        }
    }
}

@Composable
fun EditItemsScreen(eventID: String, viewPriceModel: ItemPriceViewModel, viewItemModel: EventViewModel) {
    val context = LocalContext.current

    var item by remember { mutableStateOf("") }

    val priceResult = viewPriceModel.priceResult.observeAsState()

    val events by viewItemModel.events.observeAsState(emptyList())
    val today = LocalDate.now()

    val _itemList by viewItemModel._items.collectAsState()

    val sortedEvents = events
        .filter { it.date == null || it.date >= today }
        .sortedBy { it.date }

    // --- Host verification logic ---
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val currentEvent = events.find { it.id == eventID }
    val isHost = currentEvent?.hostId == currentUserId

    LaunchedEffect(sortedEvents) {
        for (selectedEvent in sortedEvents) {
            if (selectedEvent.id == eventID) {
                val selectedEventList = selectedEvent.eventItems

                viewItemModel.clearItems()

                for (partyItem in selectedEventList) {
                    viewItemModel.addItems(
                        PartyItem(
                            name = partyItem.name,
                            price = partyItem.price
                        )
                    )
                }
            }
        }
    }

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
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        context.startActivity(intent)
                        (context as? Activity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            Modifier.size(35.dp)
                        )
                    }
                    Text(
                        text = "Consolidated Shopping List",
                        fontSize = 15.sp
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        Modifier.size(70.dp),
                        tint = Color(0xFFBF6363)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Edit Items",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 30.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = item,
                        onValueChange = { text ->
                            item = text
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text(text = "Item") }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            // --- Instantly force the input to lowercase and trim spaces ---
                            val trimmedItem = item.trim().lowercase()

                            // --- Check for empty input ---
                            if (trimmedItem.isBlank()) {
                                Toast.makeText(context, "Please type an item name first.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // --- Check for duplicates ---
                            val alreadyExists = _itemList.any { it.name.equals(trimmedItem, ignoreCase = true) }
                            if (alreadyExists) {
                                Toast.makeText(context, "That item is already on the list!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // If it passes validation, add it
                            viewItemModel.addItems(PartyItem(name = trimmedItem, price = "Loading..."))
                            viewPriceModel.getData(trimmedItem)
                            item = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA8989)),
                    ) {
                        Text(text = "Add", color = Color.Black)
                    }
                }

                LaunchedEffect(priceResult.value) {
                    when (val result = priceResult.value) {
                        is NetworkResponse.Success -> {
                            // --- We now use the Pair from the ViewModel ---
                            val itemName = result.data.first
                            val exactPrice = result.data.second

                            viewItemModel.updatePrice(itemName, exactPrice)
                        }

                        is NetworkResponse.Error -> {
                            // --- Safely display an error toast instead of corrupting the list ---
                            Toast.makeText(context, "Could not fetch price.", Toast.LENGTH_SHORT).show()
                        }

                        else -> {}
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(_itemList) { partyItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Capitalize just the first letter for UI presentation
                            val displayName = partyItem.name.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase() else it.toString()
                            }
                            Text(text = displayName, modifier = Modifier.weight(1f))

                            // Style the loading/error text nicely
                            Text(
                                text = partyItem.price,
                                color = when (partyItem.price) {
                                    "Not Found", "Unavailable" -> Color.Red
                                    "Loading..." -> Color.Gray
                                    else -> Color.Black
                                }
                            )

                            // --- Only show the delete button if they are the host ---
                            if (isHost) {
                                Spacer(modifier = Modifier.width(16.dp))
                                IconButton(
                                    onClick = { viewItemModel.removeItem(partyItem) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                        Divider()
                    }
                }
            }
            Button(
                onClick = {
                    viewItemModel.updateEventItems(eventID)
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA8989)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(text = "Save", color = Color.Black)
            }
        }
    }
}