package com.example.echoplay.Services

import android.app.Activity
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.echoplay.Utils.AdHelper
import com.example.echoplay.Utils.AdHelper.showRandomTimeAds
import com.example.echoplay.Utils.Constants.CHANNEL_ID
import com.google.android.gms.ads.MobileAds
import java.lang.ref.WeakReference

class ApplicationClass : Application(), LifecycleObserver {

    companion object {
        private var currentActivity: WeakReference<Activity>? = null

        fun getCurrentActivity(): Activity? {
            return currentActivity?.get()
        }
    }

    override fun onCreate() {
        super.onCreate()

        MobileAds.initialize(this) { initializationStatus ->
            // Optional: handle initialization status
        }

      try {
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                val notificationChannel = NotificationChannel(CHANNEL_ID, "Now Playing Song", NotificationManager.IMPORTANCE_HIGH)
                notificationChannel.description = "Needed to Show Notification for Playing Song"
                val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(notificationChannel)
          }

      }catch (e: Exception) {
          Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
      }



        // Register lifecycle observer and activity lifecycle callbacks
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)

        // Preload the app open ad
        AdHelper.loadAppOpenAd(this)
    }

    private val activityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            currentActivity = WeakReference(activity)
        }

        override fun onActivityStarted(activity: Activity) {
            currentActivity = WeakReference(activity)
        }

        override fun onActivityResumed(activity: Activity) {
            currentActivity = WeakReference(activity)
        }

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {
            if (getCurrentActivity() == activity) {
                currentActivity = null
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        getCurrentActivity()?.let { activity ->
            // Show the app open ad when the app is opened
            if (showRandomTimeAds()) {
                AdHelper.showAppOpenAdIfAvailable(activity) { // Optional: handle what to do when the ad is dismissed }
                }

            }
        }
    }
}
