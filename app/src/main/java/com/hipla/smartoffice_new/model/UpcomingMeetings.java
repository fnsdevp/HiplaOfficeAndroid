package com.hipla.smartoffice_new.model;

/**
 * Created by FNSPL on 3/3/2018.
 */

public class UpcomingMeetings {

    private GuestData guest;
    private int id;
    private String employee_id;
    private String employee_name;
    private String employee_email;
    private String employee_phone;
    private String department;
    private String totime;
    private String fromtime;
    private float duration;
    private String agenda;
    private String fdate;
    private String sdate;
    private String appointmentType;
    private String read_status;
    private String status;
    private String otp;
    private String qrUrl;
    private String room_name;
    private String location;
    private String wifiUsername = "";
    private String wifiPassword = "";
    private AddRecipientsModel[] recipient;

    public String getWifiUsername() {
        if (wifiUsername != null)
            return wifiUsername;
        else
            return "";
    }

    public void setWifiUsername(String wifiUsername) {
        this.wifiUsername = wifiUsername;
    }

    public String getWifiPassword() {
        if (wifiPassword != null)
            return wifiPassword;
        else
            return "";
    }

    public void setWifiPassword(String wifiPassword) {
        this.wifiPassword = wifiPassword;
    }

    public String getRoom_name() {
        return room_name;
    }

    public void setRoom_name(String room_name) {
        this.room_name = room_name;
    }

    public String getQrUrl() {
        if (qrUrl != null)
            return qrUrl;
        else
            return "";
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }

    public AddRecipientsModel[] getRecipient() {
        return recipient;
    }

    public void setRecipient(AddRecipientsModel[] recipient) {
        this.recipient = recipient;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public GuestData getGuest() {
        return guest;
    }

    public void setGuest(GuestData guest) {
        this.guest = guest;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }

    public String getEmployee_email() {
        return employee_email;
    }

    public void setEmployee_email(String employee_email) {
        this.employee_email = employee_email;
    }

    public String getEmployee_phone() {
        return employee_phone;
    }

    public void setEmployee_phone(String employee_phone) {
        this.employee_phone = employee_phone;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getTotime() {
        return totime;
    }

    public void setTotime(String totime) {
        this.totime = totime;
    }

    public String getFromtime() {
        return fromtime;
    }

    public void setFromtime(String fromtime) {
        this.fromtime = fromtime;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public String getAgenda() {
        return agenda;
    }

    public void setAgenda(String agenda) {
        this.agenda = agenda;
    }

    public String getFdate() {
        return fdate;
    }

    public void setFdate(String fdate) {
        this.fdate = fdate;
    }

    public String getSdate() {
        return sdate;
    }

    public void setSdate(String sdate) {
        this.sdate = sdate;
    }

    public String getAppointmentType() {
        return appointmentType;
    }

    public void setAppointmentType(String appointmentType) {
        this.appointmentType = appointmentType;
    }

    public String getRead_status() {
        return read_status;
    }

    public void setRead_status(String read_status) {
        this.read_status = read_status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
