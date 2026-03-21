package com.example.wepartyapp.ui.event_dashboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import com.example.wepartyapp.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.example.wepartyapp.ui.EventViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import com.example.wepartyapp.ui.GroupDietarySummary
import com.google.firebase.auth.FirebaseAuth
import com.example.wepartyapp.ui.create_event.InviteFriendsActivity

class EventInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // --- Status Bar Fix ---
            // This grabs the phone's window and tells it to use Dark Icons (for light backgrounds)
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                        true
                }
            }


            val eventId = intent.getStringExtra("EVENT_ID") ?: ""

            EventInfoScreenUI(
                onBackClick = { finish() },
                eventId = eventId
            )
        }
    }
}

@Composable
fun EventInfoScreenUI(
    onBackClick: () -> Unit,
    eventId: String,
) {
    val viewModel: EventViewModel = viewModel()
    val events = viewModel.events.observeAsState(emptyList())
    val currentEvent = events.value.find { it.id == eventId }
    val context = LocalContext.current

    // --- Collect the Group Dietary Summary State ---
    val groupSummary by viewModel.groupDietarySummary.collectAsState()

    // --- Trigger the fetch for dietary summary when the event loads ---
    LaunchedEffect(currentEvent) {
        currentEvent?.let { event ->
            viewModel.fetchGroupDietarySummary(event.invitedGuests)
        }
    }

    // --- The "Ghost Event" Fix ---
    // If the event hasn't loaded from Firebase yet (or was deleted), show a clean loading/error state
    if (currentEvent == null) {
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
            Text(
                text = "Loading event details...",
                color = Color.Gray,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(50))
                    .border(1.dp, Color.Black, RoundedCornerShape(50))
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Go Back")
            }
        }
        return
    }

    // --- Host Verification Logic ---
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val isHost = currentEvent.hostId == currentUserId

    val dateFormatter = DateTimeFormatter.ofPattern("MMM. d, yyyy")

    // Values pulled directly from ViewModel/Firestore
    val attending = currentEvent.attending
    val maybe = currentEvent.maybe
    val declined = currentEvent.declined

    val claimedItems = currentEvent.eventItems.filter { it.boughtBy != null }
    val unclaimedItems = currentEvent.eventItems.filter { it.boughtBy == null }

    val attendingCount = attending.size
    val maybeCount = maybe.size
    val declinedCount = declined.size
    val totalCount = attendingCount + maybeCount + declinedCount

    val itemCount = claimedItems.size + unclaimedItems.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)

    ) {
        Spacer(modifier = Modifier.height(40.dp)) // <-- Pushes the whole screen down!

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onBackClick() }
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text("Back")
        }

        Spacer(modifier = Modifier.height(4.dp))

        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "- ${currentEvent.name} -",
            fontSize = 26.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // FIX: Added safe null-handling for the date format block to prevent runtime crashes if date is null
        val displayDate = currentEvent.date?.let { it.format(dateFormatter) } ?: "Date TBD"

        Text(
            text = "$displayDate, ${currentEvent.time}",
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = currentEvent.address,
            fontSize = 14.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // --- Group Dietary Summary Box ---
        GroupDietarySummaryBox(summary = groupSummary)

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(2.dp, color = Color.Black, RoundedCornerShape((6.dp)))
                .background(Color.White)
                .padding(12.dp)
        ) {
            // Provide a fallback text if the summary is completely empty
            Text(
                text = currentEvent.summary.ifBlank { "No event details provided." },
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Attendance/Roll Call
            Column(modifier = Modifier.weight(1f)) {
                Text("Party: $totalCount", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.Black
                )

                // --- Invite More People Button (Host Only) ---
                if (isHost) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(context, InviteFriendsActivity::class.java)
                            intent.putExtra("EVENT_ID", eventId)
                            intent.putExtra("IS_EDIT_MODE", true)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.padding(bottom = 12.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF6C5BB7))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF6C5BB7), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Invite More", color = Color(0xFF6C5BB7), fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))


                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text("Attending - $attendingCount", fontWeight = FontWeight.Bold)

                    IconButton(
                        // --- RSVP Confirmation ---
                        onClick = {
                            viewModel.updateAttendance(eventId, "attending")
                            Toast.makeText(context, "RSVP: Attending!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .background(Color(0xFF6C5BB7), RoundedCornerShape(50))
                            .size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Attending",
                            tint = Color.White
                        )
                    }
                }
                attending.forEach {
                    Text("• $it")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text("Maybe - $maybeCount     ", fontWeight = FontWeight.Bold)

                    IconButton(
                        // --- RSVP Confirmation ---
                        onClick = {
                            viewModel.updateAttendance(eventId, "maybe")
                            Toast.makeText(context, "RSVP: Maybe", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .background(Color(0xFFFFB74D), RoundedCornerShape(50))
                            .size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Maybe",
                            tint = Color.White
                        )
                    }
                }
                maybe.forEach {
                    Text("• $it")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text("Declined - $declinedCount  ", fontWeight = FontWeight.Bold)

                    IconButton(
                        // --- RSVP Confirmation ---
                        onClick = {
                            viewModel.updateAttendance(eventId, "declined")
                            Toast.makeText(context, "RSVP: Declined", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .background(Color(0xFFE57373), RoundedCornerShape(50))
                            .size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Decline",
                            tint = Color.White
                        )
                    }
                }
                declined.forEach {
                    Text("• $it")
                }

            }


            Spacer(modifier = Modifier.width(60.dp))


            // Items Display
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Items: $itemCount", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Claimed Items
                Text("Claimed (${claimedItems.size})", fontWeight = FontWeight.Bold)

                claimedItems.forEach {
                    Text("☑ ${it.name}")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Unclaimed Items
                Text("Unclaimed (${unclaimedItems.size})", fontWeight = FontWeight.Bold)

                unclaimedItems.forEach {
                    Text("☐ ${it.name}")
                }
            }
        }
    }
}

// --- The Dietary Summary Box ---
@Composable
fun GroupDietarySummaryBox(summary: GroupDietarySummary) {
    if (summary.tallies.isEmpty() && summary.customNotes.isEmpty()) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3CD)),
        border = BorderStroke(1.dp, Color(0xFF856404))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "⚠️ Group Dietary Needs",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF856404)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Display Tallies
            summary.tallies.forEach { (allergy, count) ->
                // Basic cleanup: capitalize and add spaces (halal -> Halal, glutenFree -> Gluten Free)
                val displayName = allergy.replaceFirstChar { it.uppercase() }
                    .replace(Regex("([a-z])([A-Z])"), "$1 $2")

                Text(text = "• $count x $displayName", fontSize = 14.sp, color = Color.Black)
            }

            // Display Custom Notes
            if (summary.customNotes.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFD1C49D))
                Text(text = "Specific Notes:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                summary.customNotes.forEach { note ->
                    Text(text = "• $note", fontSize = 13.sp, color = Color.DarkGray)
                }
            }
        }
    }
}