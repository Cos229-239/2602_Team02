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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
                //next step - cleaning up code - try calling the composable EventsDetailScreenUI to see if that works - it does
                EventDetailsScreenUI(eventViewModel)
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