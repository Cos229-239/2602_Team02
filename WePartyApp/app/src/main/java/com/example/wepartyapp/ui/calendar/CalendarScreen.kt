package com.example.wepartyapp.ui.calendar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class CalendarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalendarScreenUI()
        }
    }
}

@Composable
fun CalendarScreenUI() {
    val context = LocalContext.current

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    val eventDate = LocalDate.of(2026, 2, 14)
    val eventName = "Valentines Day Party!!"
    val eventAddress = "123 Mickey Ln, Winter Park, FL"
    val eventTime = "Feb 14, 2026 @ 4:00 PM"

    val isEventDay = selectedDate == eventDate
    val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        // This pushes elements apart evenly so they fill the screen vertically
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

        // 2. EVENT NAME
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(if (isEventDay) 1f else 0f)
        ) {
            Text(text = "Event Name:", fontSize = 16.sp, color = Color.Black)
            Text(
                text = eventName,
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

                // Grid
                CalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    eventDate = eventDate,
                    onDateSelected = { selectedDate = it }
                )
            }
        }

        // 4. FOOTER INFO
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(if (isEventDay) 1f else 0f)
                .padding(bottom = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Where: ", fontSize = 16.sp, color = Color.Black)
                Text(
                    text = eventAddress,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Blue,
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
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Time: ", fontSize = 16.sp, color = Color.Black)
                Text(text = eventTime, fontSize = 16.sp, color = Color.Black)
            }
        }
    }
}

@Composable
fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    eventDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value
    val emptyDaysBefore = if (firstDayOfWeek == 7) 0 else firstDayOfWeek

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
        // Just big enough to fit 6 rows of dates without scrolling
        userScrollEnabled = false
    ) {
        items(emptyDaysBefore) {
            Spacer(modifier = Modifier.padding(4.dp).aspectRatio(1f))
        }

        items(daysInMonth) { dayIndex ->
            val date = currentMonth.atDay(dayIndex + 1)
            val isSelected = date == selectedDate
            val isEvent = date == eventDate

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