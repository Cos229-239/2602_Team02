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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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

    // 1. Observe the LIST of events from Firebase
    val events by viewModel.events.observeAsState(emptyList())

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // 2. Check if the currently selected date matches ANY event in our list
    val selectedEvent = events.find { it.date == selectedDate }
    val isEventDay = selectedEvent != null

    val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // 1. LOGO
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "WeParty Logo",
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentScale = ContentScale.Fit
        )

        // 2. EVENT NAME (Pulls from the selectedEvent)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(if (isEventDay) 1f else 0f)
        ) {
            Text(text = "Event Name:", fontSize = 16.sp, color = Color.Black)
            Text(
                text = selectedEvent?.name ?: "",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(top = 4.dp)
            )
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

        // 4. FOOTER INFO (Pulls from selectedEvent)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(if (isEventDay) 1f else 0f)
                .padding(bottom = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Where: ", fontSize = 16.sp, color = Color.Black)
                Text(
                    text = selectedEvent?.address ?: "",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue,
                    modifier = Modifier.clickable {
                        selectedEvent?.address?.let { address ->
                            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(address)}")
                            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            try {
                                context.startActivity(mapIntent)
                            } catch (e: Exception) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                            }
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Time: ", fontSize = 16.sp, color = Color.Black)
                Text(text = selectedEvent?.time ?: "", fontSize = 16.sp, color = Color.Black)
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

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        userScrollEnabled = false
    ) {
        items(emptyDaysBefore) {
            Spacer(modifier = Modifier.padding(4.dp).aspectRatio(1f))
        }

        items(daysInMonth) { dayIndex ->
            val date = currentMonth.atDay(dayIndex + 1)
            val isSelected = date == selectedDate

            // Check if the current date is anywhere inside our list of event dates
            val isEvent = eventDates.contains(date)

            val backgroundColor = when {
                isEvent -> Color(0xFF00C853)
                isSelected -> Color(0xFF2979FF)
                else -> Color.Transparent
            }
            val textColor = if (isSelected || isEvent) Color.White else Color.Black

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(4.dp)
                    .aspectRatio(1f)
                    .background(color = backgroundColor, shape = CircleShape)
                    .clickable { onDateSelected(date) }
            ) {
                Text(
                    text = (dayIndex + 1).toString(),
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected || isEvent) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    eventDate: LocalDate?, // <-- accepts nulls
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
    val emptyDaysBefore = if (firstDayOfWeek == 7) 0 else firstDayOfWeek

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        userScrollEnabled = false
    ) {
        items(emptyDaysBefore) {
            Spacer(modifier = Modifier.padding(4.dp).aspectRatio(1f))
        }

        items(daysInMonth) { dayIndex ->
            val date = currentMonth.atDay(dayIndex + 1)
            val isSelected = date == selectedDate

            // Check if it's an event day without crashing on null
            val isEvent = eventDate != null && date == eventDate

            val backgroundColor = when {
                isEvent -> Color(0xFF00C853)
                isSelected -> Color(0xFF2979FF)
                else -> Color.Transparent
            }
            val textColor = if (isSelected || isEvent) Color.White else Color.Black

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(4.dp)
                    .aspectRatio(1f)
                    .background(color = backgroundColor, shape = CircleShape)
                    .clickable { onDateSelected(date) }
            ) {
                Text(
                    text = (dayIndex + 1).toString(),
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected || isEvent) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}