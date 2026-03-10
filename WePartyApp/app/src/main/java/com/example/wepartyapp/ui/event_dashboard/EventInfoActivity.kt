package com.example.wepartyapp.ui.event_dashboard

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.SideEffect
import com.example.wepartyapp.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.wepartyapp.ui.EventViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import java.time.format.DateTimeFormatter


class EventInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // --- Status Bar Fix ---
            // This grabs the phone's window and tells it to use Dark Icons (for light backgrounds)
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                        true
                }
            }


            val eventId = intent.getStringExtra("EVENT_ID") ?: ""

            setContent {
                EventInfoScreenUI(onBackClick = { finish() }, eventId = eventId)
            }
        }
    }
}

@Composable
fun EventInfoScreenUI(
    onBackClick: () -> Unit,
    eventId: String,
) {
    val viewModel: EventViewModel = viewModel()
    val events = viewModel.events.observeAsState(emptyList())
    val currentEvent = events.value.find { it.id == eventId }

    val dateFormatter = DateTimeFormatter.ofPattern("MMM. d, yyyy")

    // Temporary until I can retrieve the information from firebase!
    val going = listOf("John", "Maria")
    val maybe = listOf("Alex")
    val declined = listOf("David")

    val claimedItems = currentEvent?.eventItems?.filter { it.boughtBy != null } ?: emptyList()
    val unclaimedItems = currentEvent?.eventItems?.filter { it.boughtBy == null } ?: emptyList()


    val attendingCount = going.size
    val itemCount = claimedItems.size + unclaimedItems.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .padding(16.dp)

    ) {
        Spacer(modifier = Modifier.height(40.dp)) // <-- Pushes the whole screen down!

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onBackClick() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Back")
        }

        Spacer(modifier = Modifier.height(4.dp))

        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "- " + currentEvent?.name + " -",
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = currentEvent?.date?.format(dateFormatter) + ", " + currentEvent?.time,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = currentEvent?.address.toString(),
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(2.dp, color = Color.Black, RoundedCornerShape((6.dp)))
                .background(Color.White)
                .padding(12.dp)
        ) {
            Text(currentEvent?.summary ?: "", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Attending: $attendingCount", fontWeight = FontWeight.Bold)
            Text("Items: $itemCount", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(1.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ){

            Column() {

                Text("test text")


            }

            Spacer(modifier = Modifier.width(1.dp))

            Column(verticalArrangement = Arrangement.SpaceBetween) {
                // Claimed Items
                Text("Claimed (${claimedItems.size})", fontWeight = FontWeight.Bold)

                claimedItems.forEach {
                    Text("☑ ${it.name}")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Unclaimed Items
                Text("Unclaimed (${unclaimedItems.size})", fontWeight = FontWeight.Bold)

                unclaimedItems.forEach {
                    Text("☐ ${it.name}")
                }

            }

            Spacer(modifier = Modifier.width(15.dp))

        }

    }
}