package com.hipla.smartoffice_new.services;

import android.app.ActivityManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;

import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.model.UpcomingMeetings;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.utils.CONST;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import io.paperdb.Paper;

/**
 * Created by FNSPL on 4/3/2018.
 */

public class ScheduleMeetingFinishService extends JobService implements StringRequestListener {

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    @Override
    public boolean onStartJob(JobParameters params) {
        try {
            CONST.showNotificationForLocation(this, getString(R.string.app_name), "Meeting Has Finished Successfully", 5001);

            UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

            if (meetingDetail != null) {
                Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate() + " " + meetingDetail.getTotime());

                if (new Date().compareTo(meetingDateTime) > 0) {
                    stopCurrentMeeting();

                    Paper.book().delete(CONST.IS_DETECTION_STARTED);
                    Paper.book().delete(CONST.CURRENT_MEETING_DATA);
                    Paper.book().delete(CONST.NEEDS_TO_RESCHEDULE);

                    Intent intent = new Intent();
                    intent.setAction("intent.start.Finish.Navigation");
                    sendBroadcast(intent);

                    //checkForUpcomingMeeting();
                    CONST.scheduleMeetingFetchJob(ScheduleMeetingFinishService.this, 1300, CONST.FETCH_JOB_ID);
                    stopService(new Intent(getApplicationContext(), MyNavigationService.class));
                }
            }else{
                Paper.book().delete(CONST.IS_DETECTION_STARTED);
                Paper.book().delete(CONST.CURRENT_MEETING_DATA);
                Paper.book().delete(CONST.NEEDS_TO_RESCHEDULE);

                Intent intent = new Intent();
                intent.setAction("intent.start.Finish.Navigation");
                sendBroadcast(intent);

                CONST.scheduleMeetingFetchJob(ScheduleMeetingFinishService.this, 1300, CONST.FETCH_JOB_ID);
                stopService(new Intent(getApplicationContext(), MyNavigationService.class));

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
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

    private void stopCurrentMeeting() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
            UpcomingMeetings upcomingMeetings = Paper.book().read(CONST.CURRENT_MEETING_DATA);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", upcomingMeetings.getId()));
                requestParameter.put("status", String.format("%s", "end"));

                new PostStringRequest(ScheduleMeetingFinishService.this, requestParameter,
                        ScheduleMeetingFinishService.this, "setMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
}
