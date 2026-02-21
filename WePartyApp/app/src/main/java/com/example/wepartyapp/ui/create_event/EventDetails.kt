package com.example.wepartyapp.ui.create_event

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Event Details Screen
@Preview
@Composable
fun EventDetailsScreenUI() {
    var eventName by remember {
        mutableStateOf("")
    }
    var summaryDetails by remember {
        mutableStateOf("")
    }
    var whenDetails by remember {
        mutableStateOf("")
    }
    var whereDetails by remember {
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
                value = summaryDetails,
                onValueChange = { summaryDetails = it }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "When:",
            fontSize = 20.sp,
            textDecoration = TextDecoration.Underline
        )
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = whenDetails,
                onValueChange = { whenDetails = it }
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Where:",
            fontSize = 20.sp,
            textDecoration = TextDecoration.Underline
        )
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = whereDetails,
                onValueChange = { whereDetails = it }
            )
        }
    }
}