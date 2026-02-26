package com.example.wepartyapp.ui.create_event

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wepartyapp.ui.EventViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Event Details Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailsScreenUI(viewItemModel: EventViewModel) {
    // We removed the local rememberSaveable variables.
    // Now everything types directly into the EventViewModel so it survives navigation.

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
                        viewItemModel.eventDate = sdf.format(Date(millis))
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

                    viewItemModel.eventTime = "$formattedHour:$formattedMin $amPm"
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
                value = viewItemModel.eventName,
                onValueChange = { viewItemModel.eventName = it }
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
                value = viewItemModel.eventSummary,
                onValueChange = { viewItemModel.eventSummary = it }
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
                    value = viewItemModel.eventDate,
                    onValueChange = { },
                    readOnly = true // Prevents keyboard from popping up
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
            Box(modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = viewItemModel.eventTime,
                    onValueChange = { },
                    readOnly = true
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
                value = viewItemModel.eventAddress,
                onValueChange = { viewItemModel.eventAddress = it }
            )
        }
    }
}