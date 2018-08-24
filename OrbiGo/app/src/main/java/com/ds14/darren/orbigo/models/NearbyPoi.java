package com.ds14.darren.orbigo.models;

public class NearbyPoi{
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
}