package com.hipla.smartoffice_new.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.hipla.smartoffice_new.services.FetchMeetingInfoservice;
import com.hipla.smartoffice_new.services.LocationDetection;
import com.hipla.smartoffice_new.services.ScheduleETADetectionService;
import com.hipla.smartoffice_new.services.ScheduleMeetingExtendService;
import com.hipla.smartoffice_new.services.ScheduleMeetingFinishService;
import com.hipla.smartoffice_new.services.ScheduleMeetingProcessService;
import com.hipla.smartoffice_new.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.process.BitmapProcessor;

/**
 * Created by FNSPL on 2/9/2018.
 */

public class CONST {

    public static final String MQTT_BROKER_URL = "tcp://192.168.1.30:8883";
    public static final String PUBLISH_TOPIC_DOOR_OPEN1 = "/cmnd/dunit1/POWER1";
    public static final String PUBLISH_TOPIC_DOOR_OPEN2 = "/cmnd/dunit1/POWER1";
    public static final String PUBLISH_TOPIC_DOOR_OPEN3 = "/cmnd/dunit1/POWER1";
    public static final String PUBLISH_TOPIC_DOOR_OPEN4 = "/cmnd/dunit1/POWER1";
    public static final String PUBLISH_TOPIC_LIGHT_OPEN1 = "/cmnd/lunit1/POWER1";
    public static final String PUBLISH_TOPIC_LIGHT_OPEN2 = "/cmnd/lunit1/POWER2";
    public static final String PUBLISH_TOPIC_LIGHT_OPEN3 = "/cmnd/lunit1/POWER3";
    public static final String PUBLISH_TOPIC_LIGHT_OPEN4 = "/cmnd/lunit1/POWER4";
    public static final String STATUS_TOPIC_DOOR_OPEN3 = "/stat/dunit2/POWER3";
    public static final String CLIENT_ID = "HC-AND-";
    public static final String ANDROID_TOPIC = "android" + CLIENT_ID;

    public static final String HOME_FRAGMENT = "homeFragment";
    public static final String SCHEDULE_MEETING = "scheduleMeeting";
    public static final String FIXED_MEETING = "fixedMeeting";
    public static final String CONTACT_DIALOG = "contactDialog";
    public static final String FLEXIBLE_MEETING = "flexibleMeeting";
    public static final String MANAGE_MEETING = "manageMeetings";
    public static final String APPOINTMENT_DATA = "appointmentData";
    public static final String MEETING_DETAILS = "meetingDetails";
    public static final String REQUESTED_MEETING_DETAILS = "requestedMeetingDetails";
    public static final String REQUESTED_GROUP_MEETING_DETAILS = "requestedGroupMeetingDetails";
    public static final String INDBOX_MESSGAES = "indboxMessages";
    public static final String PROFILE_MANAGMENT = "profileManagement";
    public static final String ORDER_FOOD = "orderfood";
    public static final String DISTANCE_NOTIFICATION = "distanceNotification";
    public static final String ABOUT_US = "aboutUs";
    public static final String SET_AVAIBILITY = "setAvaibility";
    public static final String CURRENT_MEETING_DATA = "currentMeetingData";
    public static final int TIME_BEFORE_DETECTION = -2;
    public static final int TIME_BEFORE_DETECTION_HRS = 2;
    public static final int EXTRA_TIME_BEFORE_DETECTION_MINS = 10;
    public static final int TIME_BEFORE_EXTEND_MEETING_IN_MIN = -10;
    public static final int TIME_BEFORE_CANCEL_MEETING_IN_MIN = -30;
    public static final int TIME_AFTER_MEETING_IN_SEC = 600;
    public static final String IS_DETECTION_STARTED = "isDetectionStarted";
    public static final String NEEDS_TO_RESCHEDULE = "needsToReschedule";
    public static final String SET_NOTIFICATION = "notificationFragment";
    public static final String NEW_MESSAGE = "newMessage";
    public static final String ZONE_ID = "zoneId";
    public static final String POINTY = "pointY";
    public static final String POINTX = "pointX";
    public static final String GEO_FENCING_STARTED = "geoFencingStart";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String IS_HALF_HOUR_SLOT = "isHalfHrSlot";
    public static final String RUNNING_LATE = "runningLate";
    public static final String FOOD_ITEMS_IN_CART = "foodItemInCart";
    public static final String FOOD_CART = "foodCartFragment";
    public static final String HOT_DESK_DATA = "hotDeskData";
    public static final String START_TIME = "09:00 AM";
    public static final String END_TIME = "09:00 PM";
    public static final Object ISE_USERNAME = "admin";
    public static final Object ISE_PASSWORD = "P@$$w0r9";

    //TCS Banyan Park
    /*public static double DESTINATION_LAT = 19.1195747;
    public static double DESTINATION_LONG = 72.8573857;*/

    //home
    /*public static double DESTINATION_LAT = 22.6413732;
    public static double DESTINATION_LONG = 88.4250184;*/

    //FNSP
    public static double DESTINATION_LAT = 22.573075;
    public static double DESTINATION_LONG = 88.452616;

    public static String DISTANCE_API_KEY = "AIzaSyDl-e82qPIBdn8JUks3jyOreYREtvavutw";

    //units are M(Miles), K(Kilometers), N(Nautical Miles)
    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == "K") {
            dist = dist * 1.609344;
        } else if (unit == "N") {
            dist = dist * 0.8684;
        }

        return (dist);
    }

    public static double distance(double startLat, double startLong,
                                  double endLat, double endLong) {

        final int EARTH_RADIUS = 6371;

        double dLat = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // <-- d
    }

    //::	This function converts decimal degrees to radians
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    //::	This function converts radians to decimal degrees
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    public static DisplayImageOptions ErrorWithLoaderNormalCorner = new DisplayImageOptions.Builder()
            .resetViewBeforeLoading(true)
            .showImageOnFail(R.drawable.ic_qr_code)
            .showImageOnLoading(R.drawable.ic_qr_code)
            .cacheInMemory(false)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .build();

    public static DisplayImageOptions ErrorWithLoaderRoundedCorner = new DisplayImageOptions.Builder()
            .showImageOnFail(R.drawable.ic_profile_placeholder)
            .showImageOnLoading(R.drawable.ic_profile_placeholder)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .considerExifParams(true)
            .imageScaleType(ImageScaleType.EXACTLY)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .displayer(new RoundedBitmapDisplayer(1000))
            .postProcessor(new BitmapProcessor() {
                @Override
                public Bitmap process(Bitmap bmp) {
                    int dimension = getSquareCropDimensionForBitmap(bmp);
                    bmp = ThumbnailUtils.extractThumbnail(bmp, dimension, dimension);
                    return bmp;
                }
            })
            .build();

    public static int getSquareCropDimensionForBitmap(Bitmap bitmap) {
        //use the smallest dimension of the image to crop to
        return Math.min(bitmap.getWidth(), bitmap.getHeight());
    }


    public static int FETCH_JOB_ID = 100;
    public static int NEXT_DAY_FETCH_JOB_ID = 106;
    public static int SCHEDULE_ETA_DETECTION_JOB_ID = 101;
    public static int SCHEDULE_START_MEETING_JOB_ID = 102;
    public static int SCHEDULE_EXTEND_MEETING_JOB_ID = 103;
    public static int SCHEDULE_END_MEETING_JOB_ID = 104;
    public static int SCHEDULE_LOCATION_JOB_ID = 105;

    public static void scheduleMeetingFetchJob(Context context, long timer, int jobId) {

        ComponentName serviceComponent = new ComponentName(context, FetchMeetingInfoservice.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            builder.setMinimumLatency(timer); // wait at least
            builder.setOverrideDeadline(timer + 1500); // maximum delay
        } else {
            builder.setMinimumLatency(timer); // wait at least
            builder.setOverrideDeadline(timer + 1500); // maximum delay
        }

        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
        jobScheduler.schedule(builder.build());
    }

    public static void scheduleNextDayMeetingFetchJob(Context context, long timer, int jobId) {

        ComponentName serviceComponent = new ComponentName(context, FetchMeetingInfoservice.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            builder.setMinimumLatency(timer); // wait at least
            builder.setOverrideDeadline(timer + 1500); // maximum delay
        } else {
            builder.setMinimumLatency(timer); // wait at least
            builder.setOverrideDeadline(timer + 1500); // maximum delay
        }
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
        jobScheduler.schedule(builder.build());
    }

    public static void scheduleMeetingETAJob(Context context, long timer, int jobId) {

        ComponentName serviceComponent = new ComponentName(context, ScheduleETADetectionService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            builder.setMinimumLatency(timer); // wait at least
            builder.setOverrideDeadline(timer + 1500); // maximum delay
        } else {
            builder.setMinimumLatency(timer); // wait at least
            builder.setOverrideDeadline(timer + 1500); // maximum delay
        }
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not

        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
        jobScheduler.schedule(builder.build());
    }

    public static void scheduleStartMeetingJob(Context context, long timer, int jobId) {

        ComponentName serviceComponent = new ComponentName(context, ScheduleMeetingProcessService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            builder.setMinimumLatency(timer); // wait at least
            builder.setOverrideDeadline(timer + 1500); // maximum delay
        } else {
            builder.setMinimumLatency(timer); // wait at least
            builder.setOverrideDeadline(timer + 1500); // maximum delay
        }
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
        jobScheduler.schedule(builder.build());
    }


    public static void scheduleExtendMeetingJob(Context context, long timer, int jobId) {

        ComponentName serviceComponent = new ComponentName(context, ScheduleMeetingExtendService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            builder.setMinimumLatency(timer); // wait at least
            builder.setOverrideDeadline(timer + 1500); // maximum delay
        } else {
            builder.setMinimumLatency(timer); // wait at least
            builder.setOverrideDeadline(timer + 1500); // maximum delay
        }
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
        jobScheduler.schedule(builder.build());
    }

    public static void scheduleEndMeetingJob(Context context, long timer, int jobId) {

        ComponentName serviceComponent = new ComponentName(context, ScheduleMeetingFinishService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            builder.setMinimumLatency(timer); // wait at least
            builder.setOverrideDeadline(timer + 1500); // maximum delay
        } else {
            builder.setMinimumLatency(timer); // wait at least
            builder.setOverrideDeadline(timer + 1500); // maximum delay
        }
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
        jobScheduler.schedule(builder.build());
    }

    public static void cancelEndMeetingJob(Context context, int jobId) {
        ComponentName serviceComponent = new ComponentName(context, ScheduleMeetingFinishService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        //builder.setOverrideDeadline(timer); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
    }

    public static void cancelExtendMeetingJob(Context context, int jobId) {
        ComponentName serviceComponent = new ComponentName(context, ScheduleMeetingExtendService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        //builder.setOverrideDeadline(timer); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
    }

    public static void cancelMeetingETAJob(Context context, int jobId) {
        ComponentName serviceComponent = new ComponentName(context, ScheduleETADetectionService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        //builder.setOverrideDeadline(timer); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
    }

    public static void cancelStartMeetingJob(Context context, int jobId) {
        ComponentName serviceComponent = new ComponentName(context, ScheduleMeetingProcessService.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        //builder.setOverrideDeadline(timer); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
    }

    public static void scheduleLocationDetectionJob(Context context, long timer, int jobId) {

        ComponentName serviceComponent = new ComponentName(context, LocationDetection.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            builder.setMinimumLatency(timer); // wait at least
            builder.setOverrideDeadline(timer + 1500); // maximum delay
        } else {
            builder.setMinimumLatency(timer); // wait at least
            builder.setOverrideDeadline(timer + 1500); // maximum delay
        }

        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
        jobScheduler.schedule(builder.build());
    }

    public static void cancelLocationDetection(Context context, int jobId) {
        ComponentName serviceComponent = new ComponentName(context, LocationDetection.class);
        JobInfo.Builder builder = new JobInfo.Builder(jobId, serviceComponent);
        //builder.setOverrideDeadline(timer); // maximum delay
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
        //builder.setRequiresDeviceIdle(true); // device should be idle
        //builder.setRequiresCharging(false); // we don't care if the device is charging or not
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(jobId);
    }

    public static boolean checkVersion() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            return true;
        } else {
            return false;
        }
    }

    static String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";
    static String NOTIFICATION_CHANNEL_ID1 = "my_channel_id_02";
    static String NOTIFICATION_CHANNEL_ID2 = "my_channel_id_03";

    public static void showNotifications(Context context, String title, String content, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications1", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Testing");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("" + title)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content));

        notificationManager.notify(/*notification id*/notificationId, notificationBuilder.build());
    }

    public static void showNotificationForLocation(Context context, String title, String content, int notificationId) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID1, "My Notifications1", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Testing");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("" + title)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content));

        notificationManager.notify(/*notification id*/notificationId, notificationBuilder.build());
    }


    public static void showNotifications(Context context, String title, String content, String info) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID2, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("" + title)
                .setPriority(Notification.PRIORITY_HIGH)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setContentInfo(info);

        notificationManager.notify(/*notification id*/1, notificationBuilder.build());
    }

}
