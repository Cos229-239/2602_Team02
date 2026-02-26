package com.example.wepartyapp.ui.create_event

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wepartyapp.ui.EventViewModel

// Event Details Screen
@Composable
fun EventDetailsScreenUI(viewItemModel: EventViewModel) {
    // We removed the local rememberSaveable variables.
    // Now everything types directly into the EventViewModel so it survives navigation.

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
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = viewItemModel.eventDate,
                onValueChange = { viewItemModel.eventDate = it }
            )
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
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = viewItemModel.eventTime,
                onValueChange = { viewItemModel.eventTime = it }
            )
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