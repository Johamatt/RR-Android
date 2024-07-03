package com.example.sport_geo_app.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds

import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdManager(private val context: Context) {

    private var rewardedAd: RewardedAd? = null
    private val TAG = "AdManager"

    init {
        // Initialize Mobile Ads SDK
        MobileAds.initialize(context) {}
        loadRewardedAd()
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()  // TODO replace google test ad unit ID
        RewardedAd.load(context, "ca-app-pub-3940256099942544/5224354917", adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                adError.toString().let { Log.d(TAG, it) }
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "Ad was loaded.")
                rewardedAd = ad
                setFullScreenContentCallback()
            }
        })
    }

    private fun setFullScreenContentCallback() {
        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                Log.d(TAG, "Ad was clicked.")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad dismissed fullscreen content.")
                rewardedAd = null
                loadRewardedAd() // Load a new ad
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                Log.e(TAG, "Ad failed to show fullscreen content.")
                rewardedAd = null
            }

            override fun onAdImpression() {
                Log.d(TAG, "Ad recorded an impression.")
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }
    }

    fun showRewardedAd(onUserEarnedReward: (RewardItem) -> Unit) {
        rewardedAd?.let { ad ->
            ad.show(context as Activity) { rewardItem: RewardItem ->
                onUserEarnedReward(rewardItem)
            }
        } ?: run {
            Toast.makeText(context, "Ad is not loaded yet", Toast.LENGTH_SHORT).show()
        }
    }
}
