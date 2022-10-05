package tech.apps.music.util

import android.app.Activity
import com.facebook.ads.Ad
import com.facebook.ads.AdError
import com.facebook.ads.InterstitialAd
import com.facebook.ads.InterstitialAdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import tech.apps.music.ui.HomeActivity

object AdsFunctions {
    var lastTimeForShowingAds: Long = 0L
    var toShowDirect: Boolean = false

    fun loadAds(context: Activity) {
        if (HomeActivity.rewardedInterstitialAd != null) {
            showGoogleAds(context)
            return
        }
        if (HomeActivity.interstitialAd != null) {
            showFacebookAds()
            return
        }
        loadGoogleAds(context)
        loadFacebookAds(context)
    }

    fun showAds(context: Activity) {
        if (HomeActivity.rewardedInterstitialAd != null) {
            showGoogleAds(context)
        } else if (HomeActivity.interstitialAd?.isAdLoaded == true) {
            showFacebookAds()
        } else {
            loadAds(context)
        }
    }

    private fun loadFacebookAds(context: Activity) {
        if ((lastTimeForShowingAds + 200000L) > System.currentTimeMillis())
            return

        lastTimeForShowingAds = System.currentTimeMillis()

        HomeActivity.interstitialAd = InterstitialAd(context, HomeActivity.PLACEMENT_ID)
        val interstitialAdListener: InterstitialAdListener = object : InterstitialAdListener {
            override fun onInterstitialDisplayed(ad: Ad) {}
            override fun onInterstitialDismissed(ad: Ad) {}

            override fun onError(ad: Ad?, adError: AdError) {
                lastTimeForShowingAds = 0L
            }

            override fun onAdLoaded(ad: Ad) {
                if (toShowDirect)
                    showFacebookAds()
            }

            override fun onAdClicked(ad: Ad) {}
            override fun onLoggingImpression(ad: Ad) {}
        }

        HomeActivity.interstitialAd?.loadAd(
            HomeActivity.interstitialAd?.buildLoadAdConfig()
                ?.withAdListener(interstitialAdListener)
                ?.build()
        )
    }

    private fun loadGoogleAds(context: Activity) {
        if ((lastTimeForShowingAds + 200000L) > System.currentTimeMillis())
            return

        RewardedInterstitialAd.load(context, HomeActivity.APP_ID,
            AdRequest.Builder().build(), object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) {
                    HomeActivity.rewardedInterstitialAd = ad
                    if (toShowDirect) {
                        showGoogleAds(context)
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    lastTimeForShowingAds = 0L
                }
            })
    }

    private fun showFacebookAds() {
        if (HomeActivity.interstitialAd == null || HomeActivity.interstitialAd?.isAdLoaded == false || HomeActivity.interstitialAd?.isAdInvalidated == true) {
            return
        }

        lastTimeForShowingAds = System.currentTimeMillis()
        HomeActivity.interstitialAd?.show()
        HomeActivity.interstitialAd = null
    }

    private fun showGoogleAds(context: Activity) {
        lastTimeForShowingAds = System.currentTimeMillis()

        HomeActivity.rewardedInterstitialAd?.show(context) {}

        HomeActivity.rewardedInterstitialAd = null
    }
}