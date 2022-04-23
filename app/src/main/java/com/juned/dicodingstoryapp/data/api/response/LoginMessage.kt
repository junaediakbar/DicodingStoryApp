package com.juned.dicodingstoryapp.data.api.response

import com.google.gson.annotations.SerializedName

data class LoginMessage(

    @field:SerializedName("name")
    val name: String,

    @field:SerializedName("userId")
    val userId: String,

    @field:SerializedName("token")
    val token: String
)