package com.hferoze.android.flewr.fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hferoze.android.flewr.R;
import com.hferoze.android.flewr.adapter.FlewrRecyclerViewAdapter;
import com.hferoze.android.flewr.obj.PhotosInfo;
import com.hferoze.android.flewr.pojo.Api;
import com.hferoze.android.flewr.pojo.PhotosObject;
import com.hferoze.android.flewr.util.Utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class FlewrMainFragment extends Fragment {

    private static final String TAG = "FlewrMainFragment";
    private static final String DATA_SETTINGS_PKG = "com.android.settings";
    private static final String DATA_SETTINGS_CLASS = "com.android.settings.Settings$DataUsageSummaryActivity";
    private static final String API_URL = "https://api.flickr.com/";
    private static final String PHOTO_EXT= "jpg";
    private static final String PHOTOS_INFO_LIST = "photos_info_list";
    private static final String REQUEST_POSTED_KEY = "launch_activity_request";
    public static final int LAUNCH_WAIT = 3000;
    private static final int DATA_STATE_DISCONNECTED = 0;
    private static final int DATA_STATE_CONNECTED = 1;
    private static final String ALERT_CANCELLED_KEY = "alert_cancelled";

    private Context mContext;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private StaggeredGridLayoutManager mStaggeredGridLayoutManager;
    private ArrayList<PhotosInfo> mPhotosInfo;
    private FlewrRecyclerViewAdapter mFlewrAdapter;
    private RecyclerView mRecyclerView;

    private Retrofit mRetrofit;
    private Gson mGson;

    private Utils mUtils;

    private int mSpanCount=2;

    private RelativeLayout mSplashView;
    private ImageView mLogoImageView;

    private Dialog mAlert;
    private boolean mAlertCancelledState = false;

    private boolean mActivityLaunchPost = false;
    
    private boolean mDbLoadDone = false;

    public void FlewrMainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getActivity().getApplicationContext();
        mUtils = new Utils(mContext);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_flewr_main, container, false);

        //Setup data alert popup
        mAlert = new Dialog(getActivity());
        mAlert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mAlert.setContentView(R.layout.no_data_dialog);

        Button btnAlertCell = (Button) mAlert.findViewById(R.id.btn_alert_data_settings);
        btnAlertCell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchCellularDataSettings();
            }
        });
        Button btnAlertWifi = (Button) mAlert.findViewById(R.id.btn_alert_wifi_settings);
        btnAlertWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchWifiSettings();
            }
        });
        Button btnAlertCancel = (Button) mAlert.findViewById(R.id.btn_alert_cancel);
        btnAlertCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertCancelled();
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.main_swipe_refresh_layout);
        mSplashView = (RelativeLayout) rootView.findViewById(R.id.splash_view);
        mLogoImageView = (ImageView) rootView.findViewById(R.id.splashProgressImage);
        if (savedInstanceState == null
                || !savedInstanceState.containsKey(PHOTOS_INFO_LIST)
                || !savedInstanceState.containsKey(REQUEST_POSTED_KEY)) {
            mPhotosInfo = new ArrayList<>();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateLogo(mLogoImageView);
                }
            }, 500);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    clearSplash(mSplashView);
                }
            }, LAUNCH_WAIT);
        } else{
            mPhotosInfo = savedInstanceState.getParcelableArrayList(PHOTOS_INFO_LIST);
            mAlertCancelledState = savedInstanceState.getBoolean(ALERT_CANCELLED_KEY);
            if (savedInstanceState.getBoolean(REQUEST_POSTED_KEY)) {
                mSplashView.setVisibility(View.INVISIBLE);
            }
        }

        mGson = new GsonBuilder().create();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        //set number of columns based on orientation
        if (mUtils.getOrientation()
                == Configuration.ORIENTATION_LANDSCAPE) {
            mSpanCount = Integer.parseInt(mContext.getString(R.string.span_count_landscape));
        }else if (mUtils.getOrientation()
                == Configuration.ORIENTATION_PORTRAIT) {
            mSpanCount = Integer.parseInt(mContext.getString(R.string.span_count_portrait));
        }

       //staggered layout
        mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(mSpanCount, StaggeredGridLayoutManager.VERTICAL);
        mStaggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);

        mRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);

        mFlewrAdapter = new FlewrRecyclerViewAdapter(mContext, getActivity(), mPhotosInfo);

        mRecyclerView.setAdapter(mFlewrAdapter);
        mRecyclerView.setLayoutManager(mStaggeredGridLayoutManager);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimaryDark, R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshView();
            }
        });

        return rootView;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (mPhotosInfo.size()<1) {
            if (mUtils.isDataAvaialable()){
                collectPhotoInfoFromFlickr();
                mAlertCancelledState = false;
                if (mAlert != null && mAlert.isShowing())
                    mAlert.dismiss();

            }else {
                if (!mAlertCancelledState) {
                    dataAlert(DATA_STATE_DISCONNECTED);
                } else {
                    Toast.makeText(mContext, getResources().getString(R.string.data_unavailable_msg), Toast.LENGTH_SHORT).show();
                }
            }
        }else {
            mFlewrAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(PHOTOS_INFO_LIST, mPhotosInfo);
        outState.putBoolean(REQUEST_POSTED_KEY, mActivityLaunchPost);
        outState.putBoolean(ALERT_CANCELLED_KEY, mAlertCancelledState);
    }

    /*
    * Show Data Unavailable popup based on current data state
    * @param dataState: current data state
    */
    private void dataAlert(int dataState) {
        if (dataState == DATA_STATE_DISCONNECTED) {
            if (mAlert != null && !mAlert.isShowing()) {
                mAlert.show();
            }
        }
    }

    /*
    * Launch data settings intent
    */
    private void launchCellularDataSettings() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(
                DATA_SETTINGS_PKG,
                DATA_SETTINGS_CLASS));
        if (mUtils.isIntentSafe(intent))
            startActivity(intent);
    }

    /*
    * Launch Wifi Settings intent
    */
    private void launchWifiSettings() {
        Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
        if (mUtils.isIntentSafe(intent))
            startActivity(intent);
    }

    /*
    * Remember if user cancelled popup
    */
    private void alertCancelled() {
        mAlertCancelledState = true;
        if (mAlert != null && mAlert.isShowing()) {
            mAlert.dismiss();
        }
    }

    /*
     *   Collects photo information from Flickr using flickr.interestingness.getList API call.
     *   Makes use of Retrofit API Call
     */
    private void collectPhotoInfoFromFlickr(){
        final String URI_VOTE_METHOD = "method";
        final String URI_VOTE_METHOD_PARAM = "flickr.interestingness.getList";
        final String URI_PER_PAGE_CNT = "per_page";
        final String URI_PER_PAGE_CNT_PARAM = "500";
        final String URI_PAGE = "page";
        final String URI_PAGE_CNT = "1";
        final String URI_EXTRAS = "extras";
        final String URI_EXTRAS_PARAM = "views, owner_name";
        final String URI_FORMAT = "format";
        final String URI_FORMAT_PARAMS = "json";
        final String URI_FORMAT_CALLBACK = "nojsoncallback";
        final String URI_FORMAT_CALLBACK_PARAM = "1";
        final String URI_API_KEY = "api_key";

        LinkedHashMap<String, String> queryParams = new LinkedHashMap<>();
        queryParams.put(URI_VOTE_METHOD, URI_VOTE_METHOD_PARAM);
        queryParams.put(URI_PER_PAGE_CNT, URI_PER_PAGE_CNT_PARAM);
        queryParams.put(URI_PAGE, URI_PAGE_CNT);
        queryParams.put(URI_EXTRAS, URI_EXTRAS_PARAM);
        queryParams.put(URI_FORMAT, URI_FORMAT_PARAMS);
        queryParams.put(URI_FORMAT_CALLBACK, URI_FORMAT_CALLBACK_PARAM);
        queryParams.put(URI_API_KEY, mContext.getString(R.string.api_key));

        Api apiService =
                mRetrofit.create(Api.class);

        Call<PhotosObject> call = apiService.getFlickrInterestingPhotos(queryParams);

        call.enqueue(new Callback<PhotosObject>() {
            @Override
            public void onResponse(Response<PhotosObject> response, Retrofit retrofit) {
                int statusCode = response.code();

                Log.d(TAG, "StatusCode: " + statusCode + " Raw " + response.raw());
                try {
                    // Collect information from API response to create Photo link
                    for (int i = 0; i < response.body().getPhotos().getPhoto().size(); i++) {
                        String link = mUtils.getPhotoURL(
                                response.body().getPhotos().getPhoto().get(i).getFarm(),
                                response.body().getPhotos().getPhoto().get(i).getServer(),
                                response.body().getPhotos().getPhoto().get(i).getId(),
                                response.body().getPhotos().getPhoto().get(i).getSecret(),
                                mContext.getString(R.string.image_size_flag),
                                PHOTO_EXT);
                        // Collect information from API response to create owner icon link
                        String thumb = mUtils.getThumbnailURL(
                                response.body().getPhotos().getPhoto().get(i).getOwner(),
                                PHOTO_EXT
                        );

                        String ownerName = response.body().getPhotos().getPhoto().get(i).getOwnername(); //Owner name
                        String title = response.body().getPhotos().getPhoto().get(i).getTitle();        //Photo title
                        String views = response.body().getPhotos().getPhoto().get(i).getViews();        //Number of views
                        mPhotosInfo.add( new PhotosInfo(link, thumb, ownerName, title, views));
                    }

                    //Asynctask to save photo information in database
                    new UpdateDbTask().execute();
                    if (mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
                    mFlewrAdapter.notifyDataSetChanged();

                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.e(TAG, "Flickr Response fail: " + t.toString());
            }
        });
    }

    /*
     *   Save photo info into the database using Surag ORM
     */
    private class UpdateDbTask extends AsyncTask<PhotosInfo, Void, String> {
        protected String doInBackground(PhotosInfo...photosInfos) {
            mDbLoadDone = false;
            PhotosInfo.deleteAll(PhotosInfo.class);
            for (int i = 0 ; i < mPhotosInfo.size(); i++) {
                PhotosInfo photosInfo = mPhotosInfo.get(i);
                photosInfo.save();
            }

            return "Done";
        }

        protected void onPostExecute(String result) {
            if (result.equals("Done")){
                Log.d(TAG, "Update db done");
                mDbLoadDone = true;
            }
        }
    }

    /*
    * Clear Splash screen
    * @param splash: The splash view RelativeLayout
    */
    private void clearSplash(final RelativeLayout splash) {

        if (splash != null) {
            Log.d(TAG, "Clear splash");
            ObjectAnimator animX = ObjectAnimator.ofFloat(splash, "alpha", 1, 0);
            animX.setDuration(700);
            animX.start();
            animX.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    splash.setVisibility(View.INVISIBLE);
                    mActivityLaunchPost = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
        }
    }

    /*
    * Refresh view on swipe
    */
    public void refreshView(){
        if (mUtils.isDataAvaialable()){
            if (mDbLoadDone){
                collectPhotoInfoFromFlickr();
                mAlertCancelledState = false;
                if (mAlert != null && mAlert.isShowing())
                    mAlert.dismiss();
            }else{
                if (mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
            }
        }else {
            if (mSwipeRefreshLayout.isRefreshing()) mSwipeRefreshLayout.setRefreshing(false);
            if (!mAlertCancelledState) {
                dataAlert(DATA_STATE_DISCONNECTED);
            } else {
                Toast.makeText(mContext, getResources().getString(R.string.data_unavailable_msg), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Animate logo
     * @param iv : view to animate
     */
    private void animateLogo(View iv) {
        final int DURATION = 1000;
        ObjectAnimator animRotate = ObjectAnimator.ofFloat(iv, "rotation", 0, 10, 0, -15, 0);
        animRotate.setInterpolator(new LinearInterpolator());
        animRotate.setRepeatCount(20);
        animRotate.setDuration(DURATION);
        animRotate.start();
    }
}
