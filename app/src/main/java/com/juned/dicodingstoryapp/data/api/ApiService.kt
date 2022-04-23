package com.juned.dicodingstoryapp.data.api

import com.juned.dicodingstoryapp.data.api.response.LoginResponse
import com.juned.dicodingstoryapp.data.api.response.MessageResponse
import com.juned.dicodingstoryapp.data.api.response.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("register")
    fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<MessageResponse>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @GET("stories")
    fun getAllStories(@Header("Authorization") token: String): Call<StoriesResponse>

    @Multipart
    @POST("stories")
    fun addStory(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Header("Authorization") auth: String
    ): Call<MessageResponse>

    companion object {
        const val PHOTO_FIELD = "photo"
    }
}