package com.example.wepartyapp.ui.calendar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels // <-- Added to easily grab the ViewModel
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState // <-- Added for scrolling
import androidx.compose.foundation.verticalScroll // <-- Added for scrolling
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState // <-- Added for LiveData
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.EventViewModel // <-- Import your ViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarActivity : ComponentActivity() {
    // This creates the ViewModel and keeps it alive
    private val eventViewModel: EventViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarScreenUI(viewModel = eventViewModel)
        }
    }
}

@Composable
fun CalendarScreenUI(viewModel: EventViewModel) {
    val context = LocalContext.current
    val scrollState = rememberScrollState() // <-- Added to track scroll position

    // 1. Observe the LIST of events from Firebase
    val events by viewModel.events.observeAsState(emptyList())

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // 2. Check if the currently selected date matches ANY event in our list
    // --- Upgrade: Filter for ALL events on the selected day ---
    val dailyEvents = events.filter { it.date == selectedDate }

    val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)
    val selectedDateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .verticalScroll(scrollState) // <-- This makes the whole screen scrollable
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Changed to Top so items don't float weirdly when scrolling
    ) {

        // 1. LOGO (Now Smaller)
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "WeParty Logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp), // <-- Reduced from 220.dp
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2. TODAY BUTTON
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = {
                    currentMonth = YearMonth.now()
                    selectedDate = LocalDate.now()
                }
            ) {
                Text("Back to Today", color = Color(0xFFFF4081), fontWeight = FontWeight.Bold)
            }
        }

        // 3. CALENDAR CARD
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", modifier = Modifier.size(28.dp))
                    }
                    Text(
                        text = currentMonth.format(monthTitleFormatter),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", modifier = Modifier.size(28.dp))
                    }
                }

                HorizontalDivider(thickness = 1.dp, color = Color.LightGray, modifier = Modifier.padding(vertical = 12.dp))

                // Grid: Pass a list of all event dates so the grid can highlight multiple days
                val allEventDates = events.mapNotNull { it.date }

                CalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    eventDates = allEventDates, // Pass the list!
                    onDateSelected = { selectedDate = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. DAILY EVENTS SECTION
        Text(
            text = "Events on ${selectedDate.format(selectedDateFormatter)}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 4.dp, bottom = 12.dp)
        )

        // --- Upgrade: Show multiple cards OR an empty state ---
        if (dailyEvents.isNotEmpty()) {
            dailyEvents.forEach { event ->
                EventDetailsCard(
                    eventName = event.name,
                    eventTime = event.time,
                    eventAddress = event.address,
                    context = context
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else {
            // Empty State Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    Text(
                        text = "No events scheduled for today!",
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Bottom padding so it doesn't hug the screen edge
    }
}

// --- New: A clean Card UI to hold the event info ---
@Composable
fun EventDetailsCard(eventName: String, eventTime: String, eventAddress: String, context: android.content.Context) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = eventName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Time: ", fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text(text = eventTime, fontSize = 16.sp, color = Color.Black)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Where: ", fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text(
                    text = eventAddress,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2979FF), // Google Blue
                    modifier = Modifier.clickable {
                        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(eventAddress)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    eventDates: List<LocalDate>, // <-- Changed to accept a list of dates
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
    val emptyDaysBefore = if (firstDayOfWeek == 7) 0 else firstDayOfWeek

    // We calculate all the days we need to show (including empty ones)
    val totalGridItems = emptyDaysBefore + daysInMonth

    Column(modifier = Modifier.fillMaxWidth()) {
        // We chunk the days into groups of 7 to create "Rows" manually
        for (i in 0 until totalGridItems step 7) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (j in 0 until 7) {
                    val index = i + j
                    if (index < emptyDaysBefore || index >= totalGridItems) {
                        // Empty spacer for days before/after the month
                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
                    } else {
                        val dayNumber = index - emptyDaysBefore + 1
                        val date = currentMonth.atDay(dayNumber)
                        val isSelected = date == selectedDate
                        val isEvent = eventDates.contains(date)

                        val backgroundColor = when {
                            isEvent -> Color(0xFF00C853) // Green for event
                            isSelected -> Color(0xFF2979FF) // Blue for selected
                            else -> Color.Transparent
                        }
                        val textColor = if (isSelected || isEvent) Color.White else Color.Black

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f) // Takes equal width in the row
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .background(color = backgroundColor, shape = CircleShape)
                                .clickable { onDateSelected(date) }
                        ) {
                            Text(
                                text = dayNumber.toString(),
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected || isEvent) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

//// Updated to be non-lazy for scrolling compatibility
//@Composable
//fun CalendarGrid(
//    currentMonth: YearMonth,
//    selectedDate: LocalDate,
//    eventDate: LocalDate?, // <-- accepts nulls
//    onDateSelected: (LocalDate) -> Unit
//) {
//    val daysInMonth = currentMonth.lengthOfMonth()
//    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
//    val emptyDaysBefore = if (firstDayOfWeek == 7) 0 else firstDayOfWeek
//    val totalGridItems = emptyDaysBefore + daysInMonth
//
//    Column(modifier = Modifier.fillMaxWidth()) {
//        for (i in 0 until totalGridItems step 7) {
//            Row(modifier = Modifier.fillMaxWidth()) {
//                for (j in 0 until 7) {
//                    val index = i + j
//                    if (index < emptyDaysBefore || index >= totalGridItems) {
//                        Spacer(modifier = Modifier.weight(1f).aspectRatio(1f).padding(4.dp))
//                    } else {
//                        val dayNumber = index - emptyDaysBefore + 1
//                        val date = currentMonth.atDay(dayNumber)
//                        val isSelected = date == selectedDate
//                        val isEvent = eventDate != null && date == eventDate
//
//                        val backgroundColor = when {
//                            isEvent -> Color(0xFF00C853)
//                            isSelected -> Color(0xFF2979FF)
//                            else -> Color.Transparent
//                        }
//                        val textColor = if (isSelected || isEvent) Color.White else Color.Black
//
//                        Box(
//                            contentAlignment = Alignment.Center,
//                            modifier = Modifier
//                                .weight(1f)
//                                .aspectRatio(1f)
//                                .padding(4.dp)
//                                .background(color = backgroundColor, shape = CircleShape)
//                                .clickable { onDateSelected(date) }
//                        ) {
//                            Text(
//                                text = dayNumber.toString(),
//                                color = textColor,
//                                fontSize = 16.sp,
//                                fontWeight = if (isSelected || isEvent) FontWeight.Bold else FontWeight.Normal
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}