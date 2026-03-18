package com.example.wepartyapp.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.EventViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.getValue

class EditEventActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventName = intent.getStringExtra("Event_Name") ?: ""

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
            EditEventScreen(eventViewModel = eventViewModel, eventName = eventName)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(eventViewModel: EventViewModel, eventName: String) {

    val context = LocalContext.current
    val events = eventViewModel.events.observeAsState(emptyList())
    val currEvent = events.value.find { it.name == eventName }

    //helps with rerendering - text fields were reverting to og data everytime the date+time fields were clicked
    LaunchedEffect(currEvent) {
        //saving the current event's info to our shared view model's vars to be able to display + change
        eventViewModel.eventName = currEvent?.name.toString()
        eventViewModel.eventSummary = currEvent?.summary.toString()
        eventViewModel.eventDate = currEvent?.date.toString()
        eventViewModel.eventTime = currEvent?.time.toString()
        eventViewModel.eventAddress = currEvent?.address.toString()
        eventViewModel.eventId = currEvent?.id.toString()
    }

    // --- Popup States ---
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    // --- Date Picker Popup ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Formats the raw milliseconds into a clean YYYY-MM-DD string
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        eventViewModel.eventDate = sdf.format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("OK", color = Color(0xFFBF6363)) } // Dark Pink
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = Color(0xFFBF6363))
                }
            },
            colors = DatePickerDefaults.colors(containerColor = Color.White)
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    titleContentColor = Color(0xFFBF6363),
                    headlineContentColor = Color.Black,
                    weekdayContentColor = Color.Black,
                    dayContentColor = Color.Black,
                    selectedDayContainerColor = Color(0xFFBF6363),
                    selectedDayContentColor = Color.White,
                    todayContentColor = Color(0xFFBF6363),
                    todayDateBorderColor = Color(0xFFBF6363)
                )
            )
        }
    }

    // --- Time Picker Popup ---
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val hour = timePickerState.hour
                    val min = timePickerState.minute
                    val amPm = if (hour >= 12) "PM" else "AM"
                    val formattedHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                    val formattedMin = min.toString().padStart(2, '0')

                    eventViewModel.eventTime = "$formattedHour:$formattedMin $amPm"
                    showTimePicker = false
                }) { Text("OK", color = Color(0xFFBF6363)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = Color(0xFFBF6363))
                }
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFFFFE9EA), // Light Pink background
                        selectorColor = Color(0xFFBF6363),  // Dark Pink selector
                        containerColor = Color.White,
                        timeSelectorSelectedContainerColor = Color(0xFFFA8989),
                        timeSelectorUnselectedContainerColor = Color(0xFFFFE9EA)
                    )
                )
            },
            containerColor = Color.White
        )
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
                        text = "Home",
                        fontSize = 15.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(                                                           //pg icon
                        imageVector = Icons.Default.Create,
                        contentDescription = null,
                        Modifier.size(60.dp),
                        tint = Color(0xFFBF6363)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(                                                           //pg title
                        text = "Edit Event",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 30.sp,
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                //next step - cleaning up code - try calling just the composable EventsDetailScreenUI to see if that works
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(
                        text = "Event Name:",
                        fontSize = 20.sp,
                        textDecoration = TextDecoration.Underline
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = eventViewModel.eventName,                                      //display current event's name
                            onValueChange = { eventViewModel.eventName = it },                     //update the var when the value changes
                            singleLine = true // Forces "Next" on keyboard instead of return
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Summary:",
                        fontSize = 20.sp,
                        textDecoration = TextDecoration.Underline
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = eventViewModel.eventSummary,
                            onValueChange = { eventViewModel.eventSummary = it },
                            minLines = 3, // Makes it look like a message box
                            maxLines = 5
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Date:",
                        fontSize = 20.sp,
                        textDecoration = TextDecoration.Underline
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Trick: Put a transparent clickable box over the text field to trigger the popup
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = eventViewModel.eventDate,
                                onValueChange = { },
                                readOnly = true, // Prevents keyboard from popping up
                                singleLine = true
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Time:",
                        fontSize = 20.sp,
                        textDecoration = TextDecoration.Underline
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = eventViewModel.eventTime,
                                onValueChange = { },
                                readOnly = true,
                                singleLine = true
                            )
                            Box(modifier = Modifier.matchParentSize().clickable { showTimePicker = true })
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Address:",
                        fontSize = 20.sp,
                        textDecoration = TextDecoration.Underline
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = eventViewModel.eventAddress,
                            onValueChange = { eventViewModel.eventAddress = it },
                            singleLine = true
                        )
                    }
                }
            }
            Button(
                onClick = {
                    eventViewModel.updateEventInfo(eventViewModel.eventId)
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                },
                colors = ButtonDefaults.buttonColors(Color(0xFFFA8989)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(text = "Save", color = Color.Black)
            }
        }
    }
}