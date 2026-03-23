package com.example.wepartyapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// The blueprint for our real notifications
data class PartyNotification(
    val id: String,
    val title: String,
    val message: String,
    val time: String,
    val timestamp: Long // Used to sort them from newest to oldest!
)

// 1. Create a blueprint for an event
data class PartyEvent(
    val id: String = "", // Added ID to uniquely identify events for chat
    val name: String,
    val time: String,
    val summary: String = "",
    val address: String,
    val date: LocalDate?,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val lastSenderId: String? = null,
    val readByUsers: Map<String, Long> = emptyMap(), // Tracks when each user last read the chat
    val hostId: String = "",
    val invitedGuests: List<String> = emptyList(),

    // - Guest Attendance -
    val attending: List<String> = emptyList(),
    val maybe: List<String> = emptyList(),
    val declined: List<String> = emptyList(),

    val eventItems: List<PartyItem> = emptyList()
)

data class PartyItem(
    val name: String,
    val price: String,
    val boughtBy: String? = null,
    val boughtByName: String? = null
)

// --- Chat Message Blueprint ---
// Andy Read This - Restored so the compiler knows what a ChatMessage is
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L
)

// --- Friend Blueprint ---
data class FriendProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val appUserId: String = ""
)

// --- Dietary Summary Blueprint ---
data class GroupDietarySummary(
    val tallies: Map<String, Int> = emptyMap(),
    val customNotes: List<String> = emptyList()
)

class EventViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 2. Holds a list of events
    private val _events = MutableLiveData<List<PartyEvent>>(emptyList())
    val events: LiveData<List<PartyEvent>> = _events

    // --- Notification State ---
    private val _notificationsList = MutableStateFlow<List<PartyNotification>>(emptyList())
    val notificationsList: StateFlow<List<PartyNotification>> = _notificationsList.asStateFlow()

    // --- Chat State ---
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // --- Friends State ---
    private val _friendsList = MutableStateFlow<List<FriendProfile>>(emptyList())
    val friendsList: StateFlow<List<FriendProfile>> = _friendsList.asStateFlow()

    private val _searchResults = MutableStateFlow<List<FriendProfile>>(emptyList())
    val searchResults: StateFlow<List<FriendProfile>> = _searchResults.asStateFlow()

    // State to hold incoming friend requests
    private val _friendRequests = MutableStateFlow<List<FriendProfile>>(emptyList())
    val friendRequests: StateFlow<List<FriendProfile>> = _friendRequests.asStateFlow()

    // State to hold Suggested Friends (Friends of Friends)
    private val _suggestedFriends = MutableStateFlow<List<FriendProfile>>(emptyList())
    val suggestedFriends: StateFlow<List<FriendProfile>> = _suggestedFriends.asStateFlow()

    // --- Group Dietary Summary State ---
    private val _groupDietarySummary = MutableStateFlow(GroupDietarySummary())
    val groupDietarySummary: StateFlow<GroupDietarySummary> = _groupDietarySummary.asStateFlow()

    // We cache the text fields here so they survive navigation between screens
    var eventId by mutableStateOf("") // Holds the pre-generated ID for FlowLinks
    var eventName by mutableStateOf("")
    var eventSummary by mutableStateOf("")
    var eventDate by mutableStateOf("")
    var eventTime by mutableStateOf("")
    var eventAddress by mutableStateOf("")
    var eventInvitedGuests by mutableStateOf<List<String>>(emptyList())

    //-list of PartyItems-
    private val _itemsList = MutableStateFlow<List<PartyItem>>(emptyList())
    val _items: StateFlow<List<PartyItem>> = _itemsList.asStateFlow()

    //-adds a PartyItem to our list-
    fun addItems(item: PartyItem) {
        _itemsList.update { currentList -> currentList + item }
    }

    //-clears the PartyItem list-
    fun clearItems() {
        _itemsList.value = emptyList()
    }

    //-updates the price of a PartyItem in the list-
    fun updatePrice(itemName: String, updatedPrice: String) {
        _itemsList.update { currentList ->
            currentList.map { item ->
                if(item.name == itemName) {
                    item.copy(price = updatedPrice)
                } else {
                    item
                }
            }
        }
    }

    //-removes a PartyItem from our local list before saving-
    fun removeItem(item: PartyItem) {
        _itemsList.update { currentList ->
            currentList.filter { it.name != item.name }
        }
    }

    init {
        // Pre-generate a Firebase ID for the very first event draft
        eventId = db.collection("events").document().id

        fetchEventsFromFirebase()
        fetchNotificationsFromFirebase() // <-- Starts listening for alerts immediately
    }

    private fun fetchEventsFromFirebase() {
        db.collection("events").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) {
                return@addSnapshotListener
            }

            val eventList = mutableListOf<PartyEvent>()

            // 3. Loop through every event in the database
            for (document in snapshot.documents) {
                val id = document.id
                val name = document.getString("name") ?: "Unknown Event"
                val time = document.getString("time") ?: "TBD"
                val summary = document.getString("summary") ?: ""
                val address = document.getString("address") ?: "TBD"
                val dateString = document.getString("date")
                val lastMsg = document.getString("lastMessage")
                val lastMsgTime = document.getLong("lastMessageTime")
                val lastSender = document.getString("lastSenderId")
                
                // Read the readByUsers map safely
                val readByUsersRaw = document.get("readByUsers") as? Map<String, Any> ?: emptyMap()
                val readByUsers = readByUsersRaw.mapValues { it.value as? Long ?: 0L }

                val fetchedHostId = document.getString("hostId") ?: ""
                val fetchedGuests = document.get("invitedGuests") as? List<String> ?: emptyList()

                val attending = document.get("attending") as? List<String> ?: emptyList()
                val maybe = document.get("maybe") as? List<String> ?: emptyList()
                val declined = document.get("declined") as? List<String> ?: emptyList()

                var date: LocalDate? = null
                if (!dateString.isNullOrEmpty()) {
                    try {
                        date = LocalDate.parse(dateString)
                    } catch (e: Exception) {
                        // ignore bad formatting
                    }
                }

                // Getting the array of maps from Firestore
                val arrayOfItems = document.get("items") as? List<Map<String, Any>>
                // Map the Firestore data to our local PartyItem model
                val eventItems = arrayOfItems?.map { map ->
                    PartyItem(
                        name = map["name"] as? String ?: "",
                        price = map["price"] as? String ?: "",
                        boughtBy = map["boughtBy"] as? String,
                        boughtByName = map["boughtByName"] as? String
                    )
                } ?: emptyList()

                // Add the event with its items and chat metadata to our local list
                eventList.add(
                    PartyEvent(
                        id,
                        name,
                        time,
                        summary,
                        address,
                        date,
                        lastMsg,
                        lastMsgTime,
                        lastSender,
                        readByUsers,
                        fetchedHostId,
                        fetchedGuests,
                        attending,
                        maybe,
                        declined,
                        eventItems
                    )
                )
            }

            // 4. Update the UI with the full list
            _events.value = eventList
        }
    }

    private fun fetchNotificationsFromFirebase() {
        // Only pull notifications where this specific user's ID is in the "allowedUsers" list
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            _notificationsList.value = emptyList()
            return
        }

        db.collection("notifications")
            .whereArrayContains("allowedUsers", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    return@addSnapshotListener
                }

                val alerts = mutableListOf<PartyNotification>()
                for (document in snapshot.documents) {
                    val id = document.id
                    val title = document.getString("title") ?: "Alert"
                    val message = document.getString("message") ?: ""
                    val timestamp = document.getLong("timestamp") ?: 0L
                    
                    // Calculate the real time difference (e.g. "5 minutes ago")
                    val calculatedTime = formatNotificationTime(timestamp)

                    alerts.add(PartyNotification(id, title, message, calculatedTime, timestamp))
                }
                // Sort notifications by newest first
                _notificationsList.value = alerts.sortedByDescending { it.timestamp }
            }
    }

    // --- Friends Functions ---

    // 1. Fetch both Friends and Pending Requests
    fun fetchFriends() {
        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("users").document(currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val friendUids = snapshot.get("friends") as? List<String> ?: emptyList()
                val requestUids = snapshot.get("friendRequests") as? List<String> ?: emptyList()

                // Fetch Friends Profiles
                if (friendUids.isNotEmpty()) {
                    db.collection("users").whereIn("uid", friendUids)
                        .get()
                        .addOnSuccessListener { friendDocs ->
                            _friendsList.value = friendDocs.documents.map { doc ->
                                FriendProfile(
                                    uid = doc.getString("uid") ?: "",
                                    name = doc.getString("name") ?: "Unknown",
                                    email = doc.getString("email") ?: "",
                                    appUserId = doc.getString("appUserId") ?: ""
                                )
                            }

                            // Trigger the Friends of Friends algorithm
                            fetchSuggestedFriends(friendUids, currentUserId)
                        }
                } else {
                    _friendsList.value = emptyList()
                    _suggestedFriends.value = emptyList() // Clear suggestions if no friends
                }

                // Fetch Friend Request Profiles
                if (requestUids.isNotEmpty()) {
                    db.collection("users").whereIn("uid", requestUids)
                        .get()
                        .addOnSuccessListener { reqDocs ->
                            _friendRequests.value = reqDocs.documents.map { doc ->
                                FriendProfile(
                                    uid = doc.getString("uid") ?: "",
                                    name = doc.getString("name") ?: "Unknown",
                                    email = doc.getString("email") ?: "",
                                    appUserId = doc.getString("appUserId") ?: ""
                                )
                            }
                        }
                } else {
                    _friendRequests.value = emptyList()
                }
            }
    }

    // --- Friends of Friends Algorithm ---
    private fun fetchSuggestedFriends(myFriendUids: List<String>, currentUserId: String) {
        if (myFriendUids.isEmpty()) return

        // Look at all of our friends' documents to see who they are friends with
        db.collection("users").whereIn("uid", myFriendUids).get().addOnSuccessListener { friendsDocs ->
            val friendsOfFriendsUids = mutableSetOf<String>()

            for (doc in friendsDocs.documents) {
                val theirFriends = doc.get("friends") as? List<String> ?: emptyList()
                friendsOfFriendsUids.addAll(theirFriends)
            }

            // Remove myself, my current friends, and people who already sent me a request
            friendsOfFriendsUids.remove(currentUserId)
            friendsOfFriendsUids.removeAll(myFriendUids.toSet())
            friendsOfFriendsUids.removeAll(_friendRequests.value.map { it.uid }.toSet())

            if (friendsOfFriendsUids.isEmpty()) {
                _suggestedFriends.value = emptyList()
                return@addOnSuccessListener
            }

            // Grab the top 10 suggestions.
            val topSuggestions = friendsOfFriendsUids.take(10)

            db.collection("users").whereIn("uid", topSuggestions).get().addOnSuccessListener { suggDocs ->
                _suggestedFriends.value = suggDocs.documents.map { doc ->
                    FriendProfile(
                        uid = doc.getString("uid") ?: "",
                        name = doc.getString("name") ?: "Unknown",
                        email = doc.getString("email") ?: "",
                        appUserId = doc.getString("appUserId") ?: ""
                    )
                }
            }
        }
    }

    // --- Case-Insensitive Search ---
    fun searchUsers(query: String) {
        val queryText = query.trim()
        if (queryText.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }

        val currentUserId = auth.currentUser?.uid ?: ""

        // Force whatever they type into the search bar to be lowercase
        val lowerCaseQuery = queryText.lowercase()

        // Launch queries against the standard fields
        val emailQuery = db.collection("users").whereEqualTo("email", lowerCaseQuery).get()
        val phoneQuery = db.collection("users").whereEqualTo("phoneNumber", queryText).get()
        val appUserIdQuery = db.collection("users").whereEqualTo("appUserId", lowerCaseQuery).get()

        // Wait for all 3 queries to finish, then merge their results into one list
        Tasks.whenAllSuccess<com.google.firebase.firestore.QuerySnapshot>(emailQuery, phoneQuery, appUserIdQuery)
            .addOnSuccessListener { snapshots ->
                val resultsMap = mutableMapOf<String, FriendProfile>() // Map prevents duplicates

                for (snapshot in snapshots) {
                    for (doc in snapshot.documents) {
                        val uid = doc.getString("uid") ?: continue

                        // Don't show the current user in their own search results
                        if (uid == currentUserId) continue

                        resultsMap[uid] = FriendProfile(
                            uid = uid,
                            name = doc.getString("name") ?: "Unknown",
                            email = doc.getString("email") ?: "",
                            appUserId = doc.getString("appUserId") ?: ""
                        )
                    }
                }
                _searchResults.value = resultsMap.values.toList()
            }
            .addOnFailureListener {
                _searchResults.value = emptyList()
            }
    }

    // 3. Send a Request
    fun sendFriendRequest(targetUid: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val currentUserName = auth.currentUser?.displayName ?: "Someone"

        // Use SetOptions.merge() to prevent crashes if the user document is missing
        db.collection("users").document(targetUid)
            .set(mapOf("friendRequests" to FieldValue.arrayUnion(currentUserId)), com.google.firebase.firestore.SetOptions.merge())

        // --- New: Friend request notifications ---
        sendAppNotification(
            title = "New Friend Request",
            message = "$currentUserName sent you a friend request!",
            allowedUsers = listOf(targetUid)
        )
    }

    // 4. Accept Request (Adds them to your friends, adds you to their friends, removes request)
    fun acceptFriendRequest(requesterUid: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        // Add them to your friends & clear the request
        db.collection("users").document(currentUserId)
            .set(mapOf(
                "friends" to FieldValue.arrayUnion(requesterUid),
                "friendRequests" to FieldValue.arrayRemove(requesterUid)
            ), com.google.firebase.firestore.SetOptions.merge())

        // Add you to their friends so it's mutual
        db.collection("users").document(requesterUid)
            .set(mapOf(
                "friends" to FieldValue.arrayUnion(currentUserId)
            ), com.google.firebase.firestore.SetOptions.merge())
    }

    // 5. Decline Request
    fun declineFriendRequest(requesterUid: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        db.collection("users").document(currentUserId)
            .set(mapOf(
                "friendRequests" to FieldValue.arrayRemove(requesterUid)
            ), com.google.firebase.firestore.SetOptions.merge())
    }

    // 6. Remove an existing friend
    fun removeFriend(friendUid: String) {
        val currentUserId = auth.currentUser?.uid ?: return

        // Remove from your list
        db.collection("users").document(currentUserId)
            .update("friends", FieldValue.arrayRemove(friendUid))

        // Remove from their list
        db.collection("users").document(friendUid)
            .update("friends", FieldValue.arrayRemove(currentUserId))
    }

    // --- Chat Functions ---

    // Sets up a real-time listener for messages within a specific event
    fun listenToMessages(eventId: String) {
        db.collection("events").document(eventId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val msgs = snapshot.documents.map { doc ->
                    ChatMessage(
                        id = doc.id,
                        senderId = doc.getString("senderId") ?: "",
                        senderName = doc.getString("senderName") ?: "Anonymous",
                        text = doc.getString("text") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                }
                _messages.value = msgs
            }
    }

    // Sends a new message and updates the event's "last message" snippet
    fun sendMessage(eventId: String, text: String) {
        val user = auth.currentUser ?: return
        val currentUserName = user.displayName ?: "User"
        val messageData = hashMapOf(
            "senderId" to user.uid,
            "senderName" to currentUserName,
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("events").document(eventId).collection("messages").add(messageData)
            .addOnSuccessListener {
                // Sync the last message back to the event document for the inbox preview
                db.collection("events").document(eventId).get().addOnSuccessListener { doc ->
                    val eventName = doc.getString("name") ?: "a party"
                    val hostId = doc.getString("hostId") ?: ""
                    val guests = doc.get("invitedGuests") as? List<String> ?: emptyList()
                    
                    // Notify everyone except the sender
                    val recipients = (guests + hostId).distinct().filter { it != user.uid }
                    
                    if (recipients.isNotEmpty()) {
                        sendAppNotification(
                            title = "New Message in $eventName",
                            message = "$currentUserName: $text",
                            allowedUsers = recipients
                        )
                    }

                    db.collection("events").document(eventId).update(
                        mapOf(
                            "lastMessage" to text,
                            "lastMessageTime" to messageData["timestamp"],
                            "lastSenderId" to user.uid,
                            "readByUsers.${user.uid}" to messageData["timestamp"] // Mark as read for sender
                        )
                    )
                }
            }
    }

    // Marks an event's chat as read for the current user
    fun markEventAsRead(eventId: String) {
        val user = auth.currentUser ?: return
        db.collection("events").document(eventId).update(
            "readByUsers.${user.uid}", System.currentTimeMillis()
        )
    }

    // Pushes the locally cached event data to Firestore
    fun saveEventData() {
        val currentUserId = auth.currentUser?.uid ?: ""
        
        // Convert custom PartyItem objects to simple maps for Firestore
        val mappedItems = _itemsList.value.map {
            mapOf("name" to it.name, "price" to it.price)
        }

        val eventMap = hashMapOf(
            "name" to eventName,
            "summary" to eventSummary,
            "time" to eventTime,
            "address" to eventAddress,
            "date" to eventDate,
            "items" to mappedItems,
            "lastMessage" to null,
            "lastMessageTime" to null,
            "lastSenderId" to null,
            "readByUsers" to mapOf(currentUserId to System.currentTimeMillis()), // Mark as read for creator
            "hostId" to currentUserId,
            "invitedGuests" to eventInvitedGuests, // <-- Updated to save selected friends!
            "attending" to emptyList<String>(),
            "maybe" to emptyList<String>(),
            "declined" to emptyList<String>()
        )

        // Use .document(eventId).set() instead of .add() to guarantee the ID matches the deep link
        db.collection("events").document(eventId).set(eventMap).addOnSuccessListener {
            // Formatting the date for the notification text
            var displayDate = eventDate
            try {
                if (eventDate.isNotBlank()) {
                    val parsedDate = LocalDate.parse(eventDate)
                    val formatter = DateTimeFormatter.ofPattern("MMM. d, yyyy")
                    displayDate = parsedDate.format(formatter)
                }
            } catch (e: Exception) {}

            // Send an alert only to the host (and guests once added)
            val allowedUsers = mutableListOf<String>()
            if (currentUserId.isNotEmpty()) allowedUsers.add(currentUserId)

            // --- New: Add all selected friends to the allowed users list so they get notified ---
            allowedUsers.addAll(eventInvitedGuests)

            sendAppNotification(
                title = "New Party Alert!",
                message = "$eventName is happening on $displayDate. Tap to see details!",
                allowedUsers = allowedUsers
            )

            // Clear the cache so the next event starts fresh
            eventName = ""
            eventSummary = ""
            eventDate = ""
            eventTime = ""
            eventAddress = ""
            _itemsList.value = emptyList()
            eventInvitedGuests = emptyList() // <-- Reset guest list for next party

            // Generate a fresh ID for the next party they create
            eventId = db.collection("events").document().id
        }
    }

    fun updateEventInfo(eventID: String) {
        db.collection("events").document(eventID).update("name", eventName)
        db.collection("events").document(eventID).update("summary", eventSummary)
        db.collection("events").document(eventID).update("date", eventDate)
        db.collection("events").document(eventID).update("time", eventTime)
        db.collection("events").document(eventID).update("address", eventAddress)
        //clear fields out after the update
        eventName = ""
        eventSummary = ""
        eventDate = ""
        eventTime = ""
        eventAddress = ""
    }

    // Updates the checklist items for an existing event
    fun updateEventItems(eventID: String) {
        val mappedItems = _itemsList.value.map {
            mapOf(
                "name" to it.name,
                "price" to it.price,
                "boughtBy" to it.boughtBy,
                "boughtByName" to it.boughtByName
            )
        }
        db.collection("events").document(eventID).update("items", mappedItems)
    }

    fun updateAttendance(eventId: String, status: String) {

        val user = auth.currentUser ?: return
        val userName = if (user.displayName.isNullOrBlank()) "User" else user.displayName!!

        val event = events.value?.find { it.id == eventId } ?: return

        val attending = event.attending.toMutableList()
        val maybe = event.maybe.toMutableList()
        val declined = event.declined.toMutableList()

        attending.remove(userName)
        maybe.remove(userName)
        declined.remove(userName)

        when (status) {
            "attending" -> attending.add(userName)
            "maybe" -> maybe.add(userName)
            "declined" -> declined.add(userName)
        }

        db.collection("events").document(eventId).update(
            mapOf(
                "attending" to attending,
                "maybe" to maybe,
                "declined" to declined
            )
        )
    }

    // Toggles the acquisition status of a party item (checks/unchecks)
    fun toggleItemCheck(eventId: String, item: PartyItem) {
        val user = auth.currentUser ?: return
        val currentUserName = user.displayName ?: "Someone"
        val event = events.value?.find { it.id == eventId } ?: return
        
        val updatedItems = event.eventItems.map {
            if (it.name == item.name) {
                // Claim the item if no one has it yet
                if (it.boughtBy == null) {
                    it.copy(boughtBy = user.uid, boughtByName = currentUserName)
                } 
                // Unclaim only if the current user is the one who bought it
                else if (it.boughtBy == user.uid) {
                    it.copy(boughtBy = null, boughtByName = null)
                } 
                else {
                    it
                }
            } else {
                it
            }
        }

        // Push the updated item array back to Firestore
        val mappedItems = updatedItems.map {
            mapOf(
                "name" to it.name,
                "price" to it.price,
                "boughtBy" to it.boughtBy,
                "boughtByName" to it.boughtByName
            )
        }
        db.collection("events").document(eventId).update("items", mappedItems).addOnSuccessListener {
            // --- Notification logic for claimed items ---
            val isNewlyClaimed = updatedItems.find { it.name == item.name }?.boughtBy == user.uid
            
            if (isNewlyClaimed) {
                val recipients = (event.invitedGuests + event.hostId).distinct().filter { it != user.uid }
                if (recipients.isNotEmpty()) {
                    sendAppNotification(
                        title = "Item Claimed!",
                        message = "$currentUserName picked up ${item.name} for ${event.name}!",
                        allowedUsers = recipients
                    )
                }
            }
        }
    }

    // --- Checklist Functions for Existing Events ---

    // Adds a single item to an existing event in Firestore
    fun addItemToExistingEvent(eventId: String, item: PartyItem) {
        val event = _events.value?.find { it.id == eventId } ?: return
        val updatedItems = event.eventItems + item
        
        val mappedItems = updatedItems.map {
            mapOf(
                "name" to it.name,
                "price" to it.price,
                "boughtBy" to it.boughtBy,
                "boughtByName" to it.boughtByName
            )
        }
        db.collection("events").document(eventId).update("items", mappedItems)
    }

    // Updates an item's price in Firestore (useful for live price lookup results)
    fun updateItemPriceInFirestore(eventId: String, itemName: String, newPrice: String) {
        val event = _events.value?.find { it.id == eventId } ?: return
        val updatedItems = event.eventItems.map {
            if (it.name == itemName) it.copy(price = newPrice) else it
        }

        val mappedItems = updatedItems.map {
            mapOf(
                "name" to it.name,
                "price" to it.price,
                "boughtBy" to it.boughtBy,
                "boughtByName" to it.boughtByName
            )
        }
        db.collection("events").document(eventId).update("items", mappedItems)
    }

    // --- Group Dietary Summary Logic ---
    fun fetchGroupDietarySummary(guestUids: List<String>) {
        if (guestUids.isEmpty()) {
            _groupDietarySummary.value = GroupDietarySummary()
            return
        }

        db.collection("users").whereIn("uid", guestUids).get()
            .addOnSuccessListener { snapshots ->
                val newTallies = mutableMapOf<String, Int>()
                val notes = mutableListOf<String>()

                val standardKeys = listOf(
                    "noOnions", "noKetchup", "noMushrooms", "extraMayo",
                    "glutenFree", "dairyFree", "nutAllergy", "shellfishAllergy",
                    "vegetarian", "vegan", "halal", "keto"
                )

                for (doc in snapshots.documents) {
                    val prefs = doc.get("dietaryPreferences") as? Map<String, Any> ?: continue

                    standardKeys.forEach { key ->
                        if (prefs[key] == true) {
                            newTallies[key] = (newTallies[key] ?: 0) + 1
                        }
                    }

                    val note = prefs["otherNotes"] as? String
                    if (!note.isNullOrBlank()) {
                        notes.add("${doc.getString("name") ?: "Guest"}: $note")
                    }
                }
                _groupDietarySummary.value = GroupDietarySummary(newTallies, notes)
            }
    }

    // --- Invite More People Button Logic ---
    fun inviteMoreGuests(eventId: String, newGuestList: List<String>) {
        if (eventId.isEmpty()) return

        // We use arrayUnion so we don't overwrite current guests
        db.collection("events").document(eventId)
            .update("invitedGuests", FieldValue.arrayUnion(*newGuestList.toTypedArray()))
            .addOnSuccessListener {
                // Send alert to new guests
                sendAppNotification(
                    title = "You're Invited!",
                    message = "You've been added to a party! Tap to see details.",
                    allowedUsers = newGuestList
                )
            }
    }

    // Internal helper to create a secure notification entry
    private fun sendAppNotification(title: String, message: String, allowedUsers: List<String>) {
        val notificationMap = hashMapOf(
            "title" to title,
            "message" to message,
            "time" to "Just now",
            "timestamp" to System.currentTimeMillis(),
            "allowedUsers" to allowedUsers
        )

        db.collection("notifications").add(notificationMap)
    }

    // --- Deletes an event completely and safely via its unique ID ---
    fun deleteEvent(event: PartyEvent) {
        if (event.id.isNotEmpty()) {
            db.collection("events").document(event.id).delete()
        }
    }

    // --- Removes a specific notification from a user's inbox ---
    fun dismissNotification(notificationId: String) {
        if (notificationId.isNotEmpty()) {
            db.collection("notifications").document(notificationId).delete()
        }
    }

    // Helper to turn timestamps into user-friendly text like "Yesterday" or "1 hour ago"
    private fun formatNotificationTime(timestamp: Long): String {
        if (timestamp == 0L) return "Just now"
        val now = System.currentTimeMillis()
        val diffMillis = now - timestamp
        val diffMinutes = diffMillis / (60 * 1000)
        val diffHours = diffMinutes / 60
        val diffDays = diffHours / 24
        return when {
            diffMinutes < 5 -> "Just now"
            diffMinutes < 60 -> "$diffMinutes minutes ago"
            diffHours < 24 -> if (diffHours == 1L) "1 hour ago" else "$diffHours hours ago"
            diffDays == 1L -> "Yesterday"
            else -> {
                val sdf = java.text.SimpleDateFormat("MMM. d, yyyy", java.util.Locale.getDefault())
                sdf.format(java.util.Date(timestamp))
            }
        }
    }

    // --- Account Deletion Logic with Re-authentication ---
    fun deleteUserAccount(password: String, onResult: (Boolean, String?) -> Unit) {
        val user = auth.currentUser
        val email = user?.email

        if (user == null || email == null) {
            onResult(false, "No user is currently logged in.")
            return
        }

        val uid = user.uid

        // 1. Re-authenticate the user first to prevent the "Recent Login Required" error
        val credential = EmailAuthProvider.getCredential(email, password)
        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                // 2. Auth successful! Delete their profile document from Firestore
                db.collection("users").document(uid).delete()
                    .addOnSuccessListener {
                        // 3. Once the database is clean, delete their actual Auth account
                        user.delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    onResult(true, null) // Success!
                                } else {
                                    onResult(false, task.exception?.localizedMessage)
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.localizedMessage ?: "Failed to delete user data from database.")
                    }
            } else {
                // Re-authentication failed
                onResult(false, "Incorrect password. Please try again.")
            }
        }
    }
}