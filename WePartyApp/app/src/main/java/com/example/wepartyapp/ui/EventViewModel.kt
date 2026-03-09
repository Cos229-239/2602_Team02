package com.example.wepartyapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
//import com.example.wepartyapp.ui.event_dashboard.ChatMessage
import com.google.firebase.auth.FirebaseAuth
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
    val address: String,
    val date: LocalDate?,
    val lastMessage: String? = null,
    val lastMessageTime: Long? = null,
    val lastSenderId: String? = null,
    val hostId: String = "",
    val invitedGuests: List<String> = emptyList(),
    val eventItems: List<PartyItem> = emptyList()
)

data class PartyItem(
    val name: String,
    val price: String,
    val boughtBy: String? = null,
    val boughtByName: String? = null
)

// --- Chat Message Blueprint ---
data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0L
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

    // We cache the text fields here so they survive navigation between screens
    var eventName by mutableStateOf("")
    var eventSummary by mutableStateOf("")
    var eventDate by mutableStateOf("")
    var eventTime by mutableStateOf("")
    var eventAddress by mutableStateOf("")

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

    init {
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
                val address = document.getString("address") ?: "TBD"
                val dateString = document.getString("date")
                val lastMsg = document.getString("lastMessage")
                val lastMsgTime = document.getLong("lastMessageTime")
                val lastSender = document.getString("lastSenderId")

                val fetchedHostId = document.getString("hostId") ?: ""
                val fetchedGuests = document.get("invitedGuests") as? List<String> ?: emptyList()

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
                eventList.add(PartyEvent(id, name, time, address, date, lastMsg, lastMsgTime, lastSender, fetchedHostId, fetchedGuests, eventItems))
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
        val messageData = hashMapOf(
            "senderId" to user.uid,
            "senderName" to (user.displayName ?: "User"),
            "text" to text,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("events").document(eventId).collection("messages").add(messageData)
            .addOnSuccessListener {
                // Sync the last message back to the event document for the inbox preview
                db.collection("events").document(eventId).update(
                    mapOf(
                        "lastMessage" to text,
                        "lastMessageTime" to messageData["timestamp"],
                        "lastSenderId" to user.uid
                    )
                )
            }
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
            "hostId" to currentUserId,
            "invitedGuests" to emptyList<String>()
        )

        db.collection("events").add(eventMap).addOnSuccessListener {
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
        }
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

    // Toggles the acquisition status of a party item (checks/unchecks)
    fun toggleItemCheck(eventId: String, item: PartyItem) {
        val user = auth.currentUser ?: return
        val updatedItems = events.value?.find { it.id == eventId }?.eventItems?.map {
            if (it.name == item.name) {
                // Claim the item if no one has it yet
                if (it.boughtBy == null) {
                    it.copy(boughtBy = user.uid, boughtByName = user.displayName ?: "User")
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
        } ?: return

        // Push the updated item array back to Firestore
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

    // Deletes an event from the Firestore database
    fun deleteEvent(event: PartyEvent) {
        val dateString = event.date?.toString() ?: ""
        db.collection("events")
            .whereEqualTo("name", event.name)
            .whereEqualTo("time", event.time)
            .whereEqualTo("date", dateString)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                }
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
}