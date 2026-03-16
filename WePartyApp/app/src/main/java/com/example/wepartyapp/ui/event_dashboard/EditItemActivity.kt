package com.example.wepartyapp.ui.event_dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
//import androidx.navigation.NavController
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.ui.ItemPriceViewModel
import com.example.wepartyapp.ui.api.NetworkResponse
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wepartyapp.ui.PartyItem
//import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.wepartyapp.ui.home.MainActivity
import com.google.firebase.auth.FirebaseAuth // <-- Added Firebase Auth Import
import java.time.LocalDate
//import kotlin.getValue

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

    val sortedEvents = events
        .filter { it.date == null || it.date >= today }
        .sortedBy { it.date }

    // --- NEW: Host verification logic ---
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
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
                        if (item.isNotBlank()) {
                            viewItemModel.addItems(PartyItem(name = item, price = "Loading..."))
                            viewPriceModel.getData(item)
                            item = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA8989)),
                ) {
                    Text(text = "Add", color = Color.Black)
                }
            }

            LaunchedEffect(priceResult.value) {
                when (val result = priceResult.value) {
                    is NetworkResponse.Success -> {
                        val exactPrice = result.data?.toString() ?: "Not Found"

                        val ogList = viewItemModel._items.value
                        val mutableCopy = ogList.toMutableList()
                        val index = mutableCopy.indexOfLast { it.price == "Loading..." }
                        if(index != -1) {
                            viewItemModel.updatePrice(mutableCopy[index].name, exactPrice)
                        }
                    }
                    is NetworkResponse.Error -> {
                        val ogList = viewItemModel._items.value
                        val mutableCopy = ogList.toMutableList()
                        val index = mutableCopy.indexOfLast { it.price == "Loading..." }
                        if(index != -1) {
                            viewItemModel.updatePrice(mutableCopy[index].name, "Not Found")
                        }
                    }
                    else -> {}
                }
            }
            val _itemList by viewItemModel._items.collectAsState()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(_itemList) { partyItem ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = partyItem.name, modifier = Modifier.weight(1f))
                        Text(text = partyItem.price)

                        // --- NEW: Only show the delete button if they are the host! ---
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