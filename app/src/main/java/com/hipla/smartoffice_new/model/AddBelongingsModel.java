package com.hipla.smartoffice_new.model;

public class AddBelongingsModel {
    public void setId(int id) {
        this.id = id;
    }

    int id;

    public int getId() {
        return id;
    }

    public int getGuest_id() {
        return guest_id;
    }

    int guest_id;
    String property_type, property_value;

    public AddBelongingsModel(int id, int guest_id, String property_type, String property_value) {
        this.id = id;
        this.guest_id = guest_id;
        this.property_type = property_type;
        this.property_value = property_value;
    }

    public AddBelongingsModel() {
    }

    public AddBelongingsModel(String name, String value) {

        this.property_type = name;
        this.property_value = value;
    }

    public String getPropertyType() {
        return property_type;
    }

    public void setPropertyType(String name) {
        this.property_type = name;
    }

    public String getPropertyValue() {
        return property_value;
    }

    public void setPropertyValue(String value) {
        this.property_value = value;
    }
}
