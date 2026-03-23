package com.example.wepartyapp.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.wepartyapp.ui.auth.LoginActivity
import com.example.wepartyapp.ui.EventViewModel
import com.example.wepartyapp.ui.FriendProfile
import com.example.wepartyapp.ui.onboarding.OnboardingActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File

@Composable
fun ProfileScreenUI(
    onEditDietaryClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onFriendsListClick: () -> Unit // <-- Added parameter for the friends list navigation
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    var userName by remember { mutableStateOf("Party Animal") }
    var profilePhotoUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        userName = user?.displayName?.takeIf { it.isNotBlank() } ?: "Party Animal"
        profilePhotoUri = user?.photoUrl
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log out") },
            text = { Text("Are you sure you want to sign out of the account?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        auth.signOut()

                        val intent = Intent(context, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }

                        context.startActivity(intent)
                    }
                ) {
                    Text("Log out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
    ) {
        // --- TOP AVATAR SECTION ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFB65C5C)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Profile Picture (Dynamically loads image OR shows placeholder)
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(3.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePhotoUri != null) {
                        AsyncImage(
                            model = profilePhotoUri,
                            contentDescription = "Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Placeholder",
                            modifier = Modifier.size(60.dp),
                            tint = Color.Gray
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = userName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- MENU OPTIONS SECTION ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            ProfileMenuRow(
                icon = Icons.Default.Settings,
                title = "Profile Settings",
                subtitle = "Update your name and profile picture",
                onClick = onEditProfileClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Friends List Menu Item ---
            ProfileMenuRow(
                icon = Icons.Default.Person,
                title = "Friends List",
                subtitle = "Manage your connections and find new friends",
                onClick = onFriendsListClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileMenuRow(
                icon = Icons.AutoMirrored.Filled.List,
                title = "Dietary Preferences",
                subtitle = "Manage your food allergies and preferences",
                onClick = onEditDietaryClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- App Tutorial Row ---
            ProfileMenuRow(
                icon = Icons.Default.PlayArrow,
                title = "App Tutorial",
                subtitle = "Replay the introductory walkthrough",
                onClick = {
                    context.startActivity(Intent(context, OnboardingActivity::class.java))
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            ProfileMenuRow(
                icon = Icons.Default.Close,
                title = "Log out",
                subtitle = "Sign out of your account",
                onClick = { showLogoutDialog = true }
            )
        }
    }
}

// --- Profile Settings ---
@Composable
fun ProfileSettingsScreenUI(
    viewModel: EventViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    // State for the text field, image, and loading status
    var nickname by remember { mutableStateOf(currentUser?.displayName ?: "") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(currentUser?.photoUrl) }
    var isUploading by remember { mutableStateOf(false) }

    // Fetch current phone number from Firestore when screen loads
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    phoneNumber = document.getString("phoneNumber") ?: ""
                }
        }
    }

    // States for the Camera/Gallery Dialog
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // 1. Gallery Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
        }
    }

    // 2. Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // If the camera successfully took the photo, update the UI with the temp file
            selectedImageUri = tempCameraUri
        }
    }

    // --- The Selection Dialog ---
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Profile Picture") },
            text = { Text("Where would you like to get your photo?") },
            confirmButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text("Gallery")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImageSourceDialog = false
                    // Create a secure temporary file, then launch the camera to fill it
                    tempCameraUri = context.createTempImageUri()
                    tempCameraUri?.let { cameraLauncher.launch(it) }
                }) {
                    Text("Camera")
                }
            }
        )
    }

    val saveProfileData = { finalPhotoUri: Uri? ->
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(nickname)

        if (finalPhotoUri != null) {
            profileUpdates.setPhotoUri(finalPhotoUri)
        }

        // Update the Auth Profile first
        currentUser?.updateProfile(profileUpdates.build())?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Also update the name and phone number in Firestore
                currentUser.uid.let { uid ->
                    val dbUpdates = mapOf(
                        "name" to nickname,
                        "phoneNumber" to phoneNumber
                    )
                    FirebaseFirestore.getInstance().collection("users").document(uid)
                        .update(dbUpdates)
                        .addOnCompleteListener { dbTask ->
                            isUploading = false
                            if (dbTask.isSuccessful) {
                                Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                Toast.makeText(context, "Update Failed: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                isUploading = false
                Toast.makeText(context, "Update Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Back Button
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
            IconButton(onClick = { if (!isUploading) onBack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.Black)
            }
        }

        Text("Edit Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB65C5C))
        Spacer(modifier = Modifier.height(32.dp))

        // Clickable Avatar for Upload
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(3.dp, Color(0xFFB65C5C), CircleShape)
                .clickable(enabled = !isUploading) {
                    // Trigger the dialog instead of instantly opening the gallery
                    showImageSourceDialog = true
                },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Add, contentDescription = "Upload", tint = Color.Gray, modifier = Modifier.size(40.dp))
                    Text("Upload", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Nickname Input
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            label = { Text("Display Nickname") },
            enabled = !isUploading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFB65C5C),
                focusedLabelColor = Color(0xFFB65C5C)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone Number Input
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            enabled = !isUploading,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFB65C5C),
                focusedLabelColor = Color(0xFFB65C5C)
            )
        )

        Spacer(modifier = Modifier.weight(1f))

        // Save Button
        Button(
            onClick = {
                // --- The "Nameless Ghost" Fix ---
                if (nickname.trim().isEmpty()) {
                    Toast.makeText(context, "Nickname cannot be empty!", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                if (isUploading) return@Button
                isUploading = true

                if (selectedImageUri != null && selectedImageUri.toString().startsWith("content://")) {

                    // --- Secure Firebase Path Updated Here ---
                    val storageRef = FirebaseStorage.getInstance().reference
                        .child("users/${currentUser?.uid}/profile_pic.jpg")

                    val inputStream = context.contentResolver.openInputStream(selectedImageUri!!)

                    if (inputStream != null) {
                        storageRef.putStream(inputStream)
                            .addOnSuccessListener {
                                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                    saveProfileData(downloadUri)
                                }
                            }
                            .addOnFailureListener { e ->
                                isUploading = false
                                Toast.makeText(context, "Upload Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        isUploading = false
                        Toast.makeText(context, "Could not read the selected image.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    saveProfileData(selectedImageUri)
                }
            },
            enabled = !isUploading,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4081)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Account Deletion Section ---
        DeleteAccountSection(viewModel = viewModel)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// --- Dedicated Composable for Account Deletion ---
@Composable
fun DeleteAccountSection(viewModel: EventViewModel) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") } // <-- State for the password
    var passwordVisible by remember { mutableStateOf(false) } // <-- Toggle State
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // The Trigger Button
        OutlinedButton(
            onClick = {
                password = ""
                passwordVisible = false
                showDeleteDialog = true
            },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD32F2F))
        ) {
            Text("Delete Account", fontWeight = FontWeight.Bold)
        }
    }

    // The Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = {
                Text(text = "Delete Account?", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
            },
            text = {
                Column {
                    Text(text = "Are you sure you want to permanently delete your WeParty account? This action cannot be undone and all your profile data will be lost.")
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Please enter your password to confirm:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    // --- Password Input Field ---
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                            val description = if (passwordVisible) "Hide password" else "Show password"

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isDeleting,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFD32F2F),
                            cursorColor = Color(0xFFD32F2F)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isDeleting = true
                        // Pass the typed password to the ViewModel
                        viewModel.deleteUserAccount(password) { success, errorMessage ->
                            isDeleting = false
                            if (success) {
                                showDeleteDialog = false
                                Toast.makeText(context, "Account deleted.", Toast.LENGTH_SHORT).show()

                                // Boot them back to the Login Screen
                                val intent = Intent(context, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                context.startActivity(intent)
                            } else {
                                // Show error but keep dialog open so they can re-type password
                                Toast.makeText(context, errorMessage ?: "Error deleting account", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    enabled = !isDeleting && password.isNotBlank()
                ) {
                    if (isDeleting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Yes, Delete", color = Color.White)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    enabled = !isDeleting
                ) {
                    Text("Cancel", color = Color.Black)
                }
            }
        )
    }
}

// --- Friends List Screen ---
@Composable
fun FriendsListScreenUI(
    viewModel: EventViewModel,
    onBack: () -> Unit
) {
    val friendsList by viewModel.friendsList.collectAsState()
    val friendRequests by viewModel.friendRequests.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val suggestedFriends by viewModel.suggestedFriends.collectAsState()

    // --- Safely grab the context once at the top of the Composable ---
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchFriends()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFE9EA))
            .padding(16.dp)
    ) {
        // --- Header ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Friends List",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB65C5C),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Search Bar ---
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { query ->
                searchQuery = query
                isSearching = query.isNotBlank()
                if (isSearching) {
                    // Convert to lowercase so the search is case-insensitive
                    viewModel.searchUsers(query.trim().lowercase())
                }
            },
            placeholder = { Text("Search by Email, Phone, or UserID...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            // --- The "Stuck Search" Fix ---
            trailingIcon = {
                if (isSearching) {
                    IconButton(onClick = {
                        searchQuery = ""
                        isSearching = false
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear Search")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFB65C5C),
                unfocusedBorderColor = Color.Gray
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- The "Nested Scrolling" Fix ---
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            if (isSearching) {
                item {
                    Text("Search Results", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    if (searchResults.isEmpty() && searchQuery.isNotBlank()) {
                        Text("No users found.", color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                    }
                }

                items(items = searchResults, key = { "search_${it.uid}" }) { user ->
                    val isAlreadyFriend = friendsList.any { it.uid == user.uid }

                    FriendRow(
                        friend = user,
                        actionIcon = if (isAlreadyFriend) null else Icons.Default.Add,
                        onActionClick = {
                            if (!isAlreadyFriend) {
                                viewModel.sendFriendRequest(user.uid)
                                Toast.makeText(context, "Request Sent!", Toast.LENGTH_SHORT).show()
                                searchQuery = ""
                                isSearching = false
                            }
                        }
                    )
                }
            } else {
                // --- 1. Pending Requests Section ---
                if (friendRequests.isNotEmpty()) {
                    item {
                        Text("Friend Requests (${friendRequests.size})", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    items(items = friendRequests, key = { "req_${it.uid}" }) { requester ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE57373)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(requester.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = requester.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                                    if (requester.appUserId.isNotBlank()) {
                                        Text(text = "@${requester.appUserId}", color = Color(0xFFB65C5C), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }

                                    Text(text = "Sent you a friend request", color = Color.Gray, fontSize = 12.sp)
                                }
                                // Decline Button
                                IconButton(onClick = { viewModel.declineFriendRequest(requester.uid) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Decline", tint = Color.Gray)
                                }
                                // Accept Button
                                IconButton(onClick = { viewModel.acceptFriendRequest(requester.uid) }) {
                                    Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color(0xFFB65C5C))
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // --- 2. Suggested Friends Section ---
                if (suggestedFriends.isNotEmpty()) {
                    item {
                        Text(
                            text = "Suggested Friends",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(items = suggestedFriends, key = { "sugg_${it.uid}" }) { suggested ->
                        FriendRow(
                            friend = suggested,
                            actionIcon = Icons.Default.Add,
                            onActionClick = {
                                viewModel.sendFriendRequest(suggested.uid)
                                Toast.makeText(context, "Request Sent!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }

                // --- 3. My Friends Section ---
                item {
                    Text("My Friends (${friendsList.size})", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

                    if (friendsList.isEmpty()) {
                        Text("You haven't added any friends yet. Use the search bar above to find them by email, phone, or UserID!", color = Color.Gray)
                    }
                }

                items(items = friendsList, key = { "friend_${it.uid}" }) { friend ->
                    FriendRow(
                        friend = friend,
                        actionIcon = Icons.Default.Delete,
                        onActionClick = { viewModel.removeFriend(friend.uid) }
                    )
                }
            }
        }
    }
}

// --- Reusable Component for a Single Friend Row ---
@Composable
fun FriendRow(
    friend: FriendProfile,
    actionIcon: ImageVector?,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE57373)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = friend.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                if (friend.appUserId.isNotBlank()) {
                    Text(text = "@${friend.appUserId}", color = Color(0xFFB65C5C), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }

                Text(text = friend.email, color = Color.Gray, fontSize = 12.sp)
            }

            if (actionIcon != null) {
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = "Action",
                        tint = if (actionIcon == Icons.Default.Delete) Color.Red else Color(0xFFB65C5C)
                    )
                }
            }
        }
    }
}

// Reusable component
@Composable
fun ProfileMenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE57373))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(text = subtitle, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}

// --- New: Helper function to securely generate a temporary file for the camera ---
fun Context.createTempImageUri(): Uri {
    val tempFile = File(this.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(
        this,
        "${this.packageName}.provider",
        tempFile
    )
}