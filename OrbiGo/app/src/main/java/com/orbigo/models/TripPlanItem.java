package com.orbigo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.orbigo.enums.TripStatus;
import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class TripPlanItem implements Parcelable,Serializable {
    private String id,imageurl,placeName,region,timeSpent,cost,category;
    private TripStatus tripStatus;
    private LatLng latLng;
    private long arrivalTime;
    private long totalDistance;
    private long totalTime;

    private long time;
    private long distance;

    public TripPlanItem() {
    }

    protected TripPlanItem(Parcel in) {
        id = in.readString();
        imageurl = in.readString();
        placeName = in.readString();
        region = in.readString();
        distance = in.readLong();
        timeSpent = in.readString();
        cost = in.readString();
        category = in.readString();
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        arrivalTime = in.readLong();
    }

    public static final Creator<TripPlanItem> CREATOR = new Creator<TripPlanItem>() {
        @Override
        public TripPlanItem createFromParcel(Parcel in) {
            return new TripPlanItem(in);
        }

        @Override
        public TripPlanItem[] newArray(int size) {
            return new TripPlanItem[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LatLng getLatlng() {
        return latLng;
    }

    public void setLatlng(LatLng latlng) {
        this.latLng = latlng;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public long getDistance() {
        return distance;
    }

    public void setDistance(long distance) {
        this.distance = distance;
    }

    public String getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(String timeSpent) {
        this.timeSpent = timeSpent;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TripStatus getTripStatus() {
        return tripStatus;
    }

    public void setTripStatus(TripStatus tripStatus) {
        this.tripStatus = tripStatus;
    }

    public long getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(long totalDistance) {
        this.totalDistance = totalDistance;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }



    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }



    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(imageurl);
        dest.writeString(placeName);
        dest.writeString(region);
        dest.writeLong(distance);
        dest.writeString(timeSpent);
        dest.writeString(cost);
        dest.writeString(category);
        dest.writeParcelable(latLng, flags);
        dest.writeLong(arrivalTime);
    }
}