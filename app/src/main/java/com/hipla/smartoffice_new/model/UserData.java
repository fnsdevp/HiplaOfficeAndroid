package com.hipla.smartoffice_new.model;

/**
 * Created by FNSPL on 2/2/2018.
 */

public class UserData {

    private int id;
    private String usertype;
    private String fname;
    private String lname;
    private String designation;
    private String department;
    private String company;
    private String email;
    private String phone;
    private String status;
    private String profile_image = "";
    private String qr_image = "";

    public String getQr_image() {
        if(qr_image!=null)
        return qr_image;
        else
            return "";
    }

    public void setQr_image(String qr_image) {
        this.qr_image = qr_image;
    }

    public String getProfile_image() {
        if (profile_image != null)
            return profile_image;
        else
            return "";
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getCompany() {
        if (company != null)
            return company;
        return "";
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
