package com.example.wepartyapp.ui.auth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.wepartyapp.R
import com.example.wepartyapp.ui.onboarding.OnboardingActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance() // <-- New: Firestore instance for uniqueness check

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        setContent {
            // Status bar appearance
            val view = LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as Activity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                }
            }

            // UI state
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }

            SignUpScreenUI(
                isLoading = isLoading, // Pass state down
                errorMessage = errorMessage, // Pass state down
                // --- Updated: Added appUserId and phone inputs ---
                onSignUpClick = { nameInput, appUserIdInput, phoneInput, emailInput, passwordInput ->
                    val name = nameInput.trim()

                    // Clean the handle and force it to lowercase instantly
                    val appUserId = appUserIdInput.trim().replace(" ", "").replace("@", "").lowercase()

                    val phone = phoneInput.trim()

                    // FORCE the email to lowercase instantly
                    val email = emailInput.trim().lowercase()

                    val password = passwordInput.trim()

                    // Reset error on new attempt
                    errorMessage = null

                    if (email.isEmpty() || password.isEmpty() || name.isEmpty() || appUserId.isEmpty()) {
                        errorMessage = "Please fill in all required fields."
                        return@SignUpScreenUI
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errorMessage = "Please enter a valid email."
                        return@SignUpScreenUI
                    }

                    if (password.length < 6) {
                        errorMessage = "Password must be at least 6 characters long."
                        return@SignUpScreenUI
                    }



                    // Lock the UI
                    isLoading = true

                    // Check if the completely lowercase UserID is already taken
                    db.collection("users").whereEqualTo("appUserId", appUserId).get()
                        .addOnSuccessListener { documents ->
                            if (!documents.isEmpty) {
                                isLoading = false
                                errorMessage = "That UserID is already taken. Please choose another."
                                return@addOnSuccessListener
                            }

                            // Proceed to create the Auth account.
                            auth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this) { task ->
                                    if (task.isSuccessful) {

                                        // Save name to Firebase
                                        val user = auth.currentUser
                                        val profileUpdates = UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build()

                                        user?.updateProfile(profileUpdates)?.addOnCompleteListener { profileTask ->

                                            // --- Database Save ---
                                            val userMap = hashMapOf(
                                                "uid" to user.uid,
                                                "name" to name, // Keeps its capitals
                                                "appUserId" to appUserId, // Saved as lowercase
                                                "phoneNumber" to phone,
                                                "email" to email, // Saved as lowercase
                                                "friends" to emptyList<String>(),
                                                "friendRequests" to emptyList<String>()
                                            )

                                            db.collection("users").document(user.uid)
                                                .set(userMap)
                                                .addOnSuccessListener {
                                                    startActivity(Intent(this, OnboardingActivity::class.java))
                                                    finish()
                                                }
                                        }

                                    } else {
                                        // Unlock the UI
                                        isLoading = false

                                        // --- Added: Human-readable error translations ---
                                        val exceptionMsg = task.exception?.message ?: ""
                                        errorMessage = when {
                                            exceptionMsg.contains("email address is already in use", ignoreCase = true) -> "An account with this email already exists."
                                            exceptionMsg.contains("network error", ignoreCase = true) -> "Network error. Please check your connection."
                                            exceptionMsg.contains("weak password", ignoreCase = true) -> "Password is too weak. Please use a stronger password."
                                            else -> "Sign Up failed. Please try again."
                                        }
                                    }
                                }
                        }
                        .addOnFailureListener {
                            isLoading = false
                            errorMessage = "Network error. Could not verify UserID availability."
                        }
                },
                onNavigateToLogin = {
                    finish()
                }
            )
        }
    }
}

// SignUp Screen UI
@Composable
fun SignUpScreenUI(
    isLoading: Boolean, // <-- Added
    errorMessage: String?, // <-- Added
    // --- Updated Signature to accept new fields ---
    onSignUpClick: (String, String, String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // State variables holding what the user types
    var name by remember { mutableStateOf("") }
    var appUserId by remember { mutableStateOf("") } // <-- New
    var phone by remember { mutableStateOf("") }     // <-- New
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current

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
                .size(120.dp) // Slightly reduced size to fit new fields on smaller screens
                .padding(bottom = 16.dp)
        )

        // Title
        Text(
            text = "Join the Party!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF4081),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Name field
        TextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Full Name") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )

        // --- New: UserID Field ---
        TextField(
            value = appUserId,
            onValueChange = { appUserId = it },
            placeholder = { Text("Unique UserID (@handle)") },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )

        // --- New: Phone Number Field ---
        TextField(
            value = phone,
            onValueChange = { phone = it },
            placeholder = { Text("Phone Number (Optional)") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )

        // Email field
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
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )

        // Password field
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
                    if (!isLoading) onSignUpClick(name, appUserId, phone, email, password)
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

        // Show error message if there is one
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Sign up button — shows spinner while loading
        Button(
            onClick = { onSignUpClick(name, appUserId, phone, email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Sign Up", color = Color.White)
            }
        }

        // Back to login
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