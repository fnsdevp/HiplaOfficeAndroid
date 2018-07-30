package com.hipla.smartoffice_new.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.hipla.smartoffice_new.application.MainApplication;
import com.hipla.smartoffice_new.database.Db_Helper;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.databinding.ActivitySplashBinding;
import com.hipla.smartoffice_new.model.ZoneInfo;
import com.hipla.smartoffice_new.utils.MarshmallowPermissionHelper;
import com.navigine.naviginesdk.Location;
import com.navigine.naviginesdk.NavigineSDK;

import io.paperdb.Paper;

public class SplashActivity extends AppCompatActivity {

    private Handler mHandler = new Handler();
    private ActivitySplashBinding binding;
    private static final int ALL_PERMISSION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        setUpZoneData();
        initView();
    }

    private void initView() {
        binding.content.startRippleAnimation();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {

                checkNavinginePermissions();

            }
        }, 3000L);

    }

    private void checkNavinginePermissions() {
        if (Build.VERSION.SDK_INT > 22) {
            if (MarshmallowPermissionHelper.getAllNaviginePermission(null
                    , SplashActivity.this, ALL_PERMISSION)) {
                if (!MainApplication.isNavigineInitialized) {
                    (new InitTask(SplashActivity.this)).execute();
                } else {
                    if(Paper.book().read(NetworkUtility.USER_INFO )!=null) {
                        startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
                        overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                        supportFinishAfterTransition();
                    }else{
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                        supportFinishAfterTransition();
                    }
                }
            }
        } else {
            if (!MainApplication.isNavigineInitialized) {
                (new InitTask(SplashActivity.this)).execute();
            } else {
                if(Paper.book().read(NetworkUtility.USER_INFO )!=null) {
                    startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
                    overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                    supportFinishAfterTransition();
                }else{
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                    supportFinishAfterTransition();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        binding.content.stopRippleAnimation();
    }

    class InitTask extends AsyncTask<Void, Void, Boolean> {
        private Context mContext = null;
        private String mErrorMsg = null;

        public InitTask(Context context) {
            mContext = context.getApplicationContext();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (!MainApplication.initialize(getApplicationContext())) {
                mErrorMsg = "Error in downloading navigation! Please, try again later or contact technical support";
                return Boolean.FALSE;
            }

            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (result.booleanValue()) {
                // Starting main activity
                if (MainApplication.isNavigineInitialized) {
                    NavigineSDK.loadLocationInBackground(MainApplication.LOCATION_ID, 30,
                            new Location.LoadListener() {
                                @Override
                                public void onFinished() {
                                    MainApplication.isNavigineInitialized = true;
                                    Paper.book().write(MainApplication.NavigineInitialized, true);
                                    if(Paper.book().read(NetworkUtility.USER_INFO )!=null) {
                                        startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
                                        overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                                        supportFinishAfterTransition();
                                    }else{
                                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                        overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                                        supportFinishAfterTransition();
                                    }
                                }

                                @Override
                                public void onFailed(int error) {
                                    MainApplication.isNavigineInitialized = false;
                                    Paper.book().write(MainApplication.NavigineInitialized, false);
                                    if(Paper.book().read(NetworkUtility.USER_INFO )!=null) {
                                        startActivity(new Intent(SplashActivity.this, DashboardActivity.class));
                                        overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                                        supportFinishAfterTransition();
                                    }else{
                                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                                        overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                                        supportFinishAfterTransition();
                                    }
                                }

                                @Override
                                public void onUpdate(int progress) {

                                }
                            });
                }
            } else {
                Paper.book().delete(MainApplication.NavigineInitialized);
            }


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
    }

    private void setUpZoneData() {
        Db_Helper db_helper = new Db_Helper(getApplicationContext());

        //For FNSPL Zone
        db_helper.insert_zone(new ZoneInfo(1,"Conference Room Inside", "23.36,19.41","20.89,23.16","25.90,22.85","25.58,13.09","19.10,15.88"));
        db_helper.insert_zone(new ZoneInfo(2,"Conference Room Outside", "16.01,22.85","16.78,26.26","20.58,26.38","18.76,20.07","15.06,20.45"));
        db_helper.insert_zone(new ZoneInfo(3,"Restricted Zone", "19.02,28.62","10.79,30.99","19.35,31.22","19.67,24.30","10.89,24.26"));
        db_helper.insert_zone(new ZoneInfo(4,"Restricted Zone1", "832,1131","8.54,20.00","18.43,20.00","18.66,15.29","8.53,15.16"));
        db_helper.insert_zone(new ZoneInfo(5,"Small Conference Room","15.49,40.14","20.79,23.08","25.84,22.56","25.90,13.13","19.12,15.92"));
        db_helper.insert_zone(new ZoneInfo(6,"Entry Room","15.49,40.14","1.43,50.23","14.02,50.14","14.69,30.51","1.13,30.4"));

        db_helper.insert_ise_wifi(1, "guest1", "fnspl123");
        db_helper.insert_ise_wifi(2, "guest1", "fnspl123");
        db_helper.insert_ise_wifi(3, "guest1", "fnspl123");
        db_helper.insert_ise_wifi(4, "guest1", "fnspl123");
        db_helper.insert_ise_wifi(5, "guest1", "fnspl123");
        db_helper.insert_ise_wifi(6, "guest1", "fnspl123");
        db_helper.insert_ise_wifi(7, "guest1", "fnspl123");
        db_helper.insert_ise_wifi(8, "guest1", "fnspl123");
        db_helper.insert_ise_wifi(9, "guest1", "fnspl123");
        db_helper.insert_ise_wifi(10, "guest1", "fnspl123");

    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        switch (requestCode) {
            case ALL_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[3] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[4] == PackageManager.PERMISSION_GRANTED) {

                    if (!MainApplication.isNavigineInitialized) {
                        (new InitTask(this)).execute();
                    }

                }
                return;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

}
