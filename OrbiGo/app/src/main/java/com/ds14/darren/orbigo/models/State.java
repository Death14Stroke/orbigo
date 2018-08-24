package com.ds14.darren.orbigo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLngBounds;

public class State extends SearchData{
    private String is_in_country;
    private LatLngBounds latLngBounds;

    public State() {
    }

    public State(Parcel source) {
        super(source);
        this.is_in_country = source.readString();
        latLngBounds = source.readParcelable(LatLngBounds.class.getClassLoader());
    }

    public LatLngBounds getLatLngBounds() {
        return latLngBounds;
    }

    public void setLatLngBounds(LatLngBounds latLngBounds) {
        this.latLngBounds = latLngBounds;
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
        dest.writeString(this.is_in_country);
        dest.writeParcelable(latLngBounds,flags);
    }

    public static final Parcelable.Creator CREATOR = new Creator() {
        @Override
        public State createFromParcel(Parcel source) {
            return new State(source);
        }

        @Override
        public State[] newArray(int size) {
            return new State[size];
        }
    };
}