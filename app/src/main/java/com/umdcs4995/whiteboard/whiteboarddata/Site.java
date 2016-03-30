package com.umdcs4995.whiteboard.whiteboarddata;

/**
 * Created by LauraKrebs on 3/29/16.
 */

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.umdcs4995.whiteboard.BuildConfig;
import com.umdcs4995.whiteboard.R;

import java.io.Serializable;

public class Site implements Serializable {
    private static final long serialVersionUID = -2998341267277845644L;

    public int mSiteid;
    public String mName;
    public String mDomain;
    public String mTitle;
    public String mTheme; // change to enum?
    public boolean mPublic;
    public boolean mAnswers;
    public String mDescription;
    public boolean mStandardAuth;
    public String mSsoUrl;
    public boolean mPublicRegistration;
    public String mCustomDomain = "";
    public String mStoreUrl;
    public Image mLogo;

    public String mObjectNameSingular;
    public String mObjectNamePlural;
    public String mGoogleOAuth2Clientid;

    private boolean mBarcodeScanner = false;

    public Site(int siteid) {
        mSiteid = siteid;
    }

    public boolean search(String query) {
        if (mTitle.toLowerCase().contains(query)) {
            // Query is somewhere in title or name.
            return true;
        }

        /**
         * Compare edit distance with the length of the string. This is kinda
         * arbitrary but makes sense because we want more room for error the
         * longer the string and less room for error the shorter the string.
         */
        return mTitle.toLowerCase().contains(query);
    }

    public String getOpenIdLoginUrl() {
        return "https://" + getAPIDomain() + "/Guide/login/openid?host=";
    }

    public boolean checkForGoogleLogin() {
        // Google login is only supported for iFixit for now so we shouldn't
        // check for it or even initialize the GoogleApiClient on any other site.
        return isWhiteboard();
    }

    public boolean hasGoogleLogin() {
        // We can't support google login in the Dozuki app because the package name is
        // tied to a client id in the same project as the site's project.
        return !(App.isWhiteboardApp() || mGoogleOAuth2Clientid == null || mGoogleOAuth2Clientid.length() == 0);
    }

    /**
     * Returns the resourceid for the current site's object name.
     */
    public String getObjectName() {
        return mObjectNameSingular;
    }

    public String getObjectNamePlural() {
        return mObjectNamePlural;
    }

    public void setBarcodeScanner(boolean enabled) {
        mBarcodeScanner = enabled;
    }

    public boolean barcodeScanningEnabled() {
        return mBarcodeScanner;
    }

    /**
     * Returns true if the user should be automatically reauthenticated if their
     * auth token expires.
     */
    public boolean reauthenticateOnLogout() {
        return isWhiteboard();
    }

    public String getAPIDomain() {
        String domain;
        if (App.inDebug()) {
            if (isWhiteboard()) {
                domain = BuildConfig.DEV_SERVER;
            } else {
                domain = mName + "." + BuildConfig.DEV_SERVER;
            }
        } else {
            domain = mDomain;
        }

        return domain;
    }

    /**
     * Returns true if the provided host is for this Site.
     */
    public boolean hostMatches(String host) {
        return mDomain.equals(host) || mCustomDomain.equals(host);
    }

    public int theme() {
        // Put custom site themes here.


        return R.style.WhiteboardMainTheme;
    }


    // Used only for custom apps, where we don't have a call to get the site info.
    public static Site getSite(String siteName) {
        Site site = null;
        Resources res = App.get().getResources();

        if (siteName.equals("whiteboard")) {
            site = new Site(2);
            site.mName = "Whiteboard";
            site.mDomain = "www.ifixit.com";
            site.mTitle = "Whiteboard";
            site.mTheme = "custom";
            site.mPublic = true;
            site.mAnswers = true;
            site.mDescription = "iFixit is the free repair manual you can edit." +
                    " We sell tools, parts and upgrades for Apple Mac, iPod, iPhone," +
                    " iPad, and MacBook as well as game consoles.";
            site.mStandardAuth = true;
            site.mSsoUrl = null;
            site.mPublicRegistration = true;
        }

        return site;
    }

    @Override
    public String toString() {
        return "{" + mSiteid + " | " + mName + " | " + mDomain + " | " + mTitle +
                " | " + mTheme + " | " + mPublic + " | " + mDescription + " | " +
                mAnswers + " | " + mStandardAuth + " | " + mSsoUrl + " | " +
                mPublicRegistration + "}";
    }

    public boolean actionBarUsesIcon() {
        return isAccustream() || isWhiteboard() || isMagnolia() || isDripAssist() || isPVA() || isOscaro();
    }

    public boolean isOscaro() {
        return mName.equals("oscaro");
    }

    public boolean isPVA() {
        return mName.equals("pva");
    }

    public boolean isDripAssist() {
        return mName.equals("dripassist");
    }

    public boolean isAccustream() {
        return mName.equals("accustream");
    }

    public boolean isWhiteboard() {
        return mName.equals("whiteboard");
    }

    public Drawable getLogo() {
        return null;
    }

    public boolean isMagnolia() {
        return mName.equals("magnoliamedical");
    }
}
