package com.hipla.smartoffice_new.database;

import android.provider.BaseColumns;

/**
 * Created by FNSPL on 3/28/2018.
 */

public class Db_Schema {

    private Db_Schema(){}

    public static class MeetingsDb implements BaseColumns {
        public  static final String TABLE_MEETINGS = "meetingsTable";
        public  static final String COLUMN_ID = "id";
        public  static final String COLUMN_EMPLOYEE_ID = "employee_id";
        public  static final String COLUMN_EMPLOYEE_NAME = "employee_name";
        public  static final String COLUMN_EMPLOYEE_EMAIL = "employee_email";
        public  static final String COLUMN_EMPLOYEE_PHONE = "employee_phone";
        public  static final String COLUMN_DEPARTMENT = "department";
        public  static final String COLUMN_TO_TIME= "totime";
        public  static final String COLUMN_FROM_TIME= "fromtime";
        public  static final String COLUMN_DURATION= "duration";
        public  static final String COLUMN_AGENDA= "agenda";
        public  static final String COLUMN_FDATE= "fdate";
        public  static final String COLUMN_SDATE= "sdate";
        public  static final String COLUMN_APPOINTMENT_TYPE= "appointmentType";
        public  static final String COLUMN_READ_STATUS= "read_status";
        public  static final String COLUMN_STATUS= "status";
        public  static final String COLUMN_OTP= "otp";
        public  static final String COLUMN_LOCATION= "location";
        public  static final String COLUMN_ROOM_NAME= "room_name";
        public  static final String COLUMN_QR_URL= "qrUrl";
        public  static final String COLUMN_TIME_STAMP= "timeStamp";
    }

    public static class GuestDb implements BaseColumns{
        public  static final String TABLE_GUEST_INFO = "guestTable";
        public  static final String COLUMN_ID = "id";
        public  static final String COLUMN_GUEST_ID = "guestId";
        public  static final String COLUMN_GUEST_NAME = "contact";
        public  static final String COLUMN_GUEST_COMPANY = "company";
        public  static final String COLUMN_GUEST_DESIGNATION = "designation";
        public  static final String COLUMN_GUEST_EMAIL = "email";
        public  static final String COLUMN_GUEST_PHONE = "phone";
    }

    public static class ZoneInfo implements BaseColumns{

        public  static final String TABLE_ZONE = "zoneInfo";
        public  static final String COLUMN_ZONE_ID = "zoneId";
        public  static final String COLUMN_ZONE_NAME = "zoneName";
        public  static final String COLUMN_CENTER = "centerPoint";
        public  static final String COLUMN_POINT_A = "pointA";
        public  static final String COLUMN_POINT_B = "pointB";
        public  static final String COLUMN_POINT_C = "pointC";
        public  static final String COLUMN_POINT_D = "pointD";

    }

    public static class NotificationText implements BaseColumns{

        public  static final String TABLE_NOTIFICATION = "notification";
        public  static final String COLUMN_NOTIFICATION_ID = "notificationId";
        public  static final String COLUMN_NOTIFICATION_TEXT = "notificationMsg";
        public  static final String COLUMN_TIME_STAMP= "timeStamp";
        public  static final String COLUMN_NOTIFICATION_DATE= "notificationDate";

    }

    public static class IseWiFI implements BaseColumns{

        public  static final String TABLE_IseWiFi = "isewifi";
        public  static final String COLUMN_IseWiFi_ID = "isewifiId";
        public  static final String COLUMN_IseUsername = "userName";
        public  static final String COLUMN_IsePassword = "isePassword";

    }

}
