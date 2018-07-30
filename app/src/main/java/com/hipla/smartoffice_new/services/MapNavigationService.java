package com.hipla.smartoffice_new.services;

import android.app.Notification;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.application.MainApplication;
import com.navigine.naviginesdk.DeviceInfo;
import com.navigine.naviginesdk.Location;
import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;

import io.paperdb.Paper;

public class MapNavigationService extends Service {

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

    public MapNavigationService() {

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
        }
*/
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

}
