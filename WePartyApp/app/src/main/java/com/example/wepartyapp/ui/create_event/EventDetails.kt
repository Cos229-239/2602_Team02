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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Event Details Screen
@Preview
@Composable
fun EventDetailsScreenUI() {
    var eventName by rememberSaveable {
        mutableStateOf("")
    }
    var eventSummary by rememberSaveable() {
        mutableStateOf("")
    }
    var eventDate by rememberSaveable() {
        mutableStateOf("")
    }
    var eventTime by rememberSaveable() {
        mutableStateOf("")
    }
    var eventAddress by rememberSaveable() {
        mutableStateOf("")
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
                value = eventName,
                onValueChange = { eventName = it }
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
                value = eventSummary,
                onValueChange = { eventSummary = it }
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
                value = eventDate,
                onValueChange = { eventDate = it }
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
                value = eventTime,
                onValueChange = { eventTime = it }
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
                value = eventAddress,
                onValueChange = { eventAddress = it }
            )
        }
    }
}