package com.example.wepartyapp.ui.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WalmartPriceApi {

    @GET("/search.json")
    suspend fun getPrice(
        @Query("engine") engine : String,
        @Query("query") item : String,
        @Query("api_key") myApi : String
    ) : Response<WalmartPriceModel>
}