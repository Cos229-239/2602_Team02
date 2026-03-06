package com.example.wepartyapp.ui.auth

import android.app.Activity // <-- Added
import android.os.Bundle
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size // <-- Added
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions // <-- Added
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator // <-- Added
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect // <-- Added
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection // <-- Added
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager // <-- Added
import androidx.compose.ui.platform.LocalView // <-- Added
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction // <-- Added
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign // <-- Added for text alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat // <-- Added
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException // <-- Added back
import com.google.firebase.auth.FirebaseAuthInvalidUserException // <-- Added back

class PasswordRecoveryActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        setContent {
            // --- Status Bar Fix ---
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                }
            }

            // --- Added: State variables to control the UI ---
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            var successMessage by remember { mutableStateOf<String?>(null) } // New state for success

            PasswordRecoveryScreenUI(
                isLoading = isLoading,
                errorMessage = errorMessage,
                successMessage = successMessage,
                onResetClick = { emailInput ->
                    val cleanedEmail = emailInput.trim()

                    // Reset messages
                    errorMessage = null
                    successMessage = null

                    if (cleanedEmail.isEmpty()) {
                        errorMessage = "Please enter your email."
                        return@PasswordRecoveryScreenUI
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(cleanedEmail).matches()) {
                        errorMessage = "Please enter a valid email."
                        return@PasswordRecoveryScreenUI
                    }

                    // Lock UI
                    isLoading = true

                    auth.sendPasswordResetEmail(cleanedEmail)
                        .addOnCompleteListener { task ->
                            // Unlock UI
                            isLoading = false

                            if (task.isSuccessful) {
                                successMessage = "Reset email sent! Please check your inbox."
                            } else {
                                // The bulletproof way to check Firebase errors
                                val ex = task.exception
                                errorMessage = when (ex) {
                                    is FirebaseAuthInvalidUserException ->
                                        "No account found for that email."
                                    is FirebaseAuthInvalidCredentialsException ->
                                        "That email address is not valid."
                                    else -> {
                                        // Fallback check for network errors
                                        if (ex?.message?.contains("network error", ignoreCase = true) == true) {
                                            "Network error. Please check your connection."
                                        } else {
                                            "Error sending reset email. Please try again."
                                        }
                                    }
                                }
                            }
                        }
                },
                onBackToLogin = { finish() }
            )
        }
    }
}

@Composable
fun PasswordRecoveryScreenUI(
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onResetClick: (String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current // <-- Controls hiding the keyboard

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "Password Recovery",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF4081),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        TextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Enter your email") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done // Set keyboard to "Done"
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    if (!isLoading) onResetClick(email)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )

        // --- Added: In-UI Error Message Display ---
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        // --- Added: In-UI Success Message Display ---
        if (successMessage != null) {
            Text(
                text = successMessage,
                color = Color(0xFF4CAF50), // A nice, readable green
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        // --- Updated: Button with Loading State ---
        Button(
            onClick = {
                focusManager.clearFocus()
                onResetClick(email)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp), // Fixed height so it doesn't jump
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
            enabled = !isLoading // Disables the button while Firebase is working
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "Send Reset Email", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Back to Login",
            color = Color(0xFFFF4081),
            fontSize = 16.sp,
            modifier = Modifier.clickable { onBackToLogin() }
        )
    }
}