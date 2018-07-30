package com.hipla.smartoffice_new.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.application.MainApplication;
import com.hipla.smartoffice_new.database.Db_Helper;
import com.hipla.smartoffice_new.model.ZoneInfo;
import com.hipla.smartoffice_new.utils.CONST;
import com.hipla.smartoffice_new.utils.PahoMqttClient;
import com.navigine.naviginesdk.DeviceInfo;
import com.navigine.naviginesdk.Location;
import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

public class MyNavigationService extends Service {

    public static final String POINTX = "pointX";
    public static final String POINTY = "pointY";
    // NavigationThread instance
    public static NavigationThread mNavigation = null;
    public static final String DEVICE_LOCATION = "deviceLocation";
    private Intent locatonFetch;
    private Intent errorMessage;
    // Location parameters
    private Location mLocation = null;
    private int mCurrentSubLocationIndex  = -1;
    public static final String ERROR = "error";
    private Db_Helper db_helper;
    private PahoMqttClient pahoMqttClient;
    private MqttAndroidClient client;
    private boolean isConferanceRoomOpen = false, isRestrictZoneShown = false;
    private ArrayList<ZoneInfo> zoneInfos = new ArrayList<>();
    private ArrayList<PointF[]> zoneInfoPoint = new ArrayList<>();

    public MyNavigationService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForeground(1002, new Notification());

        setBluetoothEnable(true, getApplicationContext());

        mNavigation = MainApplication.Navigation;

        if (mNavigation != null)
        {
            mLocation = mNavigation.getLocation();

            mNavigation.setDeviceListener(
                            new DeviceInfo.Listener() {
                                @Override public void onUpdate(DeviceInfo info) {
                                    handleDeviceUpdate(info);
                                }
                            }
                    );

            db_helper = new Db_Helper(getApplicationContext());
            if (db_helper != null) {

                zoneInfos = db_helper.getAllZoneInfo();

                for (ZoneInfo zoneInfo :
                        zoneInfos) {
                    zoneInfoPoint.add(convertToPoints(zoneInfo));
                }
            }

        }else{
            stopSelf();
        }

    }

    private void handleDeviceUpdate(DeviceInfo mDeviceInfo) {

        Paper.book().delete(DEVICE_LOCATION);

        if (mDeviceInfo == null)
            return;

        // Check if location is loaded
        if (mLocation == null )
            return;

        if (mDeviceInfo.isValid())
        {
            Paper.book().write(DEVICE_LOCATION, mDeviceInfo);

            locatonFetch = new Intent("android.intent.action.MAIN");
            sendBroadcast(locatonFetch);

            calculateZone(mDeviceInfo);
        }
        else
        {
            switch (mDeviceInfo.errorCode)
            {
                case 4:
                    errorMessage = new Intent("android.intent.action.SUCCESSLOCATION");
                    errorMessage.putExtra(ERROR, getString(R.string.erroe_message_1));
                    sendBroadcast(errorMessage);

                    break;

                case 8:
                case 30:
                    errorMessage = new Intent("android.intent.action.SUCCESSLOCATION");
                    errorMessage.putExtra(ERROR, getString(R.string.erroe_message_2));
                    sendBroadcast(errorMessage);

                    break;

                default:
                    errorMessage = new Intent("android.intent.action.SUCCESSLOCATION");
                    errorMessage.putExtra(ERROR, getString(R.string.erroe_message_3));
                    sendBroadcast(errorMessage);

                    break;
            }
        }

    }

    @Override public void onDestroy() {
        /*if (mNavigation != null)
        {
            NavigineSDK.finish();
            mNavigation = null;
        }*/

        super.onDestroy();
    }

    private boolean setBluetoothEnable(boolean enable, Context mContext) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean isEnabled = false;

        if(bluetoothAdapter != null) {
            isEnabled = bluetoothAdapter.isEnabled();
        }else{
            return false ;
        }

        if (enable && !isEnabled) {
            return bluetoothAdapter.enable();
        } else if (!enable && isEnabled) {
            return bluetoothAdapter.disable();
        }
        // No need to change bluetooth state
        return true;
    }

    private void calculateZone(DeviceInfo mDeviceInfo) {

        for (int index = 0; index < zoneInfoPoint.size(); index++) {
            boolean inZone = contains(zoneInfoPoint.get(index), new PointF(mDeviceInfo.x, mDeviceInfo.y));

            if (inZone && zoneInfos.get(index).getId() == 2 && !isConferanceRoomOpen &&
                    MainApplication.getPahoMqttClient(MyNavigationService.this) != null &&
                    MainApplication.getPahoMqttClient(MyNavigationService.this).isIsConnected()) {

                isConferanceRoomOpen = true;
                isRestrictZoneShown = false;
                doorOpen();

                Intent intentFilter = new Intent(
                        "android.intent.action.NOT_IN_ROOM");
                sendBroadcast(intentFilter);

                break;
            } else if (inZone && zoneInfos.get(index).getId() == 3 && !isRestrictZoneShown) {

                //is in restricted zone
                isConferanceRoomOpen = false;
                isRestrictZoneShown = true;

                /*CONST.showNotificationForLocation(getApplicationContext(), getString(R.string.app_name),
                        getString(R.string.restricted_zone), 1010);

                Intent intentFilter = new Intent(
                        "android.intent.action.NOT_IN_ROOM");
                sendBroadcast(intentFilter);*/

                break;
            } else if (inZone && zoneInfos.get(index).getId() == 1) {

                //Is in inside conference room
                isRestrictZoneShown = false;
                isConferanceRoomOpen = true;

                Intent intentFilter = new Intent(
                        "android.intent.action.IN_ROOM");
                sendBroadcast(intentFilter);

                break;
            } else if (inZone && zoneInfos.get(index).getId() == 4) {

                //Is in inside conference room
                isConferanceRoomOpen = false;
                isRestrictZoneShown = false;

                break;
            } else {
                //isConferanceRoomOpen = false;
                //isRestrictZoneShown = false;

                /*Intent intentFilter = new Intent(
                        "android.intent.action.NOT_IN_ROOM");
                sendBroadcast(intentFilter);*/
            }
        }
    }

    public boolean contains(PointF[] points, PointF test) {
        int i;
        int j;
        boolean result = false;

        if (((points[0].x < test.x) && (points[0].y > test.y))) {
            if (((points[3].x < test.x) && (points[3].y < test.y))) {
                if (((points[1].x > test.x) && (points[1].y > test.y))) {
                    if (((points[2].x > test.x) && (points[2].y < test.y))) {
                        result = true;
                    }
                }
            }
        }

        return result;
    }

    private PointF[] convertToPoints(ZoneInfo zoneInfo) {
        PointF[] pointFs = new PointF[4];

        String[] pointsA = zoneInfo.getPointA().split(",");
        PointF pointA = new PointF(Float.parseFloat(pointsA[0]), Float.parseFloat(pointsA[1]));
        pointFs[0] = pointA;

        String[] pointsB = zoneInfo.getPointB().split(",");
        PointF pointB = new PointF(Float.parseFloat(pointsB[0]), Float.parseFloat(pointsB[1]));
        pointFs[1] = pointB;

        String[] pointsC = zoneInfo.getPointC().split(",");
        PointF pointC = new PointF(Float.parseFloat(pointsC[0]), Float.parseFloat(pointsC[1]));
        pointFs[2] = pointC;

        String[] pointsD = zoneInfo.getPointD().split(",");
        PointF pointD = new PointF(Float.parseFloat(pointsD[0]), Float.parseFloat(pointsD[1]));
        pointFs[3] = pointD;

        return pointFs;
    }

    private void doorOpen() {
        try {

            if (MainApplication.getMqttClient(MyNavigationService.this) != null
                    && MainApplication.getPahoMqttClient(MyNavigationService.this) != null &&
                    MainApplication.getPahoMqttClient(MyNavigationService.this).isIsConnected() && isConferanceRoomOpen) {

                isConferanceRoomOpen = true;

                MainApplication.getPahoMqttClient(MyNavigationService.this).publishMessage(MainApplication.getMqttClient(MyNavigationService.this),
                        "on", 1, CONST.PUBLISH_TOPIC_DOOR_OPEN1);

                CONST.showNotificationForLocation(getApplicationContext(), getString(R.string.app_name),
                        getString(R.string.door_will_open), 1010);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        doorClose();
                    }
                }, 5000);

                openLight();

            } else {
                isConferanceRoomOpen = false;

                //MainApplication.getPahoMqttClient(MyNavigationService.this).reconnect();
            }

        } catch (Exception e) {
            e.printStackTrace();

            isConferanceRoomOpen = false;
        }
    }

    private void openLight() throws MqttException, UnsupportedEncodingException {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    MainApplication.getPahoMqttClient(MyNavigationService.this).
                            publishMessage(MainApplication.getMqttClient(MyNavigationService.this), "on", 1, CONST.PUBLISH_TOPIC_LIGHT_OPEN1);
                }catch (Exception ex){

                }
            }
        },500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    MainApplication.getPahoMqttClient(MyNavigationService.this).
                            publishMessage(MainApplication.getMqttClient(MyNavigationService.this), "on", 1, CONST.PUBLISH_TOPIC_LIGHT_OPEN2);

                }catch (Exception ex){

                }
            }
        },1000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    MainApplication.getPahoMqttClient(MyNavigationService.this).
                            publishMessage(MainApplication.getMqttClient(MyNavigationService.this), "on", 1, CONST.PUBLISH_TOPIC_LIGHT_OPEN3);

                }catch (Exception ex){

                }
            }
        },1500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    MainApplication.getPahoMqttClient(MyNavigationService.this).
                            publishMessage(MainApplication.getMqttClient(MyNavigationService.this), "on", 1, CONST.PUBLISH_TOPIC_LIGHT_OPEN4);
                }catch (Exception ex){

                }
            }
        },2000);

    }

    private void doorClose() {
        try {

            if (MainApplication.getPahoMqttClient(MyNavigationService.this) != null) {

                MainApplication.getPahoMqttClient(MyNavigationService.this).publishMessage(MainApplication.
                        getMqttClient(MyNavigationService.this), "off", 1, CONST.PUBLISH_TOPIC_DOOR_OPEN1);
            }

        } catch (MqttException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

}
