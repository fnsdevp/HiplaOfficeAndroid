package com.hipla.smartoffice_new.services;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.database.Db_Helper;
import com.hipla.smartoffice_new.model.UpcomingMeetings;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.utils.CONST;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;

/**
 * Created by FNSPL on 3/28/2018.
 */

public class FetchMeetingInfoservice extends JobService implements StringRequestListener {

    private JobParameters parameters;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    @Override
    public boolean onStartJob(JobParameters params) {
        this.parameters = params;

        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
        if(userData!=null) {
            getUpcomingMeetings();

            return true;
        }else{
            return false;
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void getUpcomingMeetings() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));

                new PostStringRequest(getApplicationContext(), requestParameter, FetchMeetingInfoservice.this, "upcomingMeetings",
                        NetworkUtility.BASEURL + NetworkUtility.UPCOMING_MEETINGS);
            }

        } catch (Exception ex) {

        }
    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        switch (type) {
            case "upcomingMeetings":

                try {
                    setFetchMeetingOnNextDay();

                    JSONObject responseObject = new JSONObject(result);
                    if (responseObject.optString("status").equalsIgnoreCase("success")) {

                        JSONArray upcomingAppointments = responseObject.getJSONArray("apointments");

                        GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();

                        UpcomingMeetings[] upcomingMeetings = gson.fromJson(upcomingAppointments.toString(), UpcomingMeetings[].class);
                        List<UpcomingMeetings> upcomingMeetingsList = Arrays.asList(upcomingMeetings);

                        if (upcomingMeetingsList.size() > 0) {
                            Db_Helper db_helper = new Db_Helper(getApplicationContext());
                            db_helper.deleteMeetings();
                            db_helper.insertAllMeetings(upcomingMeetingsList);
                            db_helper.close();

                            if (Paper.book().read(CONST.CURRENT_MEETING_DATA) == null) {
                                checkForUpcomingMeeting();
                            }else{
                                UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

                                if(isContainMeeting(meetingDetail, upcomingMeetingsList)) {
                                    for (UpcomingMeetings data :
                                            upcomingMeetingsList) {

                                        if(data.getId() == meetingDetail.getId()){
                                            meetingDetail = data;
                                            Paper.book().write(CONST.CURRENT_MEETING_DATA, meetingDetail);
                                            break;
                                        }
                                    }

                                    Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate()
                                            + " " + meetingDetail.getTotime());

                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(meetingDateTime);

                                    if (cal.getTimeInMillis() + (CONST.TIME_AFTER_MEETING_IN_SEC * 1000) < System.currentTimeMillis()) {
                                        stopCurrentMeeting(""+meetingDetail.getId());

                                        Paper.book().delete(CONST.IS_DETECTION_STARTED);
                                        Paper.book().delete(CONST.CURRENT_MEETING_DATA);
                                        Paper.book().delete(CONST.NEEDS_TO_RESCHEDULE);

                                        CONST.cancelMeetingETAJob(FetchMeetingInfoservice.this, CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
                                        CONST.cancelEndMeetingJob(FetchMeetingInfoservice.this, CONST.SCHEDULE_END_MEETING_JOB_ID);
                                        CONST.cancelStartMeetingJob(FetchMeetingInfoservice.this, CONST.SCHEDULE_START_MEETING_JOB_ID);
                                        CONST.cancelExtendMeetingJob(FetchMeetingInfoservice.this, CONST.SCHEDULE_START_MEETING_JOB_ID);

                                        Intent intent = new Intent();
                                        intent.setAction("intent.start.Finish.Navigation");
                                        sendBroadcast(intent);

                                        checkForUpcomingMeeting();
                                    }else{

                                    }

                                    checkIfAnyPriorMeeting();
                                }else{
                                    //stopCurrentMeeting(""+meetingDetail.getId());
                                    CONST.cancelMeetingETAJob(FetchMeetingInfoservice.this, CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
                                    CONST.cancelEndMeetingJob(FetchMeetingInfoservice.this, CONST.SCHEDULE_END_MEETING_JOB_ID);
                                    CONST.cancelStartMeetingJob(FetchMeetingInfoservice.this, CONST.SCHEDULE_START_MEETING_JOB_ID);
                                    CONST.cancelExtendMeetingJob(FetchMeetingInfoservice.this, CONST.SCHEDULE_START_MEETING_JOB_ID);

                                    Paper.book().delete(CONST.CURRENT_MEETING_DATA);

                                    checkForUpcomingMeeting();
                                }


                            }
                        }

                    }else{
                        Paper.book().delete(CONST.CURRENT_MEETING_DATA);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    jobFinished(parameters, false);
                }
                break;
            case "setMeetingStatus":
                try {

                    JSONObject jsonObject = new JSONObject(result);

                }catch (Exception ex){
                    ex.printStackTrace();
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

    private void checkIfAnyPriorMeeting() {
        try{
            Date currentDateTime = new Date();

            Db_Helper db_helper = new Db_Helper(getApplicationContext());
            List<UpcomingMeetings> upcomingMeetingsList = db_helper.getUpcomingMeetings(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

            UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);
            int position= -1;

            for (int i = 0; i < upcomingMeetingsList.size(); i++) {
                UpcomingMeetings newMeeting = upcomingMeetingsList.get(i);

                if(newMeeting.getId()!=meetingDetail.getId()){

                    Date newMeetingDateTime = dateFormat.parse(newMeeting.getFdate()
                            + " " + newMeeting.getFromtime());

                    Date currentMeetingDateTime = dateFormat.parse(meetingDetail.getFdate()
                            + " " + meetingDetail.getFromtime());

                    if(newMeetingDateTime.compareTo(currentMeetingDateTime)<0 && currentDateTime.compareTo(newMeetingDateTime)<=0){
                        position = i;
                    }
                }
            }

            if(position!=-1){

                CONST.cancelMeetingETAJob(FetchMeetingInfoservice.this, CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
                CONST.cancelEndMeetingJob(FetchMeetingInfoservice.this, CONST.SCHEDULE_END_MEETING_JOB_ID);
                CONST.cancelStartMeetingJob(FetchMeetingInfoservice.this, CONST.SCHEDULE_START_MEETING_JOB_ID);
                CONST.cancelExtendMeetingJob(FetchMeetingInfoservice.this, CONST.SCHEDULE_START_MEETING_JOB_ID);

                Date newMeetingDateTime = dateFormat.parse(upcomingMeetingsList.get(position).getFdate()
                        + " " + upcomingMeetingsList.get(position).getFromtime());

                Calendar cal = Calendar.getInstance();
                cal.setTime(newMeetingDateTime);
                cal.add(Calendar.HOUR, CONST.TIME_BEFORE_DETECTION);

                if((cal.getTimeInMillis() - System.currentTimeMillis())>=0) {
                    CONST.scheduleMeetingETAJob(getApplicationContext(), cal.getTimeInMillis() - System.currentTimeMillis(),
                            CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
                }else{
                    CONST.scheduleMeetingETAJob(getApplicationContext(), 1000,
                            CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
                }
                Paper.book().write(CONST.CURRENT_MEETING_DATA, upcomingMeetingsList.get(position));

            }else{

            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void checkForUpcomingMeeting() {
        try {
            Date currentDateTime = new Date();

            Db_Helper db_helper = new Db_Helper(getApplicationContext());
            List<UpcomingMeetings> upcomingMeetingsList = db_helper.getUpcomingMeetings(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));

            for (int i = 0; i < upcomingMeetingsList.size(); i++) {

                Date meetingDateTime = dateFormat.parse(upcomingMeetingsList.get(i).getFdate()
                        + " " + upcomingMeetingsList.get(i).getFromtime());

                Date meetingDateTimeEnd = dateFormat.parse(upcomingMeetingsList.get(i).getFdate()
                        + " " + upcomingMeetingsList.get(i).getTotime());

                if (currentDateTime.compareTo(meetingDateTime) > 0) {

                    if(currentDateTime.compareTo(meetingDateTimeEnd) < 0){
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(meetingDateTime);
                        cal.add(Calendar.HOUR, CONST.TIME_BEFORE_DETECTION);

                        CONST.showNotifications(FetchMeetingInfoservice.this, getString(R.string.app_name),
                                String.format(getString(R.string.meeting_going_to_start),
                                        upcomingMeetingsList.get(i).getGuest().getContact(), upcomingMeetingsList.get(i).getFromtime()), 200);

                        Paper.book().write(CONST.CURRENT_MEETING_DATA, upcomingMeetingsList.get(i));
                        CONST.scheduleLocationDetectionJob(getApplicationContext(), 1000, CONST.SCHEDULE_LOCATION_JOB_ID);

                        break;
                    }else{
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(meetingDateTimeEnd);
                        cal.add(Calendar.MINUTE, CONST.EXTRA_TIME_BEFORE_DETECTION_MINS);

                        if(System.currentTimeMillis()>cal.getTimeInMillis()){
                            stopCurrentMeeting(""+upcomingMeetingsList.get(i).getId());
                        }
                    }

                } else {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(meetingDateTime);
                    cal.add(Calendar.HOUR, CONST.TIME_BEFORE_DETECTION);

                    if((cal.getTimeInMillis() - System.currentTimeMillis())>=0) {
                        CONST.scheduleMeetingETAJob(getApplicationContext(), cal.getTimeInMillis() - System.currentTimeMillis(),
                                CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
                    }else{
                        CONST.scheduleMeetingETAJob(getApplicationContext(), 1000,
                                CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
                    }
                    Paper.book().write(CONST.CURRENT_MEETING_DATA, upcomingMeetingsList.get(i));

                    break;
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    private void stopCurrentMeeting(String meeting_id) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", meeting_id));
                requestParameter.put("status", String.format("%s", "end"));

                new PostStringRequest(getApplicationContext(), requestParameter,
                        FetchMeetingInfoservice.this, "setMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean isContainMeeting(UpcomingMeetings upcomingMeetings, List<UpcomingMeetings> upcomingMeetingsList){
        for (UpcomingMeetings data:
             upcomingMeetingsList) {

            if(data.getId()==upcomingMeetings.getId()){
                return true;
            }
        }
        return false;
    }

    private void setFetchMeetingOnNextDay(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
        calendar.set(Calendar.HOUR_OF_DAY, 7);

        CONST.scheduleNextDayMeetingFetchJob(FetchMeetingInfoservice.this, calendar.getTimeInMillis()- System.currentTimeMillis(),
                CONST.NEXT_DAY_FETCH_JOB_ID);
    }
}
