package com.juned.dicodingstoryapp.data.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.juned.dicodingstoryapp.data.api.response.StoryItem


@Database(entities = [StoryItem::class, RemoteKeyEntity::class], version = 1)
abstract class StoryDatabase : RoomDatabase() {
    abstract fun getStoryDao(): StoryDao
    abstract fun getRemoteKeyDao(): RemoteKeyDao

    companion object {
        @Volatile
        private var INSTANCE: StoryDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): StoryDatabase {
            return INSTANCE ?: Room.databaseBuilder(
                context.applicationContext,
                StoryDatabase::class.java, "story_database"
            ).build().also {
                INSTANCE = it
            }
        }
    }
}