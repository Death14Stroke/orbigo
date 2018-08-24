package com.ds14.darren.orbigo.models;

public class MeddsModel {

    private String name;
    private int image_id;
    private boolean selected;

    public MeddsModel(String name, int image_id, boolean selected) {
        this.name = name;
        this.image_id = image_id;
        this.selected = selected;
    }

    public MeddsModel() {
    }
    
    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImage_id() {
        return image_id;
    }

    public void setImage_id(int image_id) {
        this.image_id = image_id;
    }
}