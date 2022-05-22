package com.juned.dicodingstoryapp.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("remote_keys")
data class RemoteKeyEntity (
    @PrimaryKey val id: String,
    val prev: Int?,
    val next: Int?
)