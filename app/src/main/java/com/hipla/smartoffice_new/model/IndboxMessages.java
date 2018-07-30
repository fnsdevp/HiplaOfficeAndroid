package com.hipla.smartoffice_new.model;

/**
 * Created by FNSPL on 3/12/2018.
 */

public class IndboxMessages {

    private int id;
    private MessageFrom from;
    private String title;
    private String msg;
    private String time;
    private String status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MessageFrom getFrom() {
        return from;
    }

    public void setFrom(MessageFrom from) {
        this.from = from;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMsg() {
        return msg.replaceAll("\\\\","");
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}

