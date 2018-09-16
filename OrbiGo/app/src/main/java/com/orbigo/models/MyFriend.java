package com.orbigo.models;

public class MyFriend {
    private String id,name,detail,image;

    public MyFriend(String id, String name, String detail, String image) {
        this.id = id;
        this.name = name;
        this.detail = detail;
        this.image = image;
    }

    public MyFriend() {
    }

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

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}