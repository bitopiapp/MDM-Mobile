package com.bitopi.mdm

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("api/save-firebase-token")
    fun saveFirebaseToken(
        @Query("token") token: String,
        @Query("imei") imei: String
    ): Call<ApiResponse>
}

data class ApiResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null
)
