package com.juned.dicodingstoryapp.ui.widget.app

import android.content.Intent
import android.widget.RemoteViewsService

class StoriesWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory =
        StoriesRemoteViewsFactory(this.applicationContext)
}