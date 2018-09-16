package com.orbigo.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class NearbyPoi implements Serializable,Parcelable{
    private Poi poiDetails;
    private String distance,time,cost;
    private boolean liked;

    public NearbyPoi() {
    }

    public NearbyPoi(Poi poiDetails, String distance, String time, String cost, boolean liked) {
        this.poiDetails = poiDetails;
        this.distance = distance;
        this.time = time;
        this.cost = cost;
        this.liked = liked;
    }

    protected NearbyPoi(Parcel in) {
        poiDetails = in.readParcelable(Poi.class.getClassLoader());
        distance = in.readString();
        time = in.readString();
        cost = in.readString();
        liked = in.readByte() != 0;
    }

    public static final Creator<NearbyPoi> CREATOR = new Creator<NearbyPoi>() {
        @Override
        public NearbyPoi createFromParcel(Parcel in) {
            return new NearbyPoi(in);
        }

        @Override
        public NearbyPoi[] newArray(int size) {
            return new NearbyPoi[size];
        }
    };

    public Poi getPoiDetails() {
        return poiDetails;
    }

    public void setPoiDetails(Poi poiDetails) {
        this.poiDetails = poiDetails;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCost() {
        return cost;

    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(poiDetails, flags);
        dest.writeString(distance);
        dest.writeString(time);
        dest.writeString(cost);
        dest.writeByte((byte) (liked ? 1 : 0));
    }
}