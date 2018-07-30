package com.hipla.smartoffice_new.firebase;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hipla.smartoffice_new.database.Db_Helper;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.activity.LoginActivity;
import com.hipla.smartoffice_new.model.UpcomingMeetings;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.services.ScheduleMeetingFinishService;
import com.hipla.smartoffice_new.utils.CONST;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.paperdb.Paper;

public class MyFirebaseMessagingService extends FirebaseMessagingService implements StringRequestListener {

    private String TAG = "Firebase";

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "Notification Rewceived");
        if (remoteMessage != null && remoteMessage.getData() != null) {
            Log.d(TAG, "From: " + remoteMessage.getFrom());
            //Log.d(TAG, "Notification TripMessageData Body: " + remoteMessage.getNotification().getBody());
            Log.d(TAG, "Notification TripMessageData Data: " + remoteMessage.getData().toString());

            Map<String, String> data = remoteMessage.getData();

            ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
            // Get info from the currently active task
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            String activityName = taskInfo.get(0).topActivity.getClassName();

            //String body = remoteMessage.getNotification().getBody();

            try {

                UserData userDetails = Paper.book().read(NetworkUtility.USER_INFO);

                if (userDetails != null) {
                    if (data != null && data.containsKey("click_action") &&
                            data.get("click_action").equalsIgnoreCase("ConfirmAppointment")) {

                        CONST.scheduleMeetingFetchJob(MyFirebaseMessagingService.this, 1300, CONST.FETCH_JOB_ID);

                        Db_Helper db_helper = new Db_Helper(getApplicationContext());
                        db_helper.insert_notification(data.get("body"));

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name), data.get("body"), "Info");

                        Intent intent = new Intent();
                        intent.setAction("intent.start.NewNotification");
                        sendBroadcast(intent);

                    } else if (data != null && data.containsKey("click_action") &&
                            data.get("click_action").equalsIgnoreCase("Cancel")) {

                        CONST.scheduleMeetingFetchJob(MyFirebaseMessagingService.this, 1300, CONST.FETCH_JOB_ID);

                        Db_Helper db_helper = new Db_Helper(getApplicationContext());
                        db_helper.insert_notification(data.get("body"));

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name), data.get("body"), "Info");

                        Intent intent = new Intent();
                        intent.setAction("intent.start.NewNotification");
                        sendBroadcast(intent);

                    } else if (data != null && data.containsKey("click_action") &&
                            data.get("click_action").equalsIgnoreCase("Confirm")) {

                        CONST.scheduleMeetingFetchJob(MyFirebaseMessagingService.this, 1300, CONST.FETCH_JOB_ID);

                        Db_Helper db_helper = new Db_Helper(getApplicationContext());
                        db_helper.insert_notification(data.get("body"));

                        Intent intent = new Intent();
                        intent.setAction("intent.start.NewNotification");
                        sendBroadcast(intent);

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name), data.get("body"), "Info");
                    } else if (data != null && data.containsKey("click_action") &&
                            data.get("click_action").equalsIgnoreCase("End")) {

                        //check if current meeting is the meeting which has end

                        UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);
                        Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate()
                                + " " + meetingDetail.getTotime());

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(meetingDateTime);
                        cal.add(Calendar.MINUTE, CONST.EXTRA_TIME_BEFORE_DETECTION_MINS);

                        if (new Date().compareTo(cal.getTime()) > 0)
                            CONST.scheduleEndMeetingJob(MyFirebaseMessagingService.this, 500, CONST.SCHEDULE_END_MEETING_JOB_ID);

                        //CONST.scheduleMeetingFetchJob(MyFirebaseMessagingService.this, 1300, CONST.FETCH_JOB_ID);

                        Db_Helper db_helper = new Db_Helper(getApplicationContext());
                        db_helper.insert_notification(data.get("body"));

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name), data.get("body"), "Info");

                        Intent intent = new Intent();
                        intent.setAction("intent.start.NewNotification");
                        sendBroadcast(intent);

                    } else if (data != null && data.containsKey("click_action") &&
                            data.get("click_action").equalsIgnoreCase("message")) {

                        Paper.book().write(CONST.NEW_MESSAGE, true);

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name), data.get("body"), "Info");

                        Intent intent = new Intent();
                        intent.setAction("intent.start.NewMessage");
                        sendBroadcast(intent);

                        Db_Helper db_helper = new Db_Helper(getApplicationContext());
                        db_helper.insert_notification(data.get("body"));

                    } else if (data != null && data.containsKey("click_action") &&
                            data.get("click_action").equalsIgnoreCase("meeting_extend")) {

                        Db_Helper db_helper = new Db_Helper(getApplicationContext());
                        db_helper.insert_notification(data.get("title"));

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name), data.get("title"), "Info");

                        getExtendMeetingDetail(data.get("body"));

                        Intent intent = new Intent();
                        intent.setAction("intent.start.NewNotification");
                        sendBroadcast(intent);

                    } else if (data != null && data.containsKey("click_action") &&
                            data.get("click_action").equalsIgnoreCase("meeting_reschedule")) {

                        Db_Helper db_helper = new Db_Helper(getApplicationContext());
                        db_helper.insert_notification(data.get("title"));

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name), data.get("title"), "Info");

                        getRescheduleMeetingDetail(data.get("body"));

                        Intent intent = new Intent();
                        intent.setAction("intent.start.NewNotification");
                        sendBroadcast(intent);

                    } else if (data != null && data.containsKey("click_action") &&
                            data.get("click_action").equalsIgnoreCase("location update")) {

                        Db_Helper db_helper = new Db_Helper(getApplicationContext());
                        db_helper.insert_notification(data.get("body"));

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name), data.get("body"), "Info");

                        Intent intent = new Intent();
                        intent.setAction("intent.start.NewNotification");
                        sendBroadcast(intent);

                    } else if (data != null && data.containsKey("click_action") &&
                            data.get("click_action").equalsIgnoreCase("order_create")) {

                        Db_Helper db_helper = new Db_Helper(getApplicationContext());
                        db_helper.insert_notification(data.get("body"));

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name), data.get("body"),
                                "Info");

                        Intent intent = new Intent();
                        intent.setAction("intent.start.NewNotification");
                        sendBroadcast(intent);
                    } else if (data != null && data.containsKey("click_action") &&
                            data.get("click_action").equalsIgnoreCase("Set Response")) {

                        Db_Helper db_helper = new Db_Helper(getApplicationContext());
                        db_helper.insert_notification(data.get("body"));

                        CONST.showNotifications(getApplicationContext(), getString(R.string.app_name), data.get("body"),
                                "Info");

                        Intent intent = new Intent();
                        intent.setAction("intent.start.NewNotification");
                        sendBroadcast(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //sendNotification(data.get("pushType")+data.get("body"));

        }

    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();

    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(getNotificationIcon())
                        .setColor(ContextCompat.getColor(getBaseContext(), R.color.colorPrimary))
                        .setVibrate(new long[]{1000, 1000, 1000})
                        .setLights(Color.RED, 3000, 3000)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText("" + message)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message));
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, LoginActivity.class);
        //resultIntent.putExtra("NotificationMessage", "This is from notification");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(LoginActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }


    private int getNotificationIcon() {
        boolean useWhiteIcon = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.mipmap.ic_launcher_round : R.mipmap.ic_launcher;
    }

    private boolean setBluetoothEnable(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = false;

        if (bluetoothAdapter != null) {
            isEnabled = bluetoothAdapter.isEnabled();
        } else {
            return false;
        }

        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
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

    public void getExtendMeetingDetail(String meeting_id) {
        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
        UpcomingMeetings upcomingMeetings = Paper.book().read(CONST.CURRENT_MEETING_DATA);

        if (upcomingMeetings != null && upcomingMeetings.getId() == Integer.parseInt(meeting_id)) {

            HashMap<String, String> requestParameter = new HashMap<>();
            requestParameter.put("meeting_id", String.format("%s", meeting_id));

            new PostStringRequest(MyFirebaseMessagingService.this, requestParameter, MyFirebaseMessagingService.this, "meetingExtend",
                    NetworkUtility.BASEURL + NetworkUtility.MEETING_DETAIL_BY_ID);
        }

    }

    public void getRescheduleMeetingDetail(String meeting_id) {
        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
        UpcomingMeetings upcomingMeetings = Paper.book().read(CONST.CURRENT_MEETING_DATA);

        if (upcomingMeetings != null && upcomingMeetings.getId() == Integer.parseInt(meeting_id)) {

            Paper.book().delete(CONST.IS_DETECTION_STARTED);
            Paper.book().delete(CONST.NEEDS_TO_RESCHEDULE);
            Paper.book().delete(CONST.CURRENT_MEETING_DATA);

            Intent intent = new Intent();
            intent.setAction("intent.start.Finish.Navigation");
            sendBroadcast(intent);

            CONST.cancelExtendMeetingJob(MyFirebaseMessagingService.this, CONST.SCHEDULE_EXTEND_MEETING_JOB_ID);
            CONST.cancelEndMeetingJob(MyFirebaseMessagingService.this, CONST.SCHEDULE_END_MEETING_JOB_ID);
            CONST.cancelMeetingETAJob(MyFirebaseMessagingService.this, CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
            CONST.cancelStartMeetingJob(MyFirebaseMessagingService.this, CONST.SCHEDULE_START_MEETING_JOB_ID);
            CONST.cancelLocationDetection(MyFirebaseMessagingService.this, CONST.SCHEDULE_LOCATION_JOB_ID);

            CONST.scheduleMeetingFetchJob(MyFirebaseMessagingService.this, 1300, CONST.FETCH_JOB_ID);

        } else {
            CONST.scheduleMeetingFetchJob(MyFirebaseMessagingService.this, 1300, CONST.FETCH_JOB_ID);
        }

    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        try {
            switch (type) {
                case "meetingExtend":
                    JSONObject meetingDetail = new JSONObject(result);
                    if (meetingDetail.optString("status").equalsIgnoreCase("success")) {
                        JSONArray upcomingAppointments = meetingDetail.getJSONArray("apointments");

                        JSONObject newMeetingDetails = upcomingAppointments.getJSONObject(0);

                        if (newMeetingDetails != null) {

                            UpcomingMeetings upcomingMeetings = Paper.book().read(CONST.CURRENT_MEETING_DATA);
                            upcomingMeetings.setTotime(newMeetingDetails.optString("totime"));

                            CONST.cancelExtendMeetingJob(MyFirebaseMessagingService.this, CONST.SCHEDULE_EXTEND_MEETING_JOB_ID);
                            CONST.cancelEndMeetingJob(MyFirebaseMessagingService.this, CONST.SCHEDULE_END_MEETING_JOB_ID);
                            CONST.cancelLocationDetection(MyFirebaseMessagingService.this, CONST.SCHEDULE_LOCATION_JOB_ID);

                            Paper.book().write(CONST.CURRENT_MEETING_DATA, upcomingMeetings);

                            Date meetingDateEndTime = dateFormat.parse(upcomingMeetings.getFdate() + " " +
                                    upcomingMeetings.getTotime());

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(meetingDateEndTime);
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
                    }
                    break;

                case "meetingReschedule":
                    JSONObject resMeetingDetail = new JSONObject(result);
                    if (resMeetingDetail.optString("status").equalsIgnoreCase("success")) {
                        JSONArray upcomingAppointments = resMeetingDetail.getJSONArray("apointments");

                        JSONObject newMeetingDetails = upcomingAppointments.getJSONObject(0);

                        if (newMeetingDetails != null) {

                            UpcomingMeetings upcomingMeetings = Paper.book().read(CONST.CURRENT_MEETING_DATA);
                            upcomingMeetings.setTotime(newMeetingDetails.optString("totime"));

                            Paper.book().delete(CONST.IS_DETECTION_STARTED);
                            Paper.book().delete(CONST.NEEDS_TO_RESCHEDULE);
                            Paper.book().delete(CONST.CURRENT_MEETING_DATA);

                            Intent intent = new Intent();
                            intent.setAction("intent.start.Finish.Navigation");
                            sendBroadcast(intent);

                            CONST.cancelExtendMeetingJob(MyFirebaseMessagingService.this, CONST.SCHEDULE_EXTEND_MEETING_JOB_ID);
                            CONST.cancelEndMeetingJob(MyFirebaseMessagingService.this, CONST.SCHEDULE_END_MEETING_JOB_ID);
                            CONST.cancelMeetingETAJob(MyFirebaseMessagingService.this, CONST.SCHEDULE_ETA_DETECTION_JOB_ID);
                            CONST.cancelStartMeetingJob(MyFirebaseMessagingService.this, CONST.SCHEDULE_START_MEETING_JOB_ID);
                            CONST.cancelLocationDetection(MyFirebaseMessagingService.this, CONST.SCHEDULE_LOCATION_JOB_ID);

                            CONST.scheduleMeetingFetchJob(MyFirebaseMessagingService.this, 1300, CONST.FETCH_JOB_ID);

                        }
                    }

                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {

    }

    @Override
    public void onStarted() {

    }

}
