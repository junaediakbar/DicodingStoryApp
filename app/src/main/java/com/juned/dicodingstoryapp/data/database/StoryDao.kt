package com.juned.dicodingstoryapp.data.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.juned.dicodingstoryapp.data.api.response.StoryItem


@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStories(stories: List<StoryItem>)

    @Query("SELECT * FROM stories")
    fun getAllStories(): PagingSource<Int, StoryItem>

    @Query("DELETE FROM stories")
    suspend fun deleteAllStories()
}