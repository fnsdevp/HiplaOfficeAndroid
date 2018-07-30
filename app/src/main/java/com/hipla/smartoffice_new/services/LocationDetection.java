package com.hipla.smartoffice_new.services;

import android.Manifest;
import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
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
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.model.UpcomingMeetings;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.utils.CONST;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import io.paperdb.Paper;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * Created by FNSPL on 3/29/2018.
 */

public class LocationDetection extends JobService implements StringRequestListener {

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 10000;  /* 10 secs */
    private long FASTEST_INTERVAL = UPDATE_INTERVAL/2; /* 10 secs */
    private float DISPLACEMENT = 1;
    private JobParameters parameters;
    private boolean isLoadingData = false, jobFinished = false;
    private double latitude, longitude;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private boolean is200MNotificationShown = false, is400MNotificationShown = false, isWelcomeNotificationShown = false, isLateNotificationShown = false;
    private DecimalFormat df = new DecimalFormat();

    @Override
    public boolean onStartJob(JobParameters params) {
        df.setMaximumFractionDigits(1);
        parameters = params;
        if (Paper.book().read(CONST.CURRENT_MEETING_DATA) != null && !Paper.book().read(CONST.IS_DETECTION_STARTED, false)) {

            if(!isLocationEnable()){
                CONST.showNotificationForLocation(LocationDetection.this, getString(R.string.app_name),
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

        stopLocationUpdates();

        return false;
    }

    private void initializeLocationManager() {

        startLocationUpdates();
    }

    private void setGeoFencing(String push_body) {

        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
            UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

            if (userData != null && userData.getUsertype().equalsIgnoreCase("Guest") && meetingDetail != null) {

                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", "" + userData.getId());
                requestParameter.put("appid", "" + meetingDetail.getId());
                requestParameter.put("body", "" + push_body);

                new PostStringRequest(getApplicationContext(), requestParameter, LocationDetection.this, "registerGeoFencing",
                        NetworkUtility.BASEURL + NetworkUtility.SEND_PUSH_TO_EMPLOYEE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void stopLocationUpdates() {
        if(mFusedLocationClient!=null && mLocationCallback!=null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    public void onLocationChanged(Location location) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (location != null && !jobFinished && Paper.book().read(CONST.CURRENT_MEETING_DATA)!=null) {

                double distance = CONST.distance(location.getLatitude(), location.getLongitude(),
                        CONST.DESTINATION_LAT, CONST.DESTINATION_LONG, "K");

                UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

                Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate()
                        + " " + meetingDetail.getTotime());

                Calendar cal = Calendar.getInstance();
                cal.setTime(meetingDateTime);

                Calendar cal1 = Calendar.getInstance();
                cal1.setTime(new Date());

                //CONST.showNotifications(LocationDetection.this, getString(R.string.app_name), "Your distance :"+distance,2000);

                if (distance < .1 && (cal.getTimeInMillis() - cal1.getTimeInMillis()) > 0 && !isWelcomeNotificationShown) {

                    Paper.book().delete(CONST.RUNNING_LATE);
                    isWelcomeNotificationShown=true;

                    Paper.book().write(CONST.IS_DETECTION_STARTED, true);

                    if (meetingDetail != null &&
                            userData != null && userData.getUsertype().equalsIgnoreCase("Guest")) {

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                getString(R.string.welcome_message), "Info");

                        setGeoFencing(String.format(getString(R.string.guest_has_arrived), userData.getFname()+" "+userData.getLname()));
                    }

                    cal.add(Calendar.MINUTE, CONST.TIME_BEFORE_EXTEND_MEETING_IN_MIN);

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

                    jobFinished(parameters, false);
                    jobFinished = true;

                }else if(distance >= .2 && distance < .6 && !is200MNotificationShown && (cal.getTimeInMillis() - cal1.getTimeInMillis()) > 0){

                    Paper.book().delete(CONST.RUNNING_LATE);
                    is200MNotificationShown = true;
                    Paper.book().read(CONST.IS_DETECTION_STARTED, true);

                    if (meetingDetail != null && Paper.book().read(CONST.DISTANCE_NOTIFICATION, true) &&
                            userData != null && userData.getUsertype().equalsIgnoreCase("Guest")) {
                        setGeoFencing(String.format(getString(R.string.guest_about_to_reach), userData.getFname()+" "+userData.getLname()));


                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                getString(R.string.you_will_reach_200m), "Info");
                    }

                }else if(distance >= .6 && distance < 1 && !is400MNotificationShown && (cal.getTimeInMillis() - cal1.getTimeInMillis()) > 0){

                    Paper.book().delete(CONST.RUNNING_LATE);
                    Paper.book().delete(CONST.IS_DETECTION_STARTED);

                    is200MNotificationShown = false;
                    isWelcomeNotificationShown = false;
                    is400MNotificationShown = true;

                    if (meetingDetail != null && Paper.book().read(CONST.DISTANCE_NOTIFICATION, true) &&
                            userData != null && userData.getUsertype().equalsIgnoreCase("Guest")) {

                        setGeoFencing(String.format(getString(R.string.guest_will_reach), userData.getFname()+" "+userData.getLname()
                                , "" + df.format(distance)));

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name),
                                String.format(getString(R.string.you_will_reach_400m), "" + df.format(distance)), "Info");

                    }

                }else if(distance > 2 ){

                    jobFinished(parameters, false);
                    jobFinished = true;

                }

            }else{

                jobFinished(parameters, false);
                jobFinished = true;

                stopLocationUpdates();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        mLocationRequest.setMaxWaitTime(UPDATE_INTERVAL*2);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            };

        };
        // Get last known recent location using new Google Play Services SDK (v11+)
        mFusedLocationClient = getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                null);
    }

    private boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {

    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {

    }

    @Override
    public void onStarted() {

    }

    private boolean isLocationEnable(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGpsProviderEnabled, isNetworkProviderEnabled;
        isGpsProviderEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        return isGpsProviderEnabled;
    }

}
