package com.hipla.smartoffice_new.model;

/**
 * Created by FNSPL on 5/14/2018.
 */

public class HotDeskObject {

    private int hotZoneId;
    private int seatId;
    private int hot_seat_id;
    private String hotZoneName;
    private String seatName;
    private String wifiUserName;
    private String wifiPassword;

    public HotDeskObject(int hotZoneId, int seatId,int hot_seat_id, String hotZoneName, String seatName) {
        this.hotZoneId = hotZoneId;
        this.seatId = seatId;
        this.hot_seat_id = hot_seat_id;
        this.hotZoneName = hotZoneName;
        this.seatName = seatName;
    }

    public HotDeskObject(int hotZoneId, int seatId, int hot_seat_id, String hotZoneName, String seatName, String wifiUserName, String wifiPassword) {
        this.hotZoneId = hotZoneId;
        this.seatId = seatId;
        this.hot_seat_id = hot_seat_id;
        this.hotZoneName = hotZoneName;
        this.seatName = seatName;
        this.wifiUserName = wifiUserName;
        this.wifiPassword = wifiPassword;
    }

    public String getWifiUserName() {
        return wifiUserName;
    }

    public void setWifiUserName(String wifiUserName) {
        this.wifiUserName = wifiUserName;
    }

    public String getWifiPassword() {
        return wifiPassword;
    }

    public void setWifiPassword(String wifiPassword) {
        this.wifiPassword = wifiPassword;
    }

    public int getHot_seat_id() {
        return hot_seat_id;
    }

    public void setHot_seat_id(int hot_seat_id) {
        this.hot_seat_id = hot_seat_id;
    }

    public int getHotZoneId() {
        return hotZoneId;
    }

    public void setHotZoneId(int hotZoneId) {
        this.hotZoneId = hotZoneId;
    }

    public int getSeatId() {
        return seatId;
    }

    public void setSeatId(int seatId) {
        this.seatId = seatId;
    }

    public String getHotZoneName() {
        return hotZoneName;
    }

    public void setHotZoneName(String hotZoneName) {
        this.hotZoneName = hotZoneName;
    }

    public String getSeatName() {
        return seatName;
    }

    public void setSeatName(String seatName) {
        this.seatName = seatName;
    }
}
