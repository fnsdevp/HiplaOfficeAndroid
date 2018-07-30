package com.hipla.smartoffice_new.services;

import android.Manifest;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.database.Db_Helper;
import com.hipla.smartoffice_new.model.UpcomingMeetings;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.utils.CONST;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Created by FNSPL on 3/29/2018.
 */

public class ScheduleMeetingProcessService extends JobService implements StringRequestListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 10000;  /* 15 secs */
    private long FASTEST_INTERVAL = UPDATE_INTERVAL / 2; /* 10 secs */
    private JobParameters parameters;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private boolean isLoadingData = false, isAbleToReach = false;
    private double latitude, longitude;
    private UpcomingMeetings meetingDetail;
    private float DISPLACEMENT = 2;
    private boolean isJobFinished = false;
    private boolean is200MNotificationShown = false, is400MNotificationShown = false, isWelcomeNotificationShown = false, isLateNotificationShown = false;
    private TimerTask mTimerTask = null;
    private Timer mTimer = new Timer();
    private int notificationCount = 0;
    private DecimalFormat df = new DecimalFormat();
    public String ACTION_PROCESS_UPDATES = "procedd_update";

    @Override
    public boolean onStartJob(JobParameters params) {
        df.setMaximumFractionDigits(1);
        parameters = params;
        Paper.book().write(CONST.GEO_FENCING_STARTED, true);
        //CONST.showNotificationForLocation(this, getString(R.string.app_name), "Geo Fencing Started", 5000);

        if (Paper.book().read(CONST.CURRENT_MEETING_DATA) != null) {

            meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        //Looper.prepare();
                        if (!isLoadingData && !isJobFinished && Paper.book().read(CONST.CURRENT_MEETING_DATA) != null) {
                            final Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate()
                                    + " " + meetingDetail.getFromtime());

                            Date meetingDateTimeEnd = dateFormat.parse(meetingDetail.getFdate()
                                    + " " + meetingDetail.getTotime());


                            if (new Date().compareTo(meetingDateTime) <= 0) {
                                CONST.cancelLocationDetection(ScheduleMeetingProcessService.this, CONST.SCHEDULE_LOCATION_JOB_ID);
                                getETA(latitude, longitude);
                            } else {
                                checkGeoFencing(meetingDetail);
                            }

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(meetingDateTimeEnd);
                            calendar.add(Calendar.MINUTE, CONST.EXTRA_TIME_BEFORE_DETECTION_MINS);

                            if (new Date().compareTo(calendar.getTime()) > 0) {
                                stopLocationUpdates();

                                mTimer.cancel();
                                mTimerTask.cancel();

                                CONST.scheduleEndMeetingJob(ScheduleMeetingProcessService.this, 1300, CONST.SCHEDULE_END_MEETING_JOB_ID);

                                jobFinished(parameters, false);
                                isJobFinished = true;
                            }

                        } else {
                            jobFinished(parameters, false);
                            isJobFinished = true;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        //Looper.loop();
                    }
                }
            };
            mTimer.schedule(mTimerTask, 1 * 30 * 1000, 3 * 60 * 1000);

            if (!isLocationEnable()) {
                CONST.showNotificationForLocation(ScheduleMeetingProcessService.this, getString(R.string.app_name),
                        getString(R.string.enable_your_gps_for_receive_alerts_for_meeting), 1001);
            }

            initializeLocationManager();

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        //CONST.showNotificationForLocation(this, getString(R.string.app_name), "Geo Fencing Stoped", 5000);
        if (Paper.book().read(CONST.CURRENT_MEETING_DATA) != null) {
            Date meetingDateTime = null, meetingDateTimeEnd = null;
            try {
                stopLocationUpdates();

                if (mTimer != null && mTimerTask != null) {
                    mTimer.cancel();
                    mTimerTask.cancel();

                    mTimerTask = null;
                    mTimer = null;
                }

                Paper.book().delete(CONST.GEO_FENCING_STARTED);

                meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

                meetingDateTime = dateFormat.parse(meetingDetail.getFdate()
                        + " " + meetingDetail.getFromtime());

                meetingDateTimeEnd = dateFormat.parse(meetingDetail.getFdate()
                        + " " + meetingDetail.getTotime());

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                if (isJobFinished || Paper.book().read(CONST.CURRENT_MEETING_DATA) == null)
                    return false;
                else {

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(meetingDateTime);

                    Calendar cal1 = Calendar.getInstance();
                    cal1.setTime(meetingDateTimeEnd);

                    long timeDiff = cal1.getTimeInMillis() - cal.getTimeInMillis();
                    int timeInSec = (int) timeDiff / 1000;
                    int timeInMin = timeInSec / 60;

                    cal.add(Calendar.MINUTE, timeInMin);

                    if (meetingDateTime != null && (new Date().compareTo(cal.getTime()) <= 0)) {

                        if (Paper.book().read(CONST.CURRENT_MEETING_DATA) != null) {
                            CONST.scheduleStartMeetingJob(getApplicationContext(), 1000,
                                    CONST.SCHEDULE_START_MEETING_JOB_ID);
                        }

                        return false;
                    } else
                        return false;

                }
            }
        }else{
            return false;
        }
    }

    private void getETA(double latitude, double longitude) {

        ScheduleMeetingProcessService.this.latitude = latitude;
        ScheduleMeetingProcessService.this.longitude = longitude;

        Paper.book().write(CONST.LATITUDE, latitude);
        Paper.book().write(CONST.LONGITUDE, longitude);

        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            String DESTINATION_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                    "origins=" + latitude + "," + longitude + "&destinations=" + CONST.DESTINATION_LAT + "," + CONST.DESTINATION_LONG +
                    "&key=" + CONST.DISTANCE_API_KEY;


            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();

                new PostStringRequest(getApplicationContext(), requestParameter, ScheduleMeetingProcessService.this, "upcomingMeetings",
                        DESTINATION_URL);
            }

        } catch (Exception ex) {

        } finally {
            isLoadingData = true;
        }

    }

    private void setGeoFencing(String push_body) {

        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null && userData.getUsertype().equalsIgnoreCase("Guest") && meetingDetail != null) {

                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", "" + userData.getId());
                requestParameter.put("appid", "" + meetingDetail.getId());
                requestParameter.put("body", "" + push_body);

                new PostStringRequest(getApplicationContext(), requestParameter, ScheduleMeetingProcessService.this, "registerGeoFencing",
                        NetworkUtility.BASEURL + NetworkUtility.SEND_PUSH_TO_EMPLOYEE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initializeLocationManager() {

        mGoogleApiClient = new GoogleApiClient.Builder(ScheduleMeetingProcessService.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(ScheduleMeetingProcessService.this)
                .addOnConnectionFailedListener(ScheduleMeetingProcessService.this)
                .build();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public void stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, ScheduleMeetingProcessService.this);
        }

        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {

        switch (type) {
            case "upcomingMeetings":

                try {
                    JSONObject response = new JSONObject(result);
                    JSONArray rows = response.getJSONArray("rows");
                    JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");
                    int timeInSec = 0;

                    JSONObject elementOnject = elements.getJSONObject(0).getJSONObject("duration");
                    JSONObject distanceObj = elements.getJSONObject(0).getJSONObject("distance");

                    int timeTOReachInSeconds = elementOnject.getInt("value");
                    String timeTOReachInMins = elementOnject.getString("text");

                    String distanceInKmString = distanceObj.getString("text");
                    float distanceInKm = distanceObj.getLong("value");

                    UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);
                    if (meetingDetail != null) {
                        Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate()
                                + " " + meetingDetail.getFromtime());

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(meetingDateTime);

                        Calendar cal1 = Calendar.getInstance();
                        cal1.setTime(new Date());

                        long timeDiff = cal.getTimeInMillis() - cal1.getTimeInMillis();
                        timeInSec = (int) timeDiff / 1000;

                    }

                    UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

                    if (!is200MNotificationShown) {
                        if (timeTOReachInSeconds < timeInSec && userData != null && userData.getUsertype().equalsIgnoreCase("Guest")) {
                            isAbleToReach = true;

                            if (Paper.book().read(CONST.DISTANCE_NOTIFICATION, true))
                                CONST.showNotifications(ScheduleMeetingProcessService.this, getString(R.string.app_name),
                                        String.format(getString(R.string.you_will_reach_your_destination_in_mins_km), distanceInKmString, "" + timeTOReachInMins), "Info");

                            checkGeoFencing(meetingDetail);

                            Paper.book().delete(CONST.RUNNING_LATE);
                        } else {
                            //show notification for late for meeting
                            notificationCount++;

                            if (Paper.book().read(CONST.DISTANCE_NOTIFICATION, true)
                                    && userData != null && userData.getUsertype().equalsIgnoreCase("Guest")) {
                                if (timeTOReachInSeconds <= (timeInSec - (10 * 60))) {
                                    CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                            getString(R.string.you_are_running_late), "Info");

                                } else if (timeTOReachInSeconds > (timeInSec - (10 * 60)) &&
                                        timeTOReachInSeconds <= (timeInSec)) {

                                    CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                            getString(R.string.you_will_be_late), "Info");
                                } else {
                                    CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                            getString(R.string.you_will_late_and_reschedule), "Info");
                                }
                            }
                            //jobFinished(parameters, false);
                            Paper.book().write(CONST.RUNNING_LATE, true);
                        }
                    } else {
                        checkGeoFencing(meetingDetail);
                    }

                    isLoadingData = false;

                } catch (Exception ex) {
                    ex.printStackTrace();

                    isLoadingData = false;
                    getETA(latitude, longitude);
                }

                isLoadingData = false;
                break;
        }

    }

    private void checkGeoFencing(UpcomingMeetings meetingDetail) throws ParseException {
        double distance = CONST.distance(latitude, longitude,
                CONST.DESTINATION_LAT, CONST.DESTINATION_LONG, "K");

        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

        if (distance >= .6 && distance < 1 && !is400MNotificationShown) {

            is400MNotificationShown = true;

            if (meetingDetail != null && Paper.book().read(CONST.DISTANCE_NOTIFICATION, true) &&
                    userData != null && userData.getUsertype().equalsIgnoreCase("Guest")) {

                setGeoFencing(String.format(getString(R.string.guest_will_reach), userData.getFname() + " " + userData.getLname(), "" + df.format(distance)));

                CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                        String.format(getString(R.string.you_will_reach_400m), "" + df.format(distance)), "Info");

            }

            Paper.book().delete(CONST.RUNNING_LATE);
            Paper.book().delete(CONST.IS_DETECTION_STARTED);

            is200MNotificationShown = false;
            isWelcomeNotificationShown = false;

        } else if (distance >= .2 && distance < .6 && !is200MNotificationShown) {

            is200MNotificationShown = true;

            if (meetingDetail != null && Paper.book().read(CONST.DISTANCE_NOTIFICATION, true) &&
                    userData != null && userData.getUsertype().equalsIgnoreCase("Guest")) {
                setGeoFencing(String.format(getString(R.string.guest_about_to_reach), userData.getFname() + " " + userData.getLname()));


                CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                        getString(R.string.you_will_reach_200m), "Info");

            }

            Paper.book().delete(CONST.RUNNING_LATE);
            Paper.book().read(CONST.IS_DETECTION_STARTED, true);

        } else if (distance < .1 && !isWelcomeNotificationShown) {
            //start indoor navigation detection

            Paper.book().delete(CONST.RUNNING_LATE);
            isWelcomeNotificationShown = true;

            if (meetingDetail != null &&
                    userData != null && userData.getUsertype().equalsIgnoreCase("Guest")) {

                CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                        getString(R.string.welcome_message), "Info");

                setGeoFencing(String.format(getString(R.string.guest_has_arrived), userData.getFname() + " " + userData.getLname()));

            }

            Paper.book().write(CONST.IS_DETECTION_STARTED, true);
            Paper.book().delete(CONST.GEO_FENCING_STARTED);

            if (meetingDetail != null) {
                Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate() + " " + meetingDetail.getTotime());

                Calendar cal = Calendar.getInstance();
                cal.setTime(meetingDateTime);
                cal.add(Calendar.MINUTE, CONST.TIME_BEFORE_EXTEND_MEETING_IN_MIN);

                Calendar cal1 = Calendar.getInstance();
                cal1.setTime(new Date());

                if ((cal.getTimeInMillis() - cal1.getTimeInMillis()) > 0) {
                    CONST.scheduleExtendMeetingJob(getApplicationContext(),
                            cal.getTimeInMillis() - cal1.getTimeInMillis(), CONST.SCHEDULE_EXTEND_MEETING_JOB_ID);
                } else {
                    CONST.scheduleExtendMeetingJob(getApplicationContext(),
                            1300, CONST.SCHEDULE_EXTEND_MEETING_JOB_ID);
                }
            }

            Intent intent = new Intent();
            intent.setAction("intent.start.Navigation");
            sendBroadcast(intent);

            isJobFinished = true;
            if(mTimer!=null && mTimerTask!=null) {
                mTimer.cancel();
                mTimerTask.cancel();

                mTimerTask = null;
                mTimer = null;
            }
            jobFinished(parameters, false);
        }
    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {

    }

    @Override
    public void onStarted() {

    }

    public void onLocation(Location location) {
        try {
            if (location != null && !isJobFinished) {
                Paper.book().write(CONST.LATITUDE, location.getLatitude());
                Paper.book().write(CONST.LONGITUDE, location.getLatitude());

                double distance = CONST.distance(location.getLatitude(), location.getLongitude(),
                        CONST.DESTINATION_LAT, CONST.DESTINATION_LONG, "K");

                ScheduleMeetingProcessService.this.latitude = location.getLatitude();
                ScheduleMeetingProcessService.this.longitude = location.getLongitude();

                UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

                if (meetingDetail != null) {
                    Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate() + " " + meetingDetail.getTotime());

                    if (new Date().compareTo(meetingDateTime) < 0) {
                        if (distance >= .6 && distance < 1 && !is400MNotificationShown) {

                            Paper.book().delete(CONST.RUNNING_LATE);
                            Paper.book().delete(CONST.IS_DETECTION_STARTED);

                            is200MNotificationShown = false;
                            isWelcomeNotificationShown = false;
                            is400MNotificationShown = true;

                            if (meetingDetail != null && Paper.book().read(CONST.DISTANCE_NOTIFICATION, true) &&
                                    userData != null && userData.getUsertype().equalsIgnoreCase("Guest")) {

                                setGeoFencing(String.format(getString(R.string.guest_will_reach), userData.getFname() + " " + userData.getLname()
                                        , "" + df.format(distance)));

                                CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                        String.format(getString(R.string.you_will_reach_400m), "" + df.format(distance)), "Info");

                            }
                        } else if (distance >= .2 && distance < .6 && !is200MNotificationShown) {

                            Paper.book().delete(CONST.RUNNING_LATE);
                            is200MNotificationShown = true;
                            Paper.book().read(CONST.IS_DETECTION_STARTED, true);

                            if (meetingDetail != null && Paper.book().read(CONST.DISTANCE_NOTIFICATION, true) &&
                                    userData != null && userData.getUsertype().equalsIgnoreCase("Guest")) {
                                setGeoFencing(String.format(getString(R.string.guest_about_to_reach), userData.getFname() + " " + userData.getLname()));

                                CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                        getString(R.string.you_will_reach_200m), "Info");

                            }

                        } else if (distance < .1 && !isWelcomeNotificationShown) {
                            //start indoor navigation detection

                            Paper.book().delete(CONST.RUNNING_LATE);
                            isWelcomeNotificationShown = true;
                            Paper.book().write(CONST.IS_DETECTION_STARTED, true);

                            if (meetingDetail != null &&
                                    userData != null && userData.getUsertype().equalsIgnoreCase("Guest")) {

                                CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                        getString(R.string.welcome_message), "Info");

                                setGeoFencing(String.format(getString(R.string.guest_has_arrived), userData.getFname() + " " + userData.getLname()));
                            }

                            Paper.book().delete(CONST.GEO_FENCING_STARTED);

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(meetingDateTime);
                            cal.add(Calendar.MINUTE, CONST.TIME_BEFORE_EXTEND_MEETING_IN_MIN);

                            Calendar cal1 = Calendar.getInstance();
                            cal1.setTime(new Date());

                            if ((cal.getTimeInMillis() - cal1.getTimeInMillis()) > 0) {
                                CONST.scheduleExtendMeetingJob(getApplicationContext(),
                                        cal.getTimeInMillis() - cal1.getTimeInMillis(), CONST.SCHEDULE_EXTEND_MEETING_JOB_ID);
                            } else {
                                CONST.scheduleExtendMeetingJob(getApplicationContext(),
                                        1300, CONST.SCHEDULE_EXTEND_MEETING_JOB_ID);
                            }

                            Intent intent = new Intent();
                            intent.setAction("intent.start.Navigation");
                            sendBroadcast(intent);

                            isJobFinished = true;
                            if(mTimer!=null && mTimerTask!=null) {
                                mTimer.cancel();
                                mTimerTask.cancel();

                                mTimerTask = null;
                                mTimer = null;
                            }
                            jobFinished(parameters, false);
                        }

                    }
                }

            } else if (isJobFinished) {
                jobFinished(parameters, false);
                if(mTimer!=null && mTimerTask!=null) {
                    mTimer.cancel();
                    mTimerTask.cancel();

                    mTimerTask = null;
                    mTimer = null;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(ScheduleMeetingProcessService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(ScheduleMeetingProcessService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, ScheduleMeetingProcessService.this);

    }

    private boolean isLocationEnable() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsProviderEnabled, isNetworkProviderEnabled;
        isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        return isGpsProviderEnabled;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        startLocationUpdates();
        Location mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLocation!=null){
            onLocation(mLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (Paper.book().read(CONST.CURRENT_MEETING_DATA) != null) {
            onLocation(location);
        }
    }

}
