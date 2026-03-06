package com.example.wepartyapp.ui.event_dashboard

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.ui.PartyItem
import com.example.wepartyapp.ui.create_event.AddItemsScreenUI
import com.example.wepartyapp.ui.create_event.CreateEventRoutes
import com.example.wepartyapp.ui.create_event.CreateEventScreenUI
import com.example.wepartyapp.ui.create_event.InviteFriendsScreenUI
import com.example.wepartyapp.ui.home.MainScreen
import java.time.LocalDate
import kotlin.getValue

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

    val sortedEvents = events
        .filter { it.date == null || it.date >= today }
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
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(                                                           //pg icon
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    Modifier.size(80.dp),
                    tint = Color(0xFFBF6363)
                )
                Text(                                                           //pg title
                    text = "Consolidated",
                    fontSize = 45.sp
                )
                Text(                                                           //pg title
                    text = "Shopping List",
                    fontSize = 45.sp
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(sortedEvents) { event ->
                    EventDetails(eventName = event.name, eventItemsList = event.eventItems)
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun EventDetails(eventName: String, eventItemsList: List<PartyItem>) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, RoundedCornerShape(5.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = eventName,
                fontSize = 20.sp,
                textDecoration = TextDecoration.Underline
            )
            Spacer(modifier = Modifier.height(10.dp))
            Column() {
                for (item in eventItemsList) {
                    Text(
                        text = item.name,
                        fontSize = 18.sp
                    )
                }
            }
        }
        Button(
            onClick = {
                val intent = Intent(context, EditItemActivity::class.java)
                    context.startActivity(intent)
                      },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA8989)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(text = "edit", color = Color.Black)
        }
    }
}