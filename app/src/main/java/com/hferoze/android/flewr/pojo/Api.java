package com.hferoze.android.flewr.pojo;

import java.util.Map;

import retrofit.Call;
import retrofit.http.POST;
import retrofit.http.QueryMap;

public interface Api {

    @POST("services/rest/")
    Call<PhotosObject> getFlickrInterestingPhotos(
            @QueryMap Map<String, String> queryParams
    );
}
