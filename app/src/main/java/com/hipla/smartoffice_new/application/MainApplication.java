package com.hipla.smartoffice_new.application;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.hipla.smartoffice_new.utils.CONST;
import com.hipla.smartoffice_new.utils.PahoMqttClient;
import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.eclipse.paho.android.service.MqttAndroidClient;

import io.fabric.sdk.android.Fabric;
import java.util.Locale;

import io.paperdb.Paper;

/**
 * Created by FNSPL on 2/2/2018.
 */

public class MainApplication extends Application{

    public static final String TAG           = "Navigine.Demo";
    public static final String SERVER_URL    = "https://api.navigine.com";
    //For TCS
    //public static final String USER_HASH     = "142D-689D-AC24-84F9";
    //For FNSPL
    static final String USER_HASH     = "0F17-DAE1-4D0A-1778";

    //For TCS
    //public static final int LOCATION_ID   = 2766;
    //For FNSPL
    public static final int LOCATION_ID   = 1894;

    public static NavigationThread Navigation    = null;
    public static boolean isNavigineInitialized = false;
    public static String NavigineInitialized = "navigineInstalled";
    public static final boolean WRITE_LOGS    = true;
    // Screen parameters
    public static float DisplayWidthPx            = 0.0f;
    public static float DisplayHeightPx           = 0.0f;
    public static float DisplayWidthDp            = 0.0f;
    public static float DisplayHeightDp           = 0.0f;
    public static float DisplayDensity            = 0.0f;
    private static PahoMqttClient pahoMqttClient;
    private static MqttAndroidClient client;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Paper.init(getApplicationContext());

        // Create global configuration and initialize ImageLoader with this config
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(config);

    }

    public static void createMqttConnection(Context mContext) {
        pahoMqttClient = new PahoMqttClient();
        client = pahoMqttClient.getMqttClient(mContext, CONST.MQTT_BROKER_URL,
                CONST.CLIENT_ID + getDeviceID(mContext));
    }

    public static PahoMqttClient getPahoMqttClient(Context mContext){
        if(pahoMqttClient!=null) {
            return pahoMqttClient;
        }else{
            return null;
        }
    }

    public static MqttAndroidClient getMqttClient(Context mContext){
        if(client!=null) {
            return client;
        }else{
            return null;
        }
    }

    public static boolean initialize(Context mContext)
    {
        NavigineSDK.setParameter(mContext, "actions_updates_enabled",  false);
        NavigineSDK.setParameter(mContext, "location_updates_enabled", true);
        NavigineSDK.setParameter(mContext, "location_loader_timeout",  60);
        NavigineSDK.setParameter(mContext, "location_update_timeout",  300);
        NavigineSDK.setParameter(mContext, "location_retry_timeout",   300);
        NavigineSDK.setParameter(mContext, "post_beacons_enabled",     true);
        NavigineSDK.setParameter(mContext, "post_messages_enabled",    true);

        if (!(NavigineSDK.initialize(mContext, MainApplication.USER_HASH, MainApplication.SERVER_URL))) {
            return false;
        }else{
            isNavigineInitialized = true;
        }

        Navigation = NavigineSDK.getNavigation();
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        DisplayWidthPx  = displayMetrics.widthPixels;
        DisplayHeightPx = displayMetrics.heightPixels;
        DisplayDensity  = displayMetrics.density;
        DisplayWidthDp  = DisplayWidthPx  / DisplayDensity;
        DisplayHeightDp = DisplayHeightPx / DisplayDensity;

        Log.d(TAG, String.format(Locale.ENGLISH, "Display size: %.1fpx x %.1fpx (%.1fdp x %.1fdp, density=%.2f)",
                DisplayWidthPx, DisplayHeightPx,
                DisplayWidthDp, DisplayHeightDp,
                DisplayDensity));

        return true;
    }

    public static void finish()
    {
        isNavigineInitialized=false;
        if (Navigation == null)
            return;

        if (Navigation != null)
        {
            NavigineSDK.finish();
            Navigation = null;
        }
    }

    public static String getDeviceID(Context mContext){
        String deviceId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        return deviceId;
    }

}
