package com.hipla.smartoffice_new.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.hipla.smartoffice_new.R;

/**
 * Checking Internet Connection.
 **/

public class InternetConnectionDetector {

    private Context context;
    private static InternetConnectionDetector internetConnectionDetector;
    private static Context prevContext;

    private InternetConnectionDetector(Context context) {
        this.context = context;
    }

    public static InternetConnectionDetector getInstant(Context context) {
        if (internetConnectionDetector == null) {
            prevContext = context;
            internetConnectionDetector = new InternetConnectionDetector(context);
        }

        if (prevContext != context){
            internetConnectionDetector = null;
            internetConnectionDetector = new InternetConnectionDetector(context);
        }
        prevContext = context;
        return internetConnectionDetector;
    }

    public boolean isConnectingToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null)
                return true;
        }

        //ErrorMessageDialog.getInstant(prevContext).show(prevContext.getResources().getString(R.string.check_your_connection));
        return false;
    }
}
