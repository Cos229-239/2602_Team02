package com.example.wepartyapp.ui.auth

import android.app.Activity // <-- Added
import android.content.Intent
import android.media.MediaPlayer // <-- Added for audio
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect // <-- Added to handle sound lifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember // <-- Added for remembering the MediaPlayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext // <-- Added to load the raw file
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.home.MainActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Kick Off FlowLinks Deep Link Check ---
        checkDeepLinks()

        setContent {
            // --- Status Bar Fix ---
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                }
            }

            SplashScreenUI(
                onTimeout = {
                    val currentUser = FirebaseAuth.getInstance().currentUser

                    if (currentUser != null) {
                        // If they are already logged in, send them straight to the Dashboard
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        // If they are not logged in, send them to the Login Screen
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                    finish() // Close the splash screen so they can't hit the back button to return to it
                }
            )
        }
    }

    // --- FlowLinks Deep Link Catcher Logic ---
    private fun checkDeepLinks() {
        val directData: Uri? = intent?.data
        if (directData != null) {
            // Extracts "12345" from a link like https://wepartyapp-8a3a7-flowlinks.web.app/12345
            val eventId = directData.lastPathSegment
            saveEventIdForRouting(eventId)
        }
    }

    private fun saveEventIdForRouting(eventId: String?) {
        if (eventId == null) return

        // Save the ID in SharedPreferences so MainActivity knows where to send them
        val prefs = getSharedPreferences("WePartyPrefs", MODE_PRIVATE)
        prefs.edit().putString("PENDING_INVITE_EVENT_ID", eventId).apply()
    }
}

@Composable
fun SplashScreenUI(onTimeout: () -> Unit) {
    val context = LocalContext.current // <-- Needed to load the sound file

    // --- Upgrade: Remember the MediaPlayer so the timer can talk to it ---
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.intro3) }

    DisposableEffect(Unit) {
        mediaPlayer?.start() // Play the sound immediately

        // When the splash screen finishes and closes, release the audio memory
        onDispose {
            mediaPlayer?.release()
        }
    }
    LaunchedEffect(key1 = true) {
        delay(3900)

        // --- Upgrade: Explicitly kill the audio before routing ---
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer.stop()
        }

        onTimeout()
    }

    // --- THE HOPPING ANIMATION ---
    val infiniteTransition = rememberInfiniteTransition(label = "hop")

    // 1. Side to Side (Horizontal)
    // Takes 900ms to go from Left to Right
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -40f, // Start Left
        targetValue = 40f,   // End Right
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hopX"
    )

    // 2. Up and Down (Vertical)
    // Takes 450ms to go Up.
    // Total time (Up + Down) = 900ms. Perfect sync!
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,   // Ground
        targetValue = -50f,  // Air (Peak of jump)
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hopY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
    ) {

        // CENTER COLUMN: Hopping Logo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "WeParty Logo",
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .offset(x = offsetX.dp, y = offsetY.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Plan. Party. Repeat.",
                color = Color(0xFF333333),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // BOTTOM COLUMN: Team Names
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            Text(
                text = "By: Rodney Ford\nLesly Morales\nMaret Rivera Merced\nAndy Tran\nHasani Carter",
                color = Color(0xFF333333),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}