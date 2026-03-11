package com.example.wepartyapp.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wepartyapp.ui.api.Constant
import com.example.wepartyapp.ui.api.NetworkResponse
import com.example.wepartyapp.ui.api.RetrofitInstance
import com.example.wepartyapp.ui.api.WalmartPriceModel
import com.google.firebase.firestore.FirebaseFirestore // <-- Added Firestore Import
import kotlinx.coroutines.launch

class ItemPriceViewModel : ViewModel(){                     //when we click on the add btn, it'll get data from the retrofit

    private val priceApi = RetrofitInstance.priceApi
    private val db = FirebaseFirestore.getInstance() // <-- Added Firestore Instance

    // Notice we changed the response to a String. The ViewModel now does the parsing for us.
    private val _priceResult = MutableLiveData<NetworkResponse<String>>()
    val priceResult : LiveData<NetworkResponse<String>> = _priceResult

    fun getData(item : String) {
        _priceResult.value = NetworkResponse.Loading

        // Normalize the item name so "Chips" and "chips" don't create duplicate database entries
        val normalizedItem = item.lowercase().trim()

        // 1. Check Firebase first
        db.collection("item_prices").document(normalizedItem).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // It Exists. We Saved an API Call.
                    val cachedPrice = document.getString("price") ?: "Not Found"
                    _priceResult.value = NetworkResponse.Success(cachedPrice)
                } else {
                    // 2. Not in Firebase. We must hit the API.
                    viewModelScope.launch {
                        try {
                            val response = priceApi.getPrice(Constant.engine, item, Constant.apiKey)
                            if (response.isSuccessful) {

                                // Extract the exact price from the Walmart Model
                                val searchResults = response.body()?.organic_results
                                val exactPrice = if(!searchResults.isNullOrEmpty()) {
                                    "$${searchResults[0].primary_offer.offer_price}"
                                } else {
                                    "Not Found"
                                }

                                // 3. Save it to Firebase so we never have to hit the API for this item again.
                                val priceMap = hashMapOf("price" to exactPrice)
                                db.collection("item_prices").document(normalizedItem).set(priceMap)

                                // Return the exact price string
                                _priceResult.value = NetworkResponse.Success(exactPrice)

                            } else {
                                _priceResult.value = NetworkResponse.Error("Failed to load data")
                            }
                        } catch (e: Exception) {
                            _priceResult.value = NetworkResponse.Error("Failed to load data")
                        }
                    }
                }
            }
            .addOnFailureListener {
                _priceResult.value = NetworkResponse.Error("Database check failed")
            }
    }
}