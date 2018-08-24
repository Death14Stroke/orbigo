package com.ds14.darren.orbigo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLngBounds;

public class Country extends SearchData{
    private String flagUrl,iconUrl;
    private LatLngBounds latLngBounds;

    public Country() {
    }

    public Country(Parcel in){
        super(in);
        flagUrl = in.readString();
        iconUrl = in.readString();
        latLngBounds = in.readParcelable(LatLngBounds.class.getClassLoader());
    }

    public String getFlagUrl() {
        return flagUrl;
    }

    public void setFlagUrl(String flagUrl) {
        this.flagUrl = flagUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public LatLngBounds getLatLngBounds() {
        return latLngBounds;
    }

    public void setLatLngBounds(LatLngBounds latLngBounds) {
        this.latLngBounds = latLngBounds;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(flagUrl);
        dest.writeString(iconUrl);
        dest.writeParcelable(latLngBounds,flags);
    }

    public static final Parcelable.Creator CREATOR = new Creator<Country>() {
        @Override
        public Country createFromParcel(Parcel source) {
            return new Country(source);
        }

        @Override
        public Country[] newArray(int size) {
            return new Country[size];
        }
    };
}