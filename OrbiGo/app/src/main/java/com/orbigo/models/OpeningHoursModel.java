package com.orbigo.models;

public class OpeningHoursModel {
    String from;
    String to;

    public OpeningHoursModel() {
    }

    public OpeningHoursModel(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
