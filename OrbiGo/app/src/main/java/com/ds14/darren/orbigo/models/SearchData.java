package com.ds14.darren.orbigo.models;

import android.os.Parcel;
import android.os.Parcelable;

public class SearchData implements Parcelable{
    private String id;
    private String name;

    public SearchData() {
    }

    public SearchData(String id, String name) {
        this.id = id;
        this.name = name;
    }

    protected SearchData(Parcel in) {
        id = in.readString();
        name = in.readString();
    }

    public static final Creator<SearchData> CREATOR = new Creator<SearchData>() {
        @Override
        public SearchData createFromParcel(Parcel in) {
            return new SearchData(in);
        }

        @Override
        public SearchData[] newArray(int size) {
            return new SearchData[size];
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
    }
}