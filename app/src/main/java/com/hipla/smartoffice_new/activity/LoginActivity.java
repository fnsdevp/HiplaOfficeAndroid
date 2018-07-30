package com.hipla.smartoffice_new.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hipla.smartoffice_new.application.MainApplication;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.databinding.ActivityLoginBinding;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.utils.ErrorMessageDialog;
import com.hipla.smartoffice_new.utils.MarshmallowPermissionHelper;
import com.navigine.naviginesdk.Location;
import com.navigine.naviginesdk.NavigineSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.paperdb.Paper;

public class LoginActivity extends BaseActivity implements StringRequestListener {

    private static final int ALL_PERMISSION = 1000;
    private ActivityLoginBinding binding;
    private String userType = "";
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        binding.setActivity(LoginActivity.this);

        initView();
    }

    private void initView() {

        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/HelveticaNeue Light.ttf");

        checkNavinginePermissions();
    }

    private void checkNavinginePermissions() {
        if (Build.VERSION.SDK_INT > 22) {
            if (MarshmallowPermissionHelper.getAllNaviginePermission(null
                    , LoginActivity.this, ALL_PERMISSION)) {
                if (!MainApplication.isNavigineInitialized) {
                    (new InitTask(this)).execute();
                } else {

                }
            }
        } else {
            if (!MainApplication.isNavigineInitialized) {
                (new InitTask(this)).execute();
            } else {

            }
        }
    }

    public void doLogin() {

        if (!binding.inputEmail.getText().toString().isEmpty()) {

            binding.inputLayoutEmail.setErrorEnabled(false);

            if (!binding.inputPassword.getText().toString().isEmpty()) {
                //binding.inputLayoutEmail.setErrorEnabled(true);
                requestPostData();
            } else {
                ErrorMessageDialog.getInstant(LoginActivity.this).show(getResources().getString(R.string.check_your_password));
                binding.inputPassword.requestFocus(binding.inputEmail.getText().toString().length());
               /* binding.inputLayoutEmail.setErrorEnabled(true);
                binding.inputLayoutPassword.setError(getResources().getString(R.string.check_your_password));*/
            }

        } else {
            ErrorMessageDialog.getInstant(LoginActivity.this).show(getResources().getString(R.string.check_your_email));
            binding.inputEmail.requestFocus(binding.inputEmail.getText().toString().length());
            /*binding.inputLayoutEmail.setErrorEnabled(true);
            binding.inputLayoutEmail.setError(getResources().getString(R.string.check_your_email));*/
        }

    }

    public void signUp() {
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
    }

    public void forgetPassword() {
        startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
        overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
    }

    private void requestPostData() {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        if (pDialog!=null && !pDialog.isShowing())
            pDialog.show();

        if (binding.checkbox.isChecked()) {
            userType = "Employee";
        } else {
            userType = "Guest";
        }

        HashMap<String, String> requestParameter = new HashMap<>();
        requestParameter.put("username", "" + binding.inputEmail.getText().toString());
        requestParameter.put("password", "" + binding.inputPassword.getText().toString());
        requestParameter.put("deviceId", Paper.book().read(NetworkUtility.TOKEN, ""));
        requestParameter.put("type", "Android");
        requestParameter.put("usertype", "" + userType);

        new PostStringRequest(LoginActivity.this, requestParameter, LoginActivity.this, "Login",
                NetworkUtility.BASEURL + NetworkUtility.LOGIN);

    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {

        if (pDialog!=null && pDialog.isShowing()) {
            pDialog.dismiss();
        }

        switch (type) {
            case "Login":

                JSONObject responseObject = new JSONObject(result);

                GsonBuilder builder = new GsonBuilder();
                builder.setPrettyPrinting();
                Gson gson = builder.create();

                if (responseObject.optString("status").equalsIgnoreCase("success")) {

                    //Toast.makeText(LoginActivity.this, responseObject.optString("message"), Toast.LENGTH_SHORT).show();

                    UserData userData = gson.fromJson(responseObject.getJSONArray("profile").
                            getJSONObject(0).toString(), UserData.class);

                    Paper.book().write(NetworkUtility.USER_INFO, userData);

                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    overridePendingTransition(R.anim.slideinfromright, R.anim.slideouttoleft);
                    supportFinishAfterTransition();

                } else {
                    Toast.makeText(LoginActivity.this, responseObject.optString("message"), Toast.LENGTH_SHORT).show();
                }

                break;
        }

    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {
        if (pDialog!=null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    @Override
    public void onStarted() {

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
                                }

                                @Override
                                public void onFailed(int error) {
                                    MainApplication.isNavigineInitialized = false;
                                    Paper.book().write(MainApplication.NavigineInitialized, false);
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

    @Override
    protected void onResume() {
        super.onResume();

        //double distance = CONST.distance(19.120324, 72.856110, CONST.DESTINATION_LAT, CONST.DESTINATION_LONG,"K");
        //CONST.showNotifications(LoginActivity.this, getString(R.string.app_name), "Your distance :"+distance,2000);
    }
}
