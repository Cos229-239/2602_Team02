package com.example.wepartyapp.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate

// 1. Create a blueprint for an event
data class PartyEvent(
    val name: String,
    val time: String,
    val address: String,
    val date: LocalDate?
)

class EventViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    // 2. Holds a list of events
    private val _events = MutableLiveData<List<PartyEvent>>(emptyList())
    val events: LiveData<List<PartyEvent>> = _events

    init {
        fetchEventsFromFirebase()
    }

    private fun fetchEventsFromFirebase() {
        db.collection("events").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || snapshot.isEmpty) {
                return@addSnapshotListener
            }

            val eventList = mutableListOf<PartyEvent>()

            // 3. Loop through every event in the database
            for (document in snapshot.documents) {
                val name = document.getString("name") ?: "Unknown Event"
                val time = document.getString("time") ?: "TBD"
                val address = document.getString("address") ?: "TBD"
                val dateString = document.getString("date")

                var date: LocalDate? = null
                if (!dateString.isNullOrEmpty()) {
                    try {
                        date = LocalDate.parse(dateString)
                    } catch (e: Exception) {
                        // ignore bad formatting
                    }
                }

                // Add it to our temporary list
                eventList.add(PartyEvent(name, time, address, date))
            }

            // 4. Update the UI with the full list
            _events.value = eventList
        }
    }

    fun saveEventData(name: String, time: String, address: String, date: String) {
        val eventMap = hashMapOf(
            "name" to name,
            "time" to time,
            "address" to address,
            "date" to date
        )
        db.collection("events").add(eventMap)
    }
}