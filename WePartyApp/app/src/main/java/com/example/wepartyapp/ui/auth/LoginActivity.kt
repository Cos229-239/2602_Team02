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
import androidx.compose.ui.platform.LocalContext
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
import com.example.wepartyapp.ui.home.MainActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    public override fun onStart() {
        super.onStart()
        auth = FirebaseAuth.getInstance()

        // If user already logged in, go straight to Main
        val currentUser = auth.currentUser
        if (currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

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

            LoginScreenUI(
                isLoading = isLoading,
                errorMessage = errorMessage,
                onLoginClick = { emailInput, passwordInput ->
                    val email = emailInput.trim()
                    val password = passwordInput.trim()

                    // Reset error on new attempt
                    errorMessage = null

                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Please enter both email and password."
                        return@LoginScreenUI
                    }

                    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        errorMessage = "Please enter a valid email."
                        return@LoginScreenUI
                    }

                    isLoading = true

                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { task ->
                            isLoading = false

                            if (task.isSuccessful) {
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                // Translate Firebase errors into readable messages
                                val exceptionMsg = task.exception?.message ?: ""
                                errorMessage = when {
                                    exceptionMsg.contains("INVALID_LOGIN_CREDENTIALS") -> "Incorrect email or password. Please try again."
                                    exceptionMsg.contains("network error", ignoreCase = true) -> "Network error. Please check your connection."
                                    exceptionMsg.contains("blocked", ignoreCase = true) -> "Account temporarily disabled due to too many failed attempts."
                                    else -> "Authentication failed. Please try again."
                                }
                            }
                        }
                },
                onNavigateToSignUp = {
                    startActivity(Intent(this, SignUpActivity::class.java))
                },
                onNavigateToForgotPassword = {
                    startActivity(Intent(this, PasswordRecoveryActivity::class.java))
                }
            )
        }
    }
}

@Composable
fun LoginScreenUI(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginClick: (String, String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
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
                .size(180.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Welcome Back!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF4081),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Email field
        TextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Email") },
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
                    if (!isLoading) onLoginClick(email, password)
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.White,
                focusedContainerColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent
            )
        )

        // Forgot password link
        Text(
            text = "Forgot password?",
            color = Color(0xFFFF4081),
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.End)
                .padding(bottom = 16.dp)
                .clickable { onNavigateToForgotPassword() }
        )

        // Show error if there is one
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Login button — shows spinner while loading
        Button(
            onClick = { onLoginClick(email, password) },
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
                Text("Log In", color = Color.White)
            }
        }

        Text(
            text = "New here? Create an Account",
            color = Color(0xFFFF4081),
            fontSize = 16.sp,
            modifier = Modifier
                .padding(top = 16.dp)
                .clickable { onNavigateToSignUp() }
        )
    }
}
