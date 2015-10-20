package com.hferoze.android.flewr.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.widget.ImageView;

import com.hferoze.android.flewr.AppConstants;
import com.hferoze.android.flewr.R;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Random;

public class Utils {

    private Context mContext;

    private Typeface mFontTypeFace;


    public Utils(Context ctx) {
        mContext = ctx;
        mFontTypeFace = Typeface.createFromAsset(mContext.getAssets(), AppConstants.TYPE_FACE);
    }

    /*
    * Helper function to check if data is currently available
    */
    public boolean isDataAvaialable() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isDataConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (isDataConnected) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE
                    || activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /*
     * Set images using Picasso API
     * @param path : Url of the image
     * @param iv : ImageView where image would be placed
     */
    public void setImages(String path, final ImageView iv) {

        Drawable placeHolder = ContextCompat.getDrawable(mContext, R.drawable.placeholder);
        Drawable wrapDrawable = DrawableCompat.wrap(placeHolder);
        DrawableCompat.setTint(wrapDrawable, getRandomColor());

        Picasso.with(mContext).
                load(path).
                networkPolicy(isDataAvaialable() ? NetworkPolicy.NO_CACHE : NetworkPolicy.OFFLINE).
                placeholder(placeHolder).
                into(iv);

    }

    /*
    * Creates Flickr Url of photo
    */
    public String getPhotoURL(int farmId, String serverId, String imgId, String secret, String resolution, String format){
        Uri.Builder imgUri = new Uri.Builder();
        final String URI_SCHEME = "https";
        final String URI_AUTH = "farm"+farmId+"."+"staticflickr.com";
        final String URI_SERVER_ID = serverId;
        final String URI_IMG_ID = imgId+"_"+secret+"_"+resolution+"."+format;

        return imgUri.scheme(URI_SCHEME).
                appendPath(URI_AUTH).
                appendPath(URI_SERVER_ID).
                appendPath(URI_IMG_ID).build().toString();
    }

    /*
    * Creates Flickr Url of owner thumnail image
    */
    public String getThumbnailURL(String ownerId, String format){
        Uri.Builder thumbnailUri = new Uri.Builder();
        final String URI_SCHEME = "http";
        final String URI_AUTH = "flickr.com";
        final String URI_FOLDER = "buddyicons";
        final String URI_IMG_ID = ownerId+"."+format;

        return thumbnailUri.scheme(URI_SCHEME).
                appendPath(URI_AUTH).
                appendPath(URI_FOLDER).
                appendPath(URI_IMG_ID).build().toString();
    }

    public int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

    /*
    * Outputs a random color from colors.xml
    */
    public int getRandomColor(){

        String[] colorList  = mContext.getResources().getStringArray(R.array.colorsmore);
        return Color.parseColor(colorList[randInt(0,colorList.length-1)]);
    }

    public boolean isIntentSafe(Intent intent) {
        PackageManager packageManager = mContext.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        return activities.size() > 0;
    }

    /*
    * Helper function to get current orientation
    */
    public int getOrientation(){
        return mContext.getResources().getConfiguration().orientation;
    }

    public Typeface getTypeFace(){
        return mFontTypeFace;
    }
}
