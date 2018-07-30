package com.hipla.smartoffice_new.model;

/**
 * Created by FNSPL on 3/9/2018.
 */

public class ReviewMessage {

    private int id;
    private int userid;
    private String username;
    private String review;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getReview() {
        return review.replaceAll("\\\\", "");
    }

    public void setReview(String review) {
        this.review = review;
    }
}
