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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.IconButton


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

            EventInfoScreenUI(
                onBackClick = { finish() },
                eventId = eventId
            )
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
    val attending = currentEvent?.attending ?: emptyList()
    val maybe = currentEvent?.maybe ?: emptyList()
    val declined = currentEvent?.declined ?: emptyList()

    val claimedItems = currentEvent?.eventItems?.filter { it.boughtBy != null } ?: emptyList()
    val unclaimedItems = currentEvent?.eventItems?.filter { it.boughtBy == null } ?: emptyList()

    val attendingCount = attending.size
    val maybeCount = maybe.size
    val declinedCount = declined.size
    val totalCount = attendingCount + maybeCount + declinedCount

    val itemCount = claimedItems.size + unclaimedItems.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .verticalScroll(rememberScrollState())
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
            text = "- ${currentEvent?.name ?: ""} -",
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${currentEvent?.date?.format(dateFormatter) ?: ""}, ${currentEvent?.time ?: ""}",
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = currentEvent?.address ?: "",
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
            Text("Roll Call: $totalCount", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Items: $itemCount", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(25.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ){

            Column(modifier = Modifier.weight(1f)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    IconButton(
                        onClick = { viewModel.updateAttendance(eventId, "attending") },
                        modifier = Modifier
                            .background(Color(0xFF6C5BB7), RoundedCornerShape(50))
                            .size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Attending",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { viewModel.updateAttendance(eventId, "maybe") },
                        modifier = Modifier
                            .background(Color(0xFFFFB74D), RoundedCornerShape(50))
                            .size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Maybe",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = { viewModel.updateAttendance(eventId, "declined") },
                        modifier = Modifier
                            .background(Color(0xFFE57373), RoundedCornerShape(50))
                            .size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Decline",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Attending - $attendingCount", fontWeight = FontWeight.Bold)

                attending.forEach {
                    Text("• $it")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Maybe - $maybeCount", fontWeight = FontWeight.Bold)

                maybe.forEach {
                    Text("• $it")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Declined - $declinedCount", fontWeight = FontWeight.Bold)

                declined.forEach {
                    Text("• $it")
                }

            }

            Spacer(modifier = Modifier.width(1.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
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