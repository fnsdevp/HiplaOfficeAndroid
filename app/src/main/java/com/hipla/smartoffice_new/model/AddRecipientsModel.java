package com.hipla.smartoffice_new.model;

public class AddRecipientsModel {

    String client_id, client_name, status, email, phone;

    public AddRecipientsModel() {
    }

    public AddRecipientsModel(String id, String name, String email) {
        this.client_id = id;
        this.client_name = name;
        this.email = email;
    }

    public AddRecipientsModel(String id, String name) {
        this.client_id = id;
        this.client_name = name;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_name() {
        return client_name;
    }

    public void setClient_name(String client_name) {
        this.client_name = client_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
