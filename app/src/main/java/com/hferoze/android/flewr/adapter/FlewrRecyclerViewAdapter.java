package com.hferoze.android.flewr.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hferoze.android.flewr.AppConstants;
import com.hferoze.android.flewr.FlewrDetailActivity;
import com.hferoze.android.flewr.R;
import com.hferoze.android.flewr.obj.PhotosInfo;
import com.hferoze.android.flewr.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class FlewrRecyclerViewAdapter extends RecyclerView.Adapter<FlewrRecyclerViewAdapter.FlewrViewHolders> {

    private ArrayList<PhotosInfo> mPhotoList;
    private Context mContext;
    private Activity mActivity;
    private Utils mUtils;

    public FlewrRecyclerViewAdapter(Context context, Activity activity, ArrayList<PhotosInfo> photoList) {
        this.mPhotoList = photoList;
        this.mContext = context;
        mActivity = activity;
        mUtils = new Utils(mContext);
    }
    @Override
    public FlewrViewHolders onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.photos_list, null);

        FlewrViewHolders flewrViewHolders = new FlewrViewHolders(layoutView);
        return flewrViewHolders;
    }
    @Override
    public void onBindViewHolder(FlewrViewHolders holder, int position) {

        //Download images from Flickr and display
        mUtils.setImages(mPhotoList.get(position).getLink(), holder.mainPhotoImageView);

        mUtils.setImages(mPhotoList.get(position).getOwnerThumbLink(), holder.ownerThumbnailImageView);

        holder.ownerNameTextView.setSelected(true);
        holder.viewsCountTextView.setSelected(true);
        holder.photoTitleTextView.setSelected(true);

        holder.ownerNameTextView.setTypeface(mUtils.getTypeFace());
        holder.viewsCountTextView.setTypeface(mUtils.getTypeFace());
        holder.photoTitleTextView.setTypeface(mUtils.getTypeFace());

        holder.ownerNameTextView.setText(mPhotoList.get(position).getOwner());
        holder.viewsCountTextView.setText(mPhotoList.get(position).getViews());
        holder.photoTitleTextView.setText(mPhotoList.get(position).getTitle());

        Log.d("Flewr", "ownerName: " + mPhotoList.get(position).getOwner() + " views: " + mPhotoList.get(position).getViews());
    }

    @Override
    public int getItemCount() {
        return this.mPhotoList.size();
    }

    public class FlewrViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView mainPhotoImageView;
        public ImageView ownerThumbnailImageView;
        public TextView ownerNameTextView;
        public TextView viewsCountTextView;
        public TextView photoTitleTextView;

        public FlewrViewHolders(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mainPhotoImageView = (ImageView) itemView.findViewById(R.id.photos_imageView);
            ownerThumbnailImageView = (ImageView) itemView.findViewById(R.id.buddy_thumb_imageView);
            ownerNameTextView = (TextView) itemView.findViewById(R.id.owner_name_textView);
            viewsCountTextView = (TextView) itemView.findViewById(R.id.view_count_textView);
            photoTitleTextView = (TextView) itemView.findViewById(R.id.photo_title_textView);
        }

        @Override
        public void onClick(View view) {
            try {
                List<PhotosInfo> listAll = PhotosInfo.listAll(PhotosInfo.class);
    
                //Open Detail Fragment
                Intent intent = new Intent(mActivity, FlewrDetailActivity.class);
                intent.putExtra(AppConstants.PHOTO_LINK, listAll.get(getPosition()).getLink());
                intent.putExtra(AppConstants.OWNER_NAME, listAll.get(getPosition()).getOwner());
                intent.putExtra(AppConstants.OWNER_THUMB, listAll.get(getPosition()).getOwnerThumbLink());
                intent.putExtra(AppConstants.PHOTO_TITLE, listAll.get(getPosition()).getTitle());
                intent.putExtra(AppConstants.VIEWS_COUNT, listAll.get(getPosition()).getViews());
                mActivity.startActivity(intent);
            } catch (Exception e){
                e.printStackTrace();
            }

        }
    }
}
