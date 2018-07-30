package com.hipla.smartoffice_new.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.hipla.smartoffice_new.model.GuestData;
import com.hipla.smartoffice_new.model.Notification;
import com.hipla.smartoffice_new.model.UpcomingMeetings;
import com.hipla.smartoffice_new.model.ZoneInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by FNSPL on 3/28/2018.
 */

public class Db_Helper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "hiplateacher.db";
    public static final String TAG = "hiplateacher";
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    public static final String SQL_CREATE_TABLE_MEETING = "CREATE TABLE " + Db_Schema.MeetingsDb.TABLE_MEETINGS + " ( " +
            Db_Schema.MeetingsDb.COLUMN_ID + " INTEGER PRIMARY KEY , " +
            Db_Schema.GuestDb.COLUMN_GUEST_ID + " INTEGER , " +
            Db_Schema.GuestDb.COLUMN_GUEST_COMPANY + " TEXT , " +
            Db_Schema.GuestDb.COLUMN_GUEST_NAME + " TEXT , " +
            Db_Schema.GuestDb.COLUMN_GUEST_DESIGNATION + " TEXT , " +
            Db_Schema.GuestDb.COLUMN_GUEST_EMAIL + " TEXT , " +
            Db_Schema.GuestDb.COLUMN_GUEST_PHONE + " TEXT ," +
            Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_ID + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_EMAIL + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_NAME + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_PHONE + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_DEPARTMENT + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_TO_TIME + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_FROM_TIME + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_DURATION + " FLOAT , " +
            Db_Schema.MeetingsDb.COLUMN_AGENDA + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_FDATE + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_SDATE + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_APPOINTMENT_TYPE + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_READ_STATUS + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_STATUS + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_LOCATION + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_ROOM_NAME + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_QR_URL + " TEXT , " +
            Db_Schema.MeetingsDb.COLUMN_TIME_STAMP + " INTEGER , " +
            Db_Schema.MeetingsDb.COLUMN_OTP + " TEXT )";

    public static final String SQL_CREATE_TABLE_GUEST_INFO = "CREATE TABLE " + Db_Schema.GuestDb.TABLE_GUEST_INFO + " ( " +
            Db_Schema.GuestDb.COLUMN_ID + " INTEGER PRIMARY KEY , " +
            Db_Schema.GuestDb.COLUMN_GUEST_ID + " INTEGER , " +
            Db_Schema.GuestDb.COLUMN_GUEST_COMPANY + " TEXT , " +
            Db_Schema.GuestDb.COLUMN_GUEST_NAME + " TEXT , " +
            Db_Schema.GuestDb.COLUMN_GUEST_DESIGNATION + " TEXT , " +
            Db_Schema.GuestDb.COLUMN_GUEST_EMAIL + " TEXT , " +
            Db_Schema.GuestDb.COLUMN_GUEST_PHONE + " TEXT )";

    public static final String SQL_CREATE_TABLE_ZONEINFO = "CREATE TABLE " + Db_Schema.ZoneInfo.TABLE_ZONE + " ( " +
            Db_Schema.ZoneInfo.COLUMN_ZONE_ID + " INTEGER PRIMARY KEY , " +
            Db_Schema.ZoneInfo.COLUMN_CENTER + " TEXT , " +
            Db_Schema.ZoneInfo.COLUMN_ZONE_NAME + " TEXT , " +
            Db_Schema.ZoneInfo.COLUMN_POINT_A + " TEXT , " +
            Db_Schema.ZoneInfo.COLUMN_POINT_B + " TEXT , " +
            Db_Schema.ZoneInfo.COLUMN_POINT_C + " TEXT , " +
            Db_Schema.ZoneInfo.COLUMN_POINT_D + " TEXT )";

    public static final String SQL_CREATE_TABLE_NOTIFICATION = "CREATE TABLE " + Db_Schema.NotificationText.TABLE_NOTIFICATION + " ( " +
            Db_Schema.NotificationText.COLUMN_NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , " +
            Db_Schema.NotificationText.COLUMN_NOTIFICATION_TEXT + " TEXT , " +
            Db_Schema.NotificationText.COLUMN_NOTIFICATION_DATE + " TEXT , " +
            Db_Schema.NotificationText.COLUMN_TIME_STAMP + " INTEGER )";

    public static final String SQL_CREATE_TABLE_ISE = "CREATE TABLE " + Db_Schema.IseWiFI.TABLE_IseWiFi + " ( " +
            Db_Schema.IseWiFI.COLUMN_IseWiFi_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL , " +
            Db_Schema.IseWiFI.COLUMN_IseUsername + " TEXT , " +
            Db_Schema.IseWiFI.COLUMN_IsePassword + " TEXT )";

    public Db_Helper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_TABLE_MEETING);
        db.execSQL(SQL_CREATE_TABLE_ZONEINFO);
        db.execSQL(SQL_CREATE_TABLE_NOTIFICATION);
        db.execSQL(SQL_CREATE_TABLE_ISE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void deleteMeetings() {
        try {
            getWritableDatabase().delete(Db_Schema.MeetingsDb.TABLE_MEETINGS, null, null);
        } catch (SQLException e) {

        }
    }

    public void deleteGuests() {
        try {
            getWritableDatabase().delete(Db_Schema.GuestDb.TABLE_GUEST_INFO, null, null);
        } catch (SQLException e) {

        }
    }

    public void deleteZoneInfo() {
        try {
            getWritableDatabase().delete(Db_Schema.ZoneInfo.TABLE_ZONE, null, null);
        } catch (SQLException e) {

        }
    }

    public void deleteNotificationInfo() {
        try {
            getWritableDatabase().delete(Db_Schema.NotificationText.TABLE_NOTIFICATION, null, null);
        } catch (SQLException e) {

        }
    }

    public void insertAllMeetings(List<UpcomingMeetings> upcomingMeetingsList) {
        for (UpcomingMeetings upcomingMeeting :
                upcomingMeetingsList) {

            insert_meetings(upcomingMeeting);
        }
    }

    public void insert_meetings(UpcomingMeetings upcomingMeeting) {

        try {
            ContentValues values = new ContentValues();
            values.put(Db_Schema.MeetingsDb.COLUMN_ID, upcomingMeeting.getId());
            values.put(Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_ID, upcomingMeeting.getEmployee_id());
            values.put(Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_NAME, upcomingMeeting.getEmployee_name());
            values.put(Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_EMAIL, upcomingMeeting.getEmployee_email());
            values.put(Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_PHONE, upcomingMeeting.getEmployee_phone());
            values.put(Db_Schema.MeetingsDb.COLUMN_DEPARTMENT, upcomingMeeting.getDepartment());
            values.put(Db_Schema.MeetingsDb.COLUMN_TO_TIME, upcomingMeeting.getTotime());
            values.put(Db_Schema.MeetingsDb.COLUMN_FROM_TIME, upcomingMeeting.getFromtime());
            values.put(Db_Schema.MeetingsDb.COLUMN_DURATION, upcomingMeeting.getDuration());
            values.put(Db_Schema.MeetingsDb.COLUMN_AGENDA, upcomingMeeting.getAgenda());
            values.put(Db_Schema.MeetingsDb.COLUMN_FDATE, upcomingMeeting.getFdate());
            values.put(Db_Schema.MeetingsDb.COLUMN_SDATE, upcomingMeeting.getSdate());
            values.put(Db_Schema.MeetingsDb.COLUMN_APPOINTMENT_TYPE, upcomingMeeting.getAppointmentType());
            values.put(Db_Schema.MeetingsDb.COLUMN_READ_STATUS, upcomingMeeting.getRead_status());
            values.put(Db_Schema.MeetingsDb.COLUMN_STATUS, upcomingMeeting.getStatus());
            values.put(Db_Schema.MeetingsDb.COLUMN_LOCATION, upcomingMeeting.getLocation());
            values.put(Db_Schema.MeetingsDb.COLUMN_ROOM_NAME, upcomingMeeting.getRoom_name());
            values.put(Db_Schema.MeetingsDb.COLUMN_QR_URL, upcomingMeeting.getQrUrl());
            values.put(Db_Schema.GuestDb.COLUMN_GUEST_ID, upcomingMeeting.getGuest().getId());
            values.put(Db_Schema.GuestDb.COLUMN_GUEST_COMPANY, upcomingMeeting.getGuest().getCompany());
            values.put(Db_Schema.GuestDb.COLUMN_GUEST_NAME, upcomingMeeting.getGuest().getContact());
            values.put(Db_Schema.GuestDb.COLUMN_GUEST_DESIGNATION, upcomingMeeting.getGuest().getDesignation());
            values.put(Db_Schema.GuestDb.COLUMN_GUEST_EMAIL, upcomingMeeting.getGuest().getEmail());
            values.put(Db_Schema.GuestDb.COLUMN_GUEST_PHONE, upcomingMeeting.getGuest().getPhone());
            values.put(Db_Schema.MeetingsDb.COLUMN_TIME_STAMP, dateFormat.parse(upcomingMeeting.getFdate()
                    + " " + upcomingMeeting.getFromtime()).getTime());

            if (!getUid(upcomingMeeting.getId())) {
                getWritableDatabase().insert(Db_Schema.MeetingsDb.TABLE_MEETINGS, null, values);
            } else {
                getWritableDatabase().update(Db_Schema.MeetingsDb.TABLE_MEETINGS, values,
                        Db_Schema.MeetingsDb.COLUMN_ID + "=" + upcomingMeeting.getId(), null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean getUid(int uid) {
        String read_query = " SELECT " + Db_Schema.MeetingsDb.COLUMN_ID + " FROM " + Db_Schema.MeetingsDb.TABLE_MEETINGS;
        Cursor cursor = getReadableDatabase().rawQuery(read_query, null);

        if (cursor == null)
            return false;

        if (cursor.moveToFirst()) {
            Log.d(TAG, "getUid: " + cursor.getInt(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_ID)));

            if (uid == cursor.getInt(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_ID))) {
                return true;
            }
        }
        return false;
    }

    public List<UpcomingMeetings> getUpcomingMeetings(String date) {
        SQLiteDatabase db = getReadableDatabase();
        List<UpcomingMeetings> upcomingMeetingsList = new ArrayList<>();

        String[] params = new String[]{"" + date};

        Cursor cursor = db.query(Db_Schema.MeetingsDb.TABLE_MEETINGS, null,
                Db_Schema.MeetingsDb.COLUMN_FDATE + " = ?", params,
                null, null, Db_Schema.MeetingsDb.COLUMN_TIME_STAMP + " ASC");

        while (cursor != null && cursor.moveToNext()) {
            Log.d("dev", "database read: " + cursor.getInt(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_ID)));
            UpcomingMeetings upcomingMeetings = new UpcomingMeetings();
            upcomingMeetings.setId(cursor.getInt(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_ID)));
            upcomingMeetings.setEmployee_id(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_ID)));
            upcomingMeetings.setEmployee_name(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_NAME)));
            upcomingMeetings.setEmployee_email(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_EMAIL)));
            upcomingMeetings.setEmployee_phone(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_PHONE)));
            upcomingMeetings.setDepartment(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_DEPARTMENT)));
            upcomingMeetings.setTotime(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_TO_TIME)));
            upcomingMeetings.setFromtime(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_FROM_TIME)));
            upcomingMeetings.setAgenda(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_AGENDA)));
            upcomingMeetings.setDuration(cursor.getInt(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_DURATION)));
            upcomingMeetings.setFdate(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_FDATE)));
            upcomingMeetings.setSdate(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_SDATE)));
            upcomingMeetings.setAppointmentType(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_APPOINTMENT_TYPE)));
            upcomingMeetings.setRead_status(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_READ_STATUS)));
            upcomingMeetings.setStatus(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_STATUS)));
            upcomingMeetings.setOtp(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_OTP)));
            upcomingMeetings.setLocation(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_LOCATION)));
            upcomingMeetings.setRoom_name(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_ROOM_NAME)));
            upcomingMeetings.setQrUrl(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_QR_URL)));

            GuestData guestData = new GuestData();
            guestData.setId(cursor.getInt(cursor.getColumnIndex(Db_Schema.GuestDb.COLUMN_ID)));
            guestData.setContact(cursor.getString(cursor.getColumnIndex(Db_Schema.GuestDb.COLUMN_GUEST_NAME)));
            guestData.setCompany(cursor.getString(cursor.getColumnIndex(Db_Schema.GuestDb.COLUMN_GUEST_COMPANY)));
            guestData.setDesignation(cursor.getString(cursor.getColumnIndex(Db_Schema.GuestDb.COLUMN_GUEST_DESIGNATION)));
            guestData.setEmail(cursor.getString(cursor.getColumnIndex(Db_Schema.GuestDb.COLUMN_GUEST_EMAIL)));
            guestData.setPhone(cursor.getString(cursor.getColumnIndex(Db_Schema.GuestDb.COLUMN_GUEST_PHONE)));

            upcomingMeetings.setGuest(guestData);
            upcomingMeetingsList.add(upcomingMeetings);
        }

        db.close();

        return upcomingMeetingsList;
    }

    public UpcomingMeetings getUpcoingMeetings(int meetingId) {
        SQLiteDatabase db = getReadableDatabase();
        UpcomingMeetings upcomingMeetings = null;

        String[] params = new String[]{"" + meetingId};

        Cursor cursor = db.query(Db_Schema.MeetingsDb.TABLE_MEETINGS, null,
                Db_Schema.MeetingsDb.COLUMN_ID + " = ?", params,
                null, null, null);

        if (cursor != null && cursor.moveToNext()) {
            Log.d("dev", "database read: " + cursor.getInt(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_ID)));
            upcomingMeetings = new UpcomingMeetings();

            upcomingMeetings.setId(cursor.getInt(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_ID)));
            upcomingMeetings.setEmployee_id(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_ID)));
            upcomingMeetings.setEmployee_name(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_NAME)));
            upcomingMeetings.setEmployee_email(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_EMAIL)));
            upcomingMeetings.setEmployee_phone(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_EMPLOYEE_PHONE)));
            upcomingMeetings.setDepartment(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_DEPARTMENT)));
            upcomingMeetings.setTotime(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_TO_TIME)));
            upcomingMeetings.setFromtime(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_FROM_TIME)));
            upcomingMeetings.setAgenda(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_AGENDA)));
            upcomingMeetings.setDuration(cursor.getInt(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_DURATION)));
            upcomingMeetings.setFdate(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_FDATE)));
            upcomingMeetings.setSdate(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_SDATE)));
            upcomingMeetings.setAppointmentType(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_APPOINTMENT_TYPE)));
            upcomingMeetings.setRead_status(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_READ_STATUS)));
            upcomingMeetings.setStatus(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_STATUS)));
            upcomingMeetings.setOtp(cursor.getString(cursor.getColumnIndex(Db_Schema.MeetingsDb.COLUMN_OTP)));

            GuestData guestData = new GuestData();
            guestData.setId(cursor.getInt(cursor.getColumnIndex(Db_Schema.GuestDb.COLUMN_ID)));
            guestData.setContact(cursor.getString(cursor.getColumnIndex(Db_Schema.GuestDb.COLUMN_GUEST_NAME)));
            guestData.setCompany(cursor.getString(cursor.getColumnIndex(Db_Schema.GuestDb.COLUMN_GUEST_COMPANY)));
            guestData.setDesignation(cursor.getString(cursor.getColumnIndex(Db_Schema.GuestDb.COLUMN_GUEST_DESIGNATION)));
            guestData.setEmail(cursor.getString(cursor.getColumnIndex(Db_Schema.GuestDb.COLUMN_GUEST_EMAIL)));
            guestData.setPhone(cursor.getString(cursor.getColumnIndex(Db_Schema.GuestDb.COLUMN_GUEST_PHONE)));

            upcomingMeetings.setGuest(guestData);
        }

        db.close();

        return upcomingMeetings;
    }

    public boolean insert_zone(ZoneInfo zoneInfo) {
        try {
            ContentValues values = new ContentValues();
            values.put(Db_Schema.ZoneInfo.COLUMN_ZONE_ID, zoneInfo.getId());
            values.put(Db_Schema.ZoneInfo.COLUMN_ZONE_NAME, zoneInfo.getZoneName());
            values.put(Db_Schema.ZoneInfo.COLUMN_CENTER, zoneInfo.getCenterPoint());
            values.put(Db_Schema.ZoneInfo.COLUMN_POINT_A, zoneInfo.getPointA());
            values.put(Db_Schema.ZoneInfo.COLUMN_POINT_B, zoneInfo.getPointB());
            values.put(Db_Schema.ZoneInfo.COLUMN_POINT_C, zoneInfo.getPointC());
            values.put(Db_Schema.ZoneInfo.COLUMN_POINT_D, zoneInfo.getPointD());

            if (!getZoneId(zoneInfo.getId())) {

                getWritableDatabase().insert(Db_Schema.ZoneInfo.TABLE_ZONE, null, values);

                Log.d(TAG, "Zone Added");
                return true;
            } else {

                getWritableDatabase().update(Db_Schema.ZoneInfo.TABLE_ZONE, values, Db_Schema.ZoneInfo.COLUMN_ZONE_ID + "=" + zoneInfo.getId(), null);

                Log.d(TAG, "Zone updated");
                return false;
            }
        } finally {
            try {
                getWritableDatabase().close();
            } catch (Exception ignore) {
            }
        }

    }

    public boolean insert_ise_wifi(int iseId, String iseWifi, String isePassword) {
        try {
            ContentValues values = new ContentValues();
            values.put(Db_Schema.IseWiFI.COLUMN_IseWiFi_ID, iseId);
            values.put(Db_Schema.IseWiFI.COLUMN_IseUsername, iseWifi);
            values.put(Db_Schema.IseWiFI.COLUMN_IsePassword, isePassword);

            if (!getIseId(iseId)) {

                getWritableDatabase().insert(Db_Schema.IseWiFI.TABLE_IseWiFi, null, values);

                Log.d(TAG, "Zone Added");
                return true;
            } else {

                getWritableDatabase().update(Db_Schema.IseWiFI.TABLE_IseWiFi, values, Db_Schema.IseWiFI.COLUMN_IseWiFi_ID + "=" +iseId, null);

                Log.d(TAG, "Zone updated");
                return false;
            }
        } finally {
            try {
                getWritableDatabase().close();
            } catch (Exception ignore) {
            }
        }

    }

    public String getWiFiUserName(int iseId) {
        SQLiteDatabase db = getReadableDatabase();
        String userName="";

        String[] params = new String[]{"" + iseId};

        Cursor cursor = db.query(Db_Schema.IseWiFI.TABLE_IseWiFi, null,
                Db_Schema.IseWiFI.COLUMN_IseWiFi_ID + " = ?", params,
                null, null, null);

        if (cursor != null && cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndex(Db_Schema.IseWiFI.COLUMN_IseUsername));
        }

        return userName;
    }

    public String getWiFiPassword(int iseId) {
        SQLiteDatabase db = getReadableDatabase();
        String userName="";

        String[] params = new String[]{"" + iseId};

        Cursor cursor = db.query(Db_Schema.IseWiFI.TABLE_IseWiFi, null,
                Db_Schema.IseWiFI.COLUMN_IseWiFi_ID + " = ?", params,
                null, null, null);

        if (cursor != null && cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndex(Db_Schema.IseWiFI.COLUMN_IsePassword));
        }

        return userName;
    }

    public boolean getZoneId(int uid) {
        String read_query = " SELECT " + Db_Schema.ZoneInfo.COLUMN_ZONE_ID + " FROM " + Db_Schema.ZoneInfo.TABLE_ZONE;
        Cursor cursor = getReadableDatabase().rawQuery(read_query, null);

        try {
            if (cursor == null)
                return false;

            if (cursor.moveToFirst()) {
                Log.d(TAG, "getUid: " + cursor.getInt(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_ZONE_ID)));
                do {
                    if (uid == cursor.getInt(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_ZONE_ID))) {
                        return true;
                    }
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {

        } finally {
            try {
                cursor.close();
            } catch (Exception ignore) {
            }
        }
        return false;
    }

    public boolean getIseId(int uid) {
        String read_query = " SELECT " + Db_Schema.IseWiFI.COLUMN_IseWiFi_ID + " FROM " + Db_Schema.IseWiFI.TABLE_IseWiFi;
        Cursor cursor = getReadableDatabase().rawQuery(read_query, null);

        try {
            if (cursor == null)
                return false;

            if (cursor.moveToFirst()) {
                Log.d(TAG, "getUid: " + cursor.getInt(cursor.getColumnIndex(Db_Schema.IseWiFI.COLUMN_IseWiFi_ID)));
                do {
                    if (uid == cursor.getInt(cursor.getColumnIndex(Db_Schema.IseWiFI.COLUMN_IseWiFi_ID))) {
                        return true;
                    }
                } while (cursor.moveToNext());
            }
        } catch (SQLiteException e) {

        } finally {
            try {
                cursor.close();
            } catch (Exception ignore) {
            }
        }
        return false;
    }

    public ZoneInfo getZoneInfo(String uid) {
        SQLiteDatabase db = getReadableDatabase();
        ZoneInfo zoneInfo = null;
        try {
            String[] params = new String[]{uid};

            Cursor cursor = db.query(Db_Schema.ZoneInfo.TABLE_ZONE, null,
                    Db_Schema.ZoneInfo.COLUMN_ZONE_ID + " = ?", params,
                    null, null, null);

            try {
                if (cursor != null && cursor.moveToNext()) {
                    Log.d("dev", "database read: " + cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_CENTER)));
                    zoneInfo = new ZoneInfo();
                    zoneInfo.setId(cursor.getInt(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_ZONE_ID)));
                    zoneInfo.setCenterPoint(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_CENTER)));
                    zoneInfo.setZoneName(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_ZONE_NAME)));
                    zoneInfo.setPointA(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_POINT_A)));
                    zoneInfo.setPointB(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_POINT_B)));
                    zoneInfo.setPointC(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_POINT_C)));
                    zoneInfo.setPointD(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_POINT_D)));
                }
            } catch (SQLiteException e) {

            } finally {
                try {
                    cursor.close();
                } catch (Exception ignore) {
                }
            }
        } finally {
            try {
                db.close();
            } catch (Exception ignore) {
            }
        }
        return zoneInfo;
    }

    public ZoneInfo getZoneInfoByName(String zoneName) {
        SQLiteDatabase db = getReadableDatabase();
        ZoneInfo zoneInfo = null;
        try {
            String[] params = new String[]{zoneName};

            Cursor cursor = db.query(Db_Schema.ZoneInfo.TABLE_ZONE, null,
                    Db_Schema.ZoneInfo.COLUMN_ZONE_NAME + " = ?", params,
                    null, null, null);

            try {
                if (cursor != null && cursor.moveToNext()) {
                    Log.d("dev", "database read: " + cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_CENTER)));
                    zoneInfo = new ZoneInfo();
                    zoneInfo.setId(cursor.getInt(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_ZONE_ID)));
                    zoneInfo.setCenterPoint(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_CENTER)));
                    zoneInfo.setZoneName(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_ZONE_NAME)));
                    zoneInfo.setPointA(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_POINT_A)));
                    zoneInfo.setPointB(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_POINT_B)));
                    zoneInfo.setPointC(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_POINT_C)));
                    zoneInfo.setPointD(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_POINT_D)));
                }
            } catch (SQLiteException e) {

            } finally {
                try {
                    cursor.close();
                } catch (Exception ignore) {
                }
            }
        } finally {
            try {
                db.close();
            } catch (Exception ignore) {
            }
        }
        return zoneInfo;
    }

    public ArrayList<ZoneInfo> getAllZoneInfo() {

        ArrayList<ZoneInfo> list = new ArrayList<ZoneInfo>();

        String selectQuery = "SELECT  * FROM " + Db_Schema.ZoneInfo.TABLE_ZONE;

        SQLiteDatabase db = this.getReadableDatabase();
        try {

            Cursor cursor = db.rawQuery(selectQuery, null);
            try {

                if (cursor.moveToFirst()) {
                    do {
                        ZoneInfo zoneInfo = new ZoneInfo();
                        zoneInfo.setId(cursor.getInt(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_ZONE_ID)));
                        zoneInfo.setCenterPoint(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_CENTER)));
                        zoneInfo.setZoneName(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_ZONE_NAME)));
                        zoneInfo.setPointA(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_POINT_A)));
                        zoneInfo.setPointB(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_POINT_B)));
                        zoneInfo.setPointC(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_POINT_C)));
                        zoneInfo.setPointD(cursor.getString(cursor.getColumnIndex(Db_Schema.ZoneInfo.COLUMN_POINT_D)));

                        list.add(zoneInfo);
                    } while (cursor.moveToNext());
                }

            } finally {
                try {
                    cursor.close();
                } catch (Exception ignore) {
                }
            }

        } finally {
            try {
                db.close();
            } catch (Exception ignore) {
            }
        }

        return list;
    }

    public boolean insert_notification(String text) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");

            ContentValues values = new ContentValues();
            values.put(Db_Schema.NotificationText.COLUMN_NOTIFICATION_TEXT, text);
            values.put(Db_Schema.NotificationText.COLUMN_TIME_STAMP, System.currentTimeMillis());
            values.put(Db_Schema.NotificationText.COLUMN_NOTIFICATION_DATE, sdf.format(new Date()));

            getWritableDatabase().insert(Db_Schema.NotificationText.TABLE_NOTIFICATION, null, values);

            Log.d(TAG, "Notification Added");
            return true;
        } finally {
            try {
                getWritableDatabase().close();
            } catch (Exception ignore) {
            }
        }

    }

    public ArrayList<Notification> getAllNotification() {

        ArrayList<Notification> list = new ArrayList<Notification>();

        String selectQuery = "SELECT  * FROM " + Db_Schema.NotificationText.TABLE_NOTIFICATION;

        SQLiteDatabase db = this.getReadableDatabase();
        try {

            Cursor cursor = db.rawQuery(selectQuery, null);
            try {

                if (cursor.moveToFirst()) {
                    do {
                        Notification notification = new Notification();
                        notification.setNotificationText(cursor.getString(cursor.getColumnIndex(Db_Schema.NotificationText.COLUMN_NOTIFICATION_TEXT)));
                        notification.setTimeStamp(cursor.getInt(cursor.getColumnIndex(Db_Schema.NotificationText.COLUMN_TIME_STAMP)));
                        notification.setDate(cursor.getString(cursor.getColumnIndex(Db_Schema.NotificationText.COLUMN_NOTIFICATION_DATE)));

                        list.add(notification);

                    } while (cursor.moveToNext());
                }

            } finally {
                try {
                    cursor.close();
                } catch (Exception ignore) {
                }
            }

        } finally {
            try {
                db.close();
            } catch (Exception ignore) {
            }
        }

        return list;
    }

}
