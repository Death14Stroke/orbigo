package com.ds14.darren.orbigo.models;

public class Business {
    String abn,user_id,address,bus_email,bus_name,phone_number,capacity,category,description,no_bookings,no_visits,open_days,open_hours,price_range,image;

    public Business() {
    }

    public Business(String abn, String user_id, String address, String bus_email, String bus_name, String phone_number, String capacity, String category, String description, String no_bookings, String no_visits, String open_days, String open_hours, String price_range, String image) {
        this.abn = abn;
        this.user_id = user_id;
        this.address = address;
        this.bus_email = bus_email;
        this.bus_name = bus_name;
        this.phone_number = phone_number;
        this.capacity = capacity;
        this.category = category;
        this.description = description;
        this.no_bookings = no_bookings;
        this.no_visits = no_visits;
        this.open_days = open_days;
        this.open_hours = open_hours;
        this.price_range = price_range;
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAbn() {
        return abn;
    }

    public void setAbn(String abn) {
        this.abn = abn;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBus_email() {
        return bus_email;
    }

    public void setBus_email(String bus_email) {
        this.bus_email = bus_email;
    }

    public String getBus_name() {
        return bus_name;
    }

    public void setBus_name(String bus_name) {
        this.bus_name = bus_name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
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

    public String getNo_bookings() {
        return no_bookings;
    }

    public void setNo_bookings(String no_bookings) {
        this.no_bookings = no_bookings;
    }

    public String getNo_visits() {
        return no_visits;
    }

    public void setNo_visits(String no_visits) {
        this.no_visits = no_visits;
    }

    public String getOpen_days() {
        return open_days;
    }

    public void setOpen_days(String open_days) {
        this.open_days = open_days;
    }

    public String getOpen_hours() {
        return open_hours;
    }

    public void setOpen_hours(String open_hours) {
        this.open_hours = open_hours;
    }

    public String getPrice_range() {
        return price_range;
    }

    public void setPrice_range(String price_range) {
        this.price_range = price_range;
    }
}