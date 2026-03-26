package com.example.wepartyapp.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wepartyapp.ui.api.Constant
import com.example.wepartyapp.ui.api.NetworkResponse
import com.example.wepartyapp.ui.api.RetrofitInstance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ItemPriceViewModel : ViewModel(){
    private val priceApi = RetrofitInstance.priceApi
    private val db = FirebaseFirestore.getInstance()
    private val _priceResult = MutableLiveData<NetworkResponse<Pair<String, String>>>()
    val priceResult : LiveData<NetworkResponse<Pair<String, String>>> = _priceResult

    fun getData(item : String) {
        _priceResult.value = NetworkResponse.Loading

        // 1. Normalize the item (Forces it to lowercase and removes accidental spaces)
        val normalizedItem = item.lowercase().trim()

        // 2. Check Firebase first using the lowercase name
        db.collection("item_prices").document(normalizedItem).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // It Exists. We Saved an API Call.
                    val cachedPrice = document.getString("price") ?: "Not Found"
                    // Return the original item name so the UI can match it, plus the price
                    _priceResult.value = NetworkResponse.Success(Pair(item, cachedPrice))
                } else {
                    // 3. Not in Firebase. We must hit the API.
                    viewModelScope.launch {
                        try {
                            // --- Force the API to search using the lowercase 'normalizedItem' ---
                            val response = priceApi.getPrice(Constant.engine, normalizedItem, Constant.apiKey)
                            if (response.isSuccessful) {
                                // Extract the exact price from the Walmart Model
                                val searchResults = response.body()?.organic_results
                                val exactPrice = if(!searchResults.isNullOrEmpty()) {
                                    "$${searchResults[0].primary_offer.offer_price}"
                                } else {
                                    "Not Found"
                                }
                                // --- The "Permanent Typo" Fix ---
                                // Only save it to Firebase if we actually found a price
                                if (exactPrice != "Not Found") {
                                    val priceMap = hashMapOf("price" to exactPrice)
                                    db.collection("item_prices").document(normalizedItem).set(priceMap)
                                }
                                // Return both the original item name and the newly fetched price
                                _priceResult.value = NetworkResponse.Success(Pair(item, exactPrice))

                            } else {
                                _priceResult.value = NetworkResponse.Error("Failed to load data")
                            }
                        } catch (e: Exception) {
                            Log.e("ItemPriceViewModel", "API Error: ${e.message}")
                            _priceResult.value = NetworkResponse.Error("Failed to load data")
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("ItemPriceViewModel", "Firestore Error: ${e.message}")
                _priceResult.value = NetworkResponse.Error("Database check failed")
            }
    }
}