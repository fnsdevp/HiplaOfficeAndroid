package com.hipla.smartoffice_new.model;

/**
 * Created by FNSPL on 5/15/2018.
 */

public class HotDeskZone {

    private int id;
    private String zone_name;
    private String status;

    private HotSeatData[] row_list;

    public HotSeatData[] getRow_list() {
        return row_list;
    }

    public void setRow_list(HotSeatData[] row_list) {
        this.row_list = row_list;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getZone_name() {
        return zone_name;
    }

    public void setZone_name(String zone_name) {
        this.zone_name = zone_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
