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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.hipla.smartoffice_new.fragment.RequestMeetingDetailFragment;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.model.UpcomingMeetings;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.utils.CONST;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Created by FNSPL on 3/29/2018.
 */

public class ScheduleETADetectionService extends JobService implements StringRequestListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private long UPDATE_INTERVAL = 3000;  /* 10 secs */
    private long FASTEST_INTERVAL = UPDATE_INTERVAL / 2; /* 10 secs */
    private JobParameters parameters;
    private boolean isLoadingData = false, isJobFinished = false;
    private double latitude, longitude;
    private float DISPLACEMENT = 2;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public String ACTION_PROCESS_UPDATES = "procedd_update";
    private TimerTask mTimerTask = null;
    private Timer mTimer = new Timer();

    @Override
    public boolean onStartJob(JobParameters params) {
        parameters = params;
        if (Paper.book().read(CONST.CURRENT_MEETING_DATA) != null) {

            //CONST.showNotificationForLocation(this, getString(R.string.app_name), "ETA Started", 5005);

            final UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

            initializeLocationManager();

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

                            if (latitude != 0 && longitude != 0) {
                                getETA(latitude, longitude);
                            }

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(meetingDateTimeEnd);

                            if (new Date().compareTo(calendar.getTime()) > 0) {
                                stopLocationUpdates();

                                mTimer.cancel();
                                mTimerTask.cancel();

                                CONST.scheduleEndMeetingJob(ScheduleETADetectionService.this, 1300, CONST.SCHEDULE_END_MEETING_JOB_ID);

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

            mTimer.schedule(mTimerTask, 1 * 30 * 1000, 1 * 60 * 1000);

            CONST.showNotifications(ScheduleETADetectionService.this, getString(R.string.app_name),
                    String.format(getString(R.string.meeting_going_to_start), meetingDetail.getGuest().getContact(), meetingDetail.getFromtime()), 200);

            if (!isLocationEnable()) {
                CONST.showNotificationForLocation(ScheduleETADetectionService.this, getString(R.string.app_name),
                        getString(R.string.enable_your_gps_for_receive_alerts_for_meeting), 1001);
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        //CONST.showNotificationForLocation(this, getString(R.string.app_name), "ETA Stoped", 5005);

        if (mTimer != null && mTimerTask != null) {
            mTimer.cancel();
            mTimerTask.cancel();
            mTimerTask = null;
            mTimer = null;
        }

        if (isJobFinished) {

            stopLocationUpdates();

            return false;
        } else {
            if (Paper.book().read(CONST.CURRENT_MEETING_DATA) != null) {
                CONST.scheduleMeetingETAJob(getApplicationContext(), 1000,
                        CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
            }else{

            }

            isLoadingData = false;
            return false;
        }
    }

    private void getETA(double latitude, double longitude) {

        ScheduleETADetectionService.this.latitude = latitude;
        ScheduleETADetectionService.this.longitude = longitude;

        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            String DESTINATION_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                    "origins=" + latitude + "," + longitude + "&destinations=" + CONST.DESTINATION_LAT + "," + CONST.DESTINATION_LONG +
                    "&mode=driving&key=" + CONST.DISTANCE_API_KEY;


            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();

                new PostStringRequest(getApplicationContext(), requestParameter, ScheduleETADetectionService.this, "upcomingMeetings",
                        DESTINATION_URL);
            }

        } catch (Exception ex) {

        } finally {
            isLoadingData = true;
        }
    }

    private void initializeLocationManager() {

        mGoogleApiClient = new GoogleApiClient.Builder(ScheduleETADetectionService.this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(ScheduleETADetectionService.this)
                .addOnConnectionFailedListener(ScheduleETADetectionService.this)
                .build();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    public void stopLocationUpdates() {

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .removeLocationUpdates(mGoogleApiClient, ScheduleETADetectionService.this);
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

                    JSONObject elementOnject = elements.getJSONObject(0).getJSONObject("duration");

                    int timeTOReachInSeconds = elementOnject.getInt("value");
                    String timeTOReachInMins = elementOnject.optString("text");

                    String distanceInKm = elementOnject.optString("text");

                    if (timeTOReachInSeconds < CONST.TIME_BEFORE_DETECTION_HRS * 60 * 60) {
                        UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

                        if (meetingDetail != null) {
                            Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate()
                                    + " " + meetingDetail.getFromtime());

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(meetingDateTime);

                            Calendar cal1 = Calendar.getInstance();
                            cal1.setTime(new Date());

                            long timeDiff = cal.getTimeInMillis() - cal1.getTimeInMillis();
                            int timeInSec = (int) timeDiff / 1000;
                            if (timeInSec < -CONST.EXTRA_TIME_BEFORE_DETECTION_MINS * 60) {
                                return;
                            }

                            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

                            if (timeTOReachInSeconds <= timeInSec) {

                                if (timeTOReachInSeconds <= (timeInSec - ((CONST.EXTRA_TIME_BEFORE_DETECTION_MINS + 5) * 60))) {
                                    //reschedule ETA

                                    if (Paper.book().read(CONST.DISTANCE_NOTIFICATION, true) &&
                                            userData != null && userData.getUsertype().equalsIgnoreCase("Guest"))
                                        CONST.showNotifications(ScheduleETADetectionService.this, getString(R.string.app_name),
                                                String.format(getString(R.string.you_will_reach_your_destination_in_mins), "" + timeTOReachInMins), "Info");

                                    if (timeTOReachInSeconds < (CONST.EXTRA_TIME_BEFORE_DETECTION_MINS * 2 * 60)) {
                                        CONST.scheduleStartMeetingJob(ScheduleETADetectionService.this, 1000,
                                                CONST.SCHEDULE_START_MEETING_JOB_ID);

                                        Paper.book().read(CONST.IS_DETECTION_STARTED, false);

                                    } else if ((timeInSec - (timeTOReachInSeconds + CONST.EXTRA_TIME_BEFORE_DETECTION_MINS * 60)) > 0) {
                                        CONST.scheduleMeetingETAJob(ScheduleETADetectionService.this,
                                                ((CONST.EXTRA_TIME_BEFORE_DETECTION_MINS + 5) * 60) * 1000,
                                                CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
                                    } else {
                                        CONST.scheduleMeetingETAJob(ScheduleETADetectionService.this, 1000,
                                                CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
                                    }

                                } else {
                                    //start geofencing detec tion

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

                                    CONST.scheduleStartMeetingJob(ScheduleETADetectionService.this, 1000,
                                            CONST.SCHEDULE_START_MEETING_JOB_ID);
                                }

                                Paper.book().delete(CONST.RUNNING_LATE);

                            } else {
                                //reschedule
                                // strat timer now

                                double distance = CONST.distance(latitude, longitude,
                                        CONST.DESTINATION_LAT, CONST.DESTINATION_LONG, "K");

                                if (distance > 1.5) {
                                    Paper.book().write(CONST.RUNNING_LATE, true);

                                    CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                            getString(R.string.you_are_running_late), "Info");
                                }

                                CONST.scheduleStartMeetingJob(ScheduleETADetectionService.this, 1000,
                                        CONST.SCHEDULE_START_MEETING_JOB_ID);

                            }

                        }
                    } else {
                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                getString(R.string.you_will_late_and_reschedule), "Info");

                        CONST.scheduleStartMeetingJob(ScheduleETADetectionService.this, 1000, CONST.SCHEDULE_START_MEETING_JOB_ID);

                        Paper.book().write(CONST.RUNNING_LATE, true);
                    }

                    jobFinished(parameters, false);
                    isJobFinished = true;

                } catch (Exception ex) {
                    ex.printStackTrace();

                    isLoadingData = false;
                    isJobFinished = false;
                    getETA(latitude, longitude);
                }
                break;
        }

    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {

    }

    @Override
    public void onStarted() {

    }

    public void onLocation(Location location) {
        if (location != null && !isLoadingData && !isJobFinished)
            getETA(location.getLatitude(), location.getLongitude());

        if (isJobFinished) {
            jobFinished(parameters, false);
            mTimer.cancel();
            mTimerTask.cancel();

            stopLocationUpdates();
        }
    }

    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(ScheduleETADetectionService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(ScheduleETADetectionService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, ScheduleETADetectionService.this);

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
        if (mLocation != null) {
            getETA(mLocation.getLatitude(), mLocation.getLongitude());
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
