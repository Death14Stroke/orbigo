package com.orbigo.models;

public class PendingBooking {
    String booking_id,customer_name,no_of_adult,no_of_children,arrival;

    public PendingBooking() {
    }

    public PendingBooking(String booking_id, String customer_name, String no_of_adult, String no_of_children, String arrival) {
        this.booking_id = booking_id;
        this.customer_name = customer_name;
        this.no_of_adult = no_of_adult;
        this.no_of_children = no_of_children;
        this.arrival = arrival;
    }

    public String getBooking_id() {
        return booking_id;
    }

    public void setBooking_id(String booking_id) {
        this.booking_id = booking_id;
    }

    public String getCustomer_name() {
        return customer_name;
    }

    public void setCustomer_name(String customer_name) {
        this.customer_name = customer_name;
    }

    public String getNo_of_adult() {
        return no_of_adult;
    }

    public void setNo_of_adult(String no_of_adult) {
        this.no_of_adult = no_of_adult;
    }

    public String getNo_of_children() {
        return no_of_children;
    }

    public void setNo_of_children(String no_of_children) {
        this.no_of_children = no_of_children;
    }

    public String getArrival() {
        return arrival;
    }

    public void setArrival(String arrival) {
        this.arrival = arrival;
    }
}
