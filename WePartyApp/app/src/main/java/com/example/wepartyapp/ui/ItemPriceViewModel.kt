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
import kotlinx.coroutines.launch

class ItemPriceViewModel : ViewModel(){                     //when we click on the add btn, it'll get data from the retrofit

    private val priceApi = RetrofitInstance.priceApi
    private val _priceResult = MutableLiveData<NetworkResponse<WalmartPriceModel>>()
    val priceResult : LiveData<NetworkResponse<WalmartPriceModel>> = _priceResult
    fun getData(item : String) {
        _priceResult.value = NetworkResponse.Loading
        viewModelScope.launch {
            try {
                val response = priceApi.getPrice(Constant.engine, item, Constant.apiKey)
                if (response.isSuccessful) {
                    response.body()?.let {
                        _priceResult.value = NetworkResponse.Success(it)
                    }
                } else {
                    _priceResult.value = NetworkResponse.Error("Failed to load data")
                }
            } catch (e: Exception) {
                _priceResult.value = NetworkResponse.Error("Failed to load data")
            }
        }
    }
}