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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.ui.create_event.EventDetailsScreenUI
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
@Composable
fun EditEventScreen(eventViewModel: EventViewModel, eventName: String) {

    val context = LocalContext.current
    val events = eventViewModel.events.observeAsState(emptyList())
    val currEvent = events.value.find { it.name == eventName }

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
                            value = currEvent?.name ?: "",
                            onValueChange = {  },
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
                            value = currEvent?.summary ?: "",
                            onValueChange = {  },
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
                                value = "",
                                onValueChange = { },
                                readOnly = true, // Prevents keyboard from popping up
                                singleLine = true
                            )
                            //Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
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
                                value = currEvent?.time ?: "",
                                onValueChange = { },
                                readOnly = true,
                                singleLine = true
                            )
                            //Box(modifier = Modifier.matchParentSize().clickable { showTimePicker = true })
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
                            value = currEvent?.address ?: "",
                            onValueChange = {  },
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}