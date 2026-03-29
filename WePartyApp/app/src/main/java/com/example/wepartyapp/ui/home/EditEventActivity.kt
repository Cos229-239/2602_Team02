package com.example.wepartyapp.ui.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import com.example.wepartyapp.ui.create_event.InviteFriendsActivity
import com.google.firebase.auth.FirebaseAuth
import kotlin.getValue
import com.example.wepartyapp.utils.BaseActivity

class EditEventActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val eventName = intent.getStringExtra("Event_Name") ?: ""

        setContent {
            // --- Status Bar Fix ---
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.setDecorFitsSystemWindows(window, false)
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
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

    // --- Scroll State ---
    val scrollState = rememberScrollState()

    // --- The "Ghost Event" Fix ---
    if (currEvent == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFE9EA))
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(color = Color(0xFFB65C5C))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Loading event details...", color = Color.Gray)
        }
        return
    }

    // --- Host verification logic ---
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isHost = currEvent.hostId == currentUserId

    LaunchedEffect(currEvent) {
        // --- The "Literal Null" Fix ---
        // Using .let guarantees we don't accidentally save the word "null" into the text fields
        currEvent.let { event ->
            eventViewModel.eventName = event.name
            eventViewModel.eventSummary = event.summary
            eventViewModel.eventDate = event.date?.toString() ?: ""
            eventViewModel.eventTime = event.time
            eventViewModel.eventAddress = event.address
            eventViewModel.eventId = event.id
        }
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
                .padding(innerpadding)
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
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
                            Modifier.size(30.dp)
                        )
                    }
                    Text(
                        text = "Home",
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // -- Page Icon --
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = null,
                        Modifier.size(60.dp),
                        tint = Color(0xFFBF6363)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    // -- Page Title --
                    Text(
                        text = "Edit Event",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 30.sp,
                    )

                    // --- Show a warning if a non-host tries to view this page ---
                    if (!isHost) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Only the host can edit these details.",
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // --- Invite Friends Section (Host Only) ---
                if (isHost) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(context, InviteFriendsActivity::class.java)
                            intent.putExtra("EVENT_ID", currEvent.id)
                            intent.putExtra("IS_EDIT_MODE", true)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFBF6363))
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFFBF6363))
                        Spacer(Modifier.width(8.dp))
                        Text("Manage Guest List", color = Color(0xFFBF6363))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                // -- Calling the composable function instead of rewriting the code --
                EventDetailsScreenUI(eventViewModel)
                Spacer(modifier = Modifier.height(80.dp))
            }

            // --- Next Button with Error Handling & Host Verification ---
            Button(
                onClick = {
                    if (!isHost) {
                        Toast.makeText(context, "Only the host can save changes.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // --- Check for empty required fields ---
                    if (eventViewModel.eventName.isBlank()) {
                        Toast.makeText(context, "Event Name cannot be empty!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    eventViewModel.updateEventInfo(eventViewModel.eventId)
                    Toast.makeText(context, "Event updated successfully!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                },
                border = if (isHost) BorderStroke(1.dp, Color(0xFFBF6363)) else BorderStroke(1.dp, Color.DarkGray),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isHost) Color(0xFFFA8989) else Color.LightGray),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Save",
                    color = if (isHost) Color.Black else Color.DarkGray
                )
            }
        }
    }
}