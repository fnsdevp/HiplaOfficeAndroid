package com.hipla.smartoffice_new.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ForgetPasswordActivity extends BaseActivity implements StringRequestListener {

    private EditText input_email;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forget_password);
        initView();
    }

    private void initView() {
        input_email = (EditText) findViewById(R.id.input_email);

        findViewById(R.id.txtSignuplogin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });

        findViewById(R.id.loginB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!input_email.getText().toString().trim().isEmpty())
                    submitUserData();
                else
                    Toast.makeText(ForgetPasswordActivity.this, getString(R.string.enter_your_email), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitUserData() {
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        if (pDialog != null && !pDialog.isShowing())
            pDialog.show();

        HashMap<String, String> requestParameter = new HashMap<>();
        requestParameter.put("email", "" + input_email.getText().toString().trim());

        new PostStringRequest(ForgetPasswordActivity.this, requestParameter, ForgetPasswordActivity.this, "Login",
                NetworkUtility.BASEURL + NetworkUtility.FORGET_PASSWORD);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        supportFinishAfterTransition();
        overridePendingTransition(R.anim.slideinfromleft, R.anim.slideouttoright);
    }

    public void goToLogin() {
        supportFinishAfterTransition();
        overridePendingTransition(R.anim.slideinfromleft, R.anim.slideouttoright);
    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        try {
            if (pDialog != null && pDialog.isShowing())
                pDialog.dismiss();

            JSONObject resJsonObject = new JSONObject(result);
            if(resJsonObject.optString("status").equalsIgnoreCase("success")){
                Toast.makeText(this, resJsonObject.optString("message"), Toast.LENGTH_SHORT).show();
                goToLogin();
            }else{
                Toast.makeText(this, resJsonObject.optString("message"), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onStarted() {

    }
}
