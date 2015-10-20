package com.hferoze.android.flewr.obj;

import android.os.Parcel;
import android.os.Parcelable;

import com.orm.SugarRecord;

public class PhotosInfo
        extends SugarRecord<PhotosInfo> implements Parcelable {

    private String mLink, mOwnerThumbLink, mOwnerName, mTitle, mViews;

    public PhotosInfo(){

    }

    public PhotosInfo(String link, String ownerThumbnailLink, String ownerName, String title, String views){
        this.mLink = link;
        this.mOwnerThumbLink = ownerThumbnailLink;
        this.mOwnerName = ownerName;
        this.mTitle = title;
        this.mViews = views;
    }

    public void setLink(String link){
        this.mLink = link;
    }

    public void setOwnerThumbLink(String ownerThumbLink){

        this.mOwnerThumbLink = ownerThumbLink;
    }

    public void setOwner(String ownerName){
        this.mOwnerName = ownerName;
    }

    public void setTitle(String title){
        this.mTitle = title;
    }

    public void setViews(String views){
        this.mViews = views;
    }

    public String getLink(){
        return this.mLink;
    }

    public String getOwnerThumbLink(){
        return this.mOwnerThumbLink;
    }

    public String getOwner(){
        return this.mOwnerName;
    }

    public String getTitle(){
        return this.mTitle;
    }

    public String getViews(){
        return this.mViews;
    }

    private PhotosInfo(Parcel in) {
        this.mLink = in.readString();
        this.mOwnerThumbLink = in.readString();
        this.mOwnerName = in.readString();
        this.mTitle = in.readString();
        this.mViews = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.mLink.toString());
        out.writeString(this.mOwnerThumbLink.toString());
        out.writeString(this.mOwnerName);
        out.writeString(this.mTitle);
        out.writeString(this.mViews);
    }

    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<PhotosInfo> CREATOR = new Parcelable.Creator<PhotosInfo>() {
        public PhotosInfo createFromParcel(Parcel in) {
            return new PhotosInfo(in);
        }

        public PhotosInfo[] newArray(int size) {
            return new PhotosInfo[size];
        }
    };
}
