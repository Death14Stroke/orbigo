package com.ds14.darren.orbigo.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class Poi extends SearchData{
    private String is_in_country,is_in_region,is_in_state,address,category,description,eventdate,phone,url,picture;
    private LatLng location;

    public Poi() {
    }

    public Poi(String id, String name, String is_in_country, String is_in_region, String is_in_state, LatLng location, String address, String category, String description, String eventdate, String phone, String url, String picture) {
        super(id, name);
        this.is_in_country = is_in_country;
        this.is_in_region = is_in_region;
        this.is_in_state = is_in_state;
        this.location = location;
        this.address = address;
        this.category = category;
        this.description = description;
        this.eventdate = eventdate;
        this.phone = phone;
        this.url = url;
        this.picture = picture;
    }

    protected Poi(Parcel in) {
        super(in);
        is_in_country = in.readString();
        is_in_region = in.readString();
        is_in_state = in.readString();
        address = in.readString();
        category = in.readString();
        description = in.readString();
        eventdate = in.readString();
        phone = in.readString();
        url = in.readString();
        picture = in.readString();
        location = in.readParcelable(LatLng.class.getClassLoader());
    }

    public String getIs_in_country() {
        return is_in_country;
    }

    public void setIs_in_country(String is_in_country) {
        this.is_in_country = is_in_country;
    }

    public String getIs_in_region() {
        return is_in_region;
    }

    public void setIs_in_region(String is_in_region) {
        this.is_in_region = is_in_region;
    }

    public String getIs_in_state() {
        return is_in_state;
    }

    public void setIs_in_state(String is_in_state) {
        this.is_in_state = is_in_state;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventdate() {
        return eventdate;
    }

    public void setEventdate(String eventdate) {
        this.eventdate = eventdate;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeString(is_in_country);
        dest.writeString(is_in_region);
        dest.writeString(is_in_state);
        dest.writeString(address);
        dest.writeString(category);
        dest.writeString(description);
        dest.writeString(eventdate);
        dest.writeString(phone);
        dest.writeString(url);
        dest.writeString(picture);
        dest.writeParcelable(location,flags);
    }

    public static final Parcelable.Creator CREATOR = new Creator<Poi>() {
        @Override
        public Poi createFromParcel(Parcel source) {
            return new Poi(source);
        }

        @Override
        public Poi[] newArray(int size) {
            return new Poi[size];
        }
    };
}