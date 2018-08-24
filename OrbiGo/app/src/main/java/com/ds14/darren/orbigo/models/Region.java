package com.ds14.darren.orbigo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLngBounds;

public class Region extends SearchData{
    private String is_in_state,is_in_country;
    private LatLngBounds latLngBounds;

    public Region() {
    }

    public Region(Parcel source) {
        super(source);
        this.is_in_state = source.readString();
        this.is_in_country = source.readString();
        this.latLngBounds = source.readParcelable(LatLngBounds.class.getClassLoader());
    }

    public LatLngBounds getLatLngBounds() {
        return latLngBounds;
    }

    public void setLatLngBounds(LatLngBounds latLngBounds) {
        this.latLngBounds = latLngBounds;
    }

    public String getIs_in_state() {
        return is_in_state;
    }

    public void setIs_in_state(String is_in_state) {
        this.is_in_state = is_in_state;
    }

    public String getIs_in_country() {
        return is_in_country;
    }

    public void setIs_in_country(String is_in_country) {
        this.is_in_country = is_in_country;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeString(is_in_state);
        dest.writeString(is_in_country);
        dest.writeParcelable(latLngBounds,flags);
    }

    public static final Parcelable.Creator CREATOR = new Creator<Region>() {
        @Override
        public Region createFromParcel(Parcel source) {
            return new Region(source);
        }

        @Override
        public Region[] newArray(int size) {
            return new Region[size];
        }
    };
}