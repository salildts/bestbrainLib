package com.app.bestbrain.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiInterface {

    @Headers("Content-Type: application/json")
    @POST("auth/init")
    fun getSessionId(
        @Header("Bb-Api-Key") apiKey: String?,
        @Body body: RequestBody?
    ): Call<ResponseBody?>

    @GET("chat/{appId}/sessions")
    fun getThreadList(
        @Header("Bb-Api-Key") apiKey: String?,
        @Path("appId") appId: String?,
        @Query("created_by_id") userId: Int?
    ): Call<ResponseBody?>

    @GET("chat/{appId}/threads/histories")
    fun getChatHistory(
        @Header("Bb-Api-Key") apiKey: String?,
        @Path("appId") appId: String?,
        @Query("session_id") sessionId: String?
    ): Call<ResponseBody?>

    @DELETE("chat/threads/{session_id}/destroy")
    fun deleteThread(
        @Header("Bb-Api-Key") apiKey: String?,
        @Path("session_id") sessionId: String?
    ): Call<ResponseBody?>
}