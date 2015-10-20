package com.hferoze.android.flewr.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hferoze.android.flewr.AppConstants;
import com.hferoze.android.flewr.R;
import com.hferoze.android.flewr.util.Utils;

public class FlewrDetailFragment extends Fragment {

    private TextView mTitleTextView;
    private TextView mOwnerNameTextView;
    private TextView mViewsCountTextView;
    private ImageView mOwnerThumbImageView;
    private ImageView mPhotoImageView;

    private Context mContext;
    private Utils mUtils;

    public void FlewrDetailFragment() {
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
        final View rootView = inflater.inflate(R.layout.fragment_flewr_detail, container, false);

        rootView.setBackgroundColor(mUtils.getRandomColor());

        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle != null) {

            mTitleTextView = (TextView) rootView.findViewById(R.id.photo_title_textView_detail);
            mOwnerNameTextView = (TextView) rootView.findViewById(R.id.owner_name_textView_detail);
            mViewsCountTextView = (TextView) rootView.findViewById(R.id.view_count_textView_detail);
            mOwnerThumbImageView = (ImageView) rootView.findViewById(R.id.buddy_thumb_imageView_detail);
            mPhotoImageView = (ImageView) rootView.findViewById(R.id.photos_imageView_detail);

            if (mUtils.getOrientation() == Configuration.ORIENTATION_LANDSCAPE) {
                final ScrollView mainScrollView = (ScrollView) rootView.findViewById(R.id.mainScrollView);
                mainScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        mainScrollView.scrollTo(0, getActivity().getWindowManager().getDefaultDisplay().getHeight() / 2);
                    }
                });
            }

            mOwnerNameTextView.setSelected(true);
            mTitleTextView.setSelected(true);

            mOwnerNameTextView.setTypeface(mUtils.getTypeFace());
            mTitleTextView.setTypeface(mUtils.getTypeFace());
            mViewsCountTextView.setTypeface(mUtils.getTypeFace());

            mUtils.setImages(bundle.getString(AppConstants.PHOTO_LINK), mPhotoImageView);

            mPhotoImageView.setHorizontalFadingEdgeEnabled(true);
            mPhotoImageView.setFadingEdgeLength(40);

            mUtils.setImages(bundle.getString(AppConstants.OWNER_THUMB), mOwnerThumbImageView);
            mOwnerNameTextView.setText(bundle.getString(AppConstants.OWNER_NAME));
            mTitleTextView.setText(bundle.getString(AppConstants.PHOTO_TITLE));
            mViewsCountTextView.setText(bundle.getString(AppConstants.VIEWS_COUNT));
        }

        return rootView;
    }
}
