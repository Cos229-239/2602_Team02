package com.example.wepartyapp.ui.auth

import android.app.Activity // <-- Added
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions // <-- Added
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction // <-- Added
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat // <-- Added
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.onboarding.OnboardingActivity // <-- Updated Import
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest // <-- NEW IMPORT NEEDED TO SAVE NAME

class SignUpActivity : ComponentActivity() {

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

            SignUpScreenUI(
                onSignUpClick = { name, email, password ->
                    if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
                        Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                        return@SignUpScreenUI
                    }

                    // Create User in Firebase
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {

                                // Save name to Firebase
                                val user = auth.currentUser
                                val profileUpdates = UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build()

                                user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->
                                    // Success: Route straight to the Onboarding Screen
                                    startActivity(Intent(this, OnboardingActivity::class.java))
                                    finish()
                                }
                                // -------------------------------------------

                            } else {
                                // Fail: Show error message
                                Toast.makeText(this, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                },
                onNavigateToLogin = {
                    finish() // Just close this screen to go back to Login
                }
            )
        }
    }
}

// SignUp Screen UI
@Composable
fun SignUpScreenUI(
    onSignUpClick: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // State variables holding what the user types
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current // <-- Controls moving between fields

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Logo
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "WeParty Logo",
            modifier = Modifier
                .size(180.dp)
                .padding(bottom = 16.dp)
        )

        // Title
        Text(
            text = "Join the Party!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF4081),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Name Field
        TextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Full Name") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
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

        // Email Field
        TextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Email Address") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
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

        // Password Field
        TextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onSignUpClick(name, email, password)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )

        // Sign Up Button
        Button(
            onClick = { onSignUpClick(name, email, password) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081))
        ) {
            Text("Sign Up", color = Color.White)
        }

        // Back to Login Link
        Text(
            text = "Already have an account? Log In",
            color = Color(0xFFFF4081),
            fontSize = 16.sp,
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable { onNavigateToLogin() }
        )
    }
}