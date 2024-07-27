package com.example.echoplay.Utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd

object AdHelper {


    private var appOpenAd: AppOpenAd? = null
    private var isAdLoading: Boolean = false
    private const val AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921"


    // Load and display a banner ad
    fun loadBannerAd(context: Context, adView: AdView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                adView.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("AdMob", "Banner ad failed to load: ${adError.message}")
                adView.visibility = View.GONE
            }

            override fun onAdOpened() {
                // Code to execute when the ad is displayed
            }

            override fun onAdClicked() {
                // Code to execute when the user clicks on the ad
            }

            override fun onAdClosed() {
                // Code to execute when the ad is closed
            }
        }
    }



    // Load an app open ad
    fun loadAppOpenAd(context: Context, onAdLoaded: (() -> Unit)? = null) {
        if (isAdLoading || appOpenAd != null) return

        isAdLoading = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            AD_UNIT_ID,
            request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    isAdLoading = false
                    appOpenAd = ad
                    onAdLoaded?.invoke()
                    Log.d("AdHelper", "App open ad loaded")
                }

                override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                    isAdLoading = false
                    Log.e("AdHelper", "Failed to load app open ad: ${loadAdError.message}")
                }
            }
        )
    }

    // Show the app open ad
    fun showAppOpenAdIfAvailable(activity: Activity, onAdDismissed: (() -> Unit)? = null) {
        if (appOpenAd != null) {
            appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    onAdDismissed?.invoke()
                    loadAppOpenAd(activity) // Preload the next ad
                    Log.d("AdHelper", "App open ad dismissed")
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    appOpenAd = null
                    loadAppOpenAd(activity) // Preload the next ad
                    Log.e("AdHelper", "Failed to show app open ad: ${adError.message}")
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("AdHelper", "App open ad showed")
                }
            }
            appOpenAd?.show(activity)
        } else {
            loadAppOpenAd(activity) // Preload the ad if not available
            onAdDismissed?.invoke()
        }
    }

    fun showRandomTimeAds(): Boolean {
        val random = (1..10).random()
        if (random > 3) {
            return true
        }
        return false
    }

}
