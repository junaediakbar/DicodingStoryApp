package com.juned.dicodingstoryapp.data.api

import com.juned.dicodingstoryapp.data.api.response.LoginResponse
import com.juned.dicodingstoryapp.data.api.response.GeneralResponse
import com.juned.dicodingstoryapp.data.api.response.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): GeneralResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("stories")
    suspend fun getAllStories(
        @Header("Authorization") token: String,
        @Query("location") location: Int = 1
    ): StoriesResponse

    @GET("stories")
    suspend fun getAllStoriesPaged(
        @Header("Authorization") token: String,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): StoriesResponse

    @Multipart
    @POST("stories")
    suspend fun addStory(
        @Part file: MultipartBody.Part,
        @PartMap params: HashMap<String, RequestBody>,
        @Header("Authorization") auth: String
    ): GeneralResponse

    companion object {
        const val PHOTO_FIELD = "photo"
    }
}