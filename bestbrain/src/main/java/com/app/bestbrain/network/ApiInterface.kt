package com.app.bestbrain.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiInterface {

    @Headers("Content-Type: application/json")
    @POST("auth/init")
    fun getSessionId(
        @Header("Bb-Api-Key") apiKey: String?,
        @Body body: RequestBody?
    ): Call<ResponseBody?>
}