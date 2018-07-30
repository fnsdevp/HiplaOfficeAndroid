package com.hipla.smartoffice_new.model;

/**
 * Created by FNSPL on 2/22/2018.
 */

public class RoomData {

    private int id;
    private String name;
    private String place_unique_id;
    private String venue_unique_id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace_unique_id() {
        return place_unique_id;
    }

    public void setPlace_unique_id(String place_unique_id) {
        this.place_unique_id = place_unique_id;
    }

    public String getVenue_unique_id() {
        return venue_unique_id;
    }

    public void setVenue_unique_id(String venue_unique_id) {
        this.venue_unique_id = venue_unique_id;
    }
}
