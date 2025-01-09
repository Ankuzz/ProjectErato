package com.pmydm.projecterato

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FacebookSdk.setClientToken("@string/facebook_client_token")
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
    }
}
