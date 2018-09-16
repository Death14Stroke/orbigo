package com.orbigo.models;

public class EarningHistoryModel {
    String date;
    String business_user_id;
    String abn;
    String booking_id;
    String amount;

    public EarningHistoryModel() {
    }

    public EarningHistoryModel(String date, String business_user_id, String abn, String booking_id, String amount) {
        this.date = date;
        this.business_user_id = business_user_id;
        this.abn = abn;
        this.booking_id = booking_id;
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBusiness_user_id() {
        return business_user_id;
    }

    public void setBusiness_user_id(String business_user_id) {
        this.business_user_id = business_user_id;
    }

    public String getAbn() {
        return abn;
    }

    public void setAbn(String abn) {
        this.abn = abn;
    }

    public String getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(String booking_id) {
        this.booking_id = booking_id;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
