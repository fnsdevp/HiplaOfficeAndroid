package com.hipla.smartoffice_new.activity;

import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.databinding.ActivitySignUpBinding;
import com.hipla.smartoffice_new.utils.ErrorMessageDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.paperdb.Paper;

public class SignUpActivity extends BaseActivity implements StringRequestListener {

    private ActivitySignUpBinding binding;
    private boolean isAGuest = true;
    private ProgressDialog pDialog;
    private String userType="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        binding.setActivity(SignUpActivity.this);

        initView();
    }

    private void initView() {

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

    }

    public void selectGuest(){
        binding.llGuest.setBackground(getResources().getDrawable(R.drawable.rounded_corner_blue_left));
        binding.tvGuest.setTextColor(getResources().getColor(R.color.text_white));

        binding.llEmployee.setBackground(getResources().getDrawable(R.drawable.rounded_corner_dark_employee));
        binding.tvEmployee.setTextColor(getResources().getColor(R.color.text_white));

        binding.inputLayoutDepartment.setVisibility(View.GONE);
        binding.inputLayoutCompany.setVisibility(View.VISIBLE);

        isAGuest = true;
    }

    public void selectEmployee(){
        binding.llGuest.setBackground(getResources().getDrawable(R.drawable.rounded_corner_dark_employee_left));
        binding.tvGuest.setTextColor(getResources().getColor(R.color.text_white));

        binding.llEmployee.setBackground(getResources().getDrawable(R.drawable.rounded_corner_blue_right));
        binding.tvEmployee.setTextColor(getResources().getColor(R.color.text_white));

        binding.inputLayoutDepartment.setVisibility(View.VISIBLE);
        binding.inputLayoutCompany.setVisibility(View.GONE);

        isAGuest = false;

    }

    public void goToLogin(){
        supportFinishAfterTransition();
        overridePendingTransition(R.anim.slideinfromleft, R.anim.slideouttoright);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        supportFinishAfterTransition();
        overridePendingTransition(R.anim.slideinfromleft, R.anim.slideouttoright);
    }

    public void doSignUp(){
        setHideSoftKeyboard();

        if(checkForError()){
            requestPostData();
        }
    }

    private void requestPostData(){

        if(!pDialog.isShowing())
            pDialog.show();

        HashMap<String, String> requestParameter = new HashMap<>();

        if(!isAGuest){
            userType = "Employee";

            requestParameter.put("username", binding.etUsername.getText().toString().trim());
            requestParameter.put("password", binding.etPassword.getText().toString().trim());
            requestParameter.put("usertype", userType);
            requestParameter.put("type", NetworkUtility.DEVICE_TYPE);
            requestParameter.put("deviceId", Paper.book().read(NetworkUtility.TOKEN, ""));
            requestParameter.put("fname", binding.etFirstname.getText().toString().trim());
            requestParameter.put("lname", binding.etLastname.getText().toString().trim());
            requestParameter.put("designation", binding.etDesignamtion.getText().toString().trim());
            requestParameter.put("department", binding.etDepartment.getText().toString().trim());
            requestParameter.put("company", "");
            requestParameter.put("email", binding.etEmailt.getText().toString().trim());
            requestParameter.put("phone", binding.etPhone.getText().toString().trim());

        }else{
            userType = "Guest";

            requestParameter.put("username", binding.etUsername.getText().toString().trim());
            requestParameter.put("password", binding.etPassword.getText().toString().trim());
            requestParameter.put("usertype", userType);
            requestParameter.put("type", NetworkUtility.DEVICE_TYPE);
            requestParameter.put("deviceId", Paper.book().read(NetworkUtility.TOKEN, ""));
            requestParameter.put("fname", binding.etFirstname.getText().toString().trim());
            requestParameter.put("lname", binding.etLastname.getText().toString().trim());
            requestParameter.put("designation", binding.etDesignamtion.getText().toString().trim());
            requestParameter.put("department", "");
            requestParameter.put("company", binding.etCompany.getText().toString().trim());
            requestParameter.put("email", binding.etEmailt.getText().toString().trim());
            requestParameter.put("phone", binding.etPhone.getText().toString().trim());

        }

        new PostStringRequest(SignUpActivity.this, requestParameter, SignUpActivity.this, "SignUp",
                NetworkUtility.BASEURL+NetworkUtility.SIGNUP);

    }

    private boolean checkForError(){
        if(binding.etUsername.getText().toString().isEmpty()){
            ErrorMessageDialog.getInstant(SignUpActivity.this).show(getResources().getString(R.string.enetr_valid_username));
            return false;
        }else if(binding.etPassword.getText().toString().isEmpty()){
            ErrorMessageDialog.getInstant(SignUpActivity.this).show(getResources().getString(R.string.enetr_valid_password));
            return false;
        }else if(binding.etFirstname.getText().toString().isEmpty()){
            ErrorMessageDialog.getInstant(SignUpActivity.this).show(getResources().getString(R.string.enetr_valid_fname));
            return false;
        }else if(binding.etLastname.getText().toString().isEmpty()){
            ErrorMessageDialog.getInstant(SignUpActivity.this).show(getResources().getString(R.string.enetr_valid_lname));
            return false;
        }else if(binding.etPhone.getText().toString().isEmpty() || !validate1(binding.etPhone.getText().toString())){
            ErrorMessageDialog.getInstant(SignUpActivity.this).show(getResources().getString(R.string.enetr_valid_phone));
            return false;
        }else if(binding.etEmailt.getText().toString().isEmpty() || !validateEmail(binding.etEmailt.getText().toString())){
            ErrorMessageDialog.getInstant(SignUpActivity.this).show(getResources().getString(R.string.enetr_valid_email));
            return false;
        }else if(!isAGuest && binding.etDepartment.getText().toString().isEmpty()){
            ErrorMessageDialog.getInstant(SignUpActivity.this).show(getResources().getString(R.string.enetr_valid_department));
            return false;
        }else if(binding.etDesignamtion.getText().toString().isEmpty() || validate1(binding.etEmailt.getText().toString())){
            ErrorMessageDialog.getInstant(SignUpActivity.this).show(getResources().getString(R.string.enetr_valid_designation));
            return false;
        }

        return true;
    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        if(pDialog.isShowing())
            pDialog.dismiss();

        switch (type){
            case "SignUp":
                try{

                    JSONObject responseObject = new JSONObject(result);

                    GsonBuilder builder = new GsonBuilder();
                    builder.setPrettyPrinting();
                    Gson gson = builder.create();

                    if(responseObject.optString("status").equalsIgnoreCase("success")){

                        Toast.makeText(SignUpActivity.this, responseObject.optString("message"), Toast.LENGTH_SHORT).show();

                        supportFinishAfterTransition();
                        overridePendingTransition(R.anim.slideinfromleft, R.anim.slideouttoright);
                    }else{
                        Toast.makeText(SignUpActivity.this, responseObject.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                }catch (Exception ex){
                    ex.printStackTrace();

                    Toast.makeText(SignUpActivity.this, getResources().getString(R.string.please_try_again), Toast.LENGTH_SHORT).show();

                }
                break;
        }

    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {
        if(pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onStarted() {

    }

}
