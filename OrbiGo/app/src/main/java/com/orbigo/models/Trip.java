package com.orbigo.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Trip implements Parcelable {
    String id,name,createdDate;

    public Trip() {
    }

    public Trip(String id, String name, String createdDate) {
        this.id = id;
        this.name = name;
        this.createdDate = createdDate;
    }

    protected Trip(Parcel in) {
        id = in.readString();
        name = in.readString();
        createdDate = in.readString();
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(createdDate);
    }
}