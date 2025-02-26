package com.pmydm.projecterato

import android.app.Application
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle

class MyApplication : Application() {

    private lateinit var musicServiceIntent: Intent
    private var activityCount = 0
    private var screenOffReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()

        musicServiceIntent = Intent(applicationContext, MusicService::class.java)

        // Registrar ActivityLifecycleCallbacks
        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                if (activityCount == 0) {
                    startMusic() // Reanudar música cuando la app vuelve al primer plano
                }
                activityCount++
            }
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                activityCount--
                if (activityCount == 0) {
                    stopMusic() // Detener la música si la app va al fondo
                }
            }
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })

        // Register screen off receiver
        screenOffReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_SCREEN_OFF) {
                    stopMusic() // Detener la música cuando la pantalla se apaga
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)
    }

    private fun startMusic() {
        musicServiceIntent.putExtra("volume_state", true)
        startService(musicServiceIntent) // Reanudar música
    }

    private fun stopMusic() {
        musicServiceIntent.putExtra("volume_state", false)
        startService(musicServiceIntent) // Pausar música
    }

    override fun onTerminate() {
        super.onTerminate()
        // Unregister the receiver when the app is terminated
        if (screenOffReceiver != null) {
            unregisterReceiver(screenOffReceiver)
        }
    }
}
