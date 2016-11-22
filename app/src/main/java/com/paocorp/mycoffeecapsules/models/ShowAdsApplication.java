package com.paocorp.mycoffeecapsules.models;


import android.app.Application;

public class ShowAdsApplication extends Application {

    private boolean hideAd;

    public boolean getHideAd() {
        return hideAd;
    }

    public void setHideAd(boolean bo) {
        hideAd = bo;
    }
}
