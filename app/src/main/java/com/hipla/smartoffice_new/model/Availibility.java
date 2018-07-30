package com.hipla.smartoffice_new.model;

/**
 * Created by FNSPL on 3/15/2018.
 */

public class Availibility {

    private String date;
    private String showingDate;
    private String day;
    private String from_time = "";
    private String to_time = "";
    private String status="N";

    public String getShowingDate() {
        return showingDate;
    }

    public void setShowingDate(String showingDate) {
        this.showingDate = showingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFrom_time() {
        if (from_time != null)
            return from_time;
        else
            return "";
    }

    public void setFrom_time(String from_time) {
        this.from_time = from_time;
    }

    public String getTo_time() {
        if (to_time != null)
            return to_time;
        else
            return "";
    }

    public void setTo_time(String to_time) {
        this.to_time = to_time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }
}
