package com.juned.dicodingstoryapp.ui.widget.app

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.juned.dicodingstoryapp.R
import com.juned.dicodingstoryapp.data.api.ApiConfig
import com.juned.dicodingstoryapp.data.api.response.StoryItem
import com.juned.dicodingstoryapp.data.pref.SessionPreferences
import com.juned.dicodingstoryapp.ui.view.home.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class StoriesRemoteViewsFactory(private val context: Context) :
    RemoteViewsService.RemoteViewsFactory {
    private val stories = ArrayList<StoryItem>()

    override fun onCreate() {
        //
    }

    override fun onDataSetChanged() {
        var token = ""
        val pref = SessionPreferences.getInstance(context.dataStore)
        CoroutineScope(Dispatchers.Main).launch {
            pref.getSavedToken().collect {
                token = it
            }
        }

        try {
            val listStories = ApiConfig.getApiService()
                .getAllStories(context.getString(R.string.auth, token))
                .execute()
                .body()
                ?.listStory as List<StoryItem>
            stories.clear()
            stories.addAll(listStories)
        } catch (e: Exception) {
            Log.e(TAG, "onResponse: ${e.message}")
            e.printStackTrace()
        }
        if(token=="" ||token.isEmpty()){
            stories.clear()
        }

    }

    override fun onDestroy() {
        //
    }

    override fun getCount() = stories.size

    override fun getViewAt(position: Int): RemoteViews {
        val rv = RemoteViews(context.packageName, R.layout.widget_story_item).apply {
            val image = Glide.with(context)
                .asBitmap()
                .load(stories[position].photoUrl)
                .submit()
                .get()

            setImageViewBitmap(R.id.story_image, image)
        }

        val extras = bundleOf(
            StoriesWidget.EXTRA_ITEM to stories[position].name
        )

        val fillIntent = Intent().apply {
            putExtras(extras)
        }

        rv.setOnClickFillInIntent(R.id.story_image, fillIntent)
        return rv
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount() = 1

    override fun getItemId(p0: Int) = 0L

    override fun hasStableIds() = false

    companion object {
        private const val TAG = "StoriesRemoteViewsFacto"
    }
}