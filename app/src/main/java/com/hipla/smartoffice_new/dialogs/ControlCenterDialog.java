package com.hipla.smartoffice_new.dialogs;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hipla.smartoffice_new.application.MainApplication;
import com.hipla.smartoffice_new.database.Db_Helper;
import com.hipla.smartoffice_new.fragment.NavigineMapDialogNew;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.databinding.FragmentControlCenterDialogBinding;
import com.hipla.smartoffice_new.model.UpcomingMeetings;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.model.ZoneInfo;
import com.hipla.smartoffice_new.utils.CONST;
import com.hipla.smartoffice_new.utils.ColorPicker;
import com.navigine.naviginesdk.DeviceInfo;
import com.navigine.naviginesdk.NavigationThread;
import com.navigine.naviginesdk.NavigineSDK;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import io.paperdb.Paper;

public class ControlCenterDialog extends DialogFragment implements ColorPicker.OnColorChangedListener, StringRequestListener {

    private ColorPicker colorPickerView;
    private OnColorSelectedListener onColorSelectedListener;
    private View view;
    private FragmentControlCenterDialogBinding binding;
    private ProgressDialog mDialog;
    private OnDialogEvent mListener;
    private boolean isPasswordVisible = true;
    private BroadcastReceiver mErrorReceiver;
    private BroadcastReceiver mReceiver;
    private BroadcastReceiver inFloorReceiver;
    private BroadcastReceiver mNotInRoom;
    private boolean isInRoom = false;
    private long mErrorMessageTime = 0;
    private static final int ERROR_MESSAGE_TIMEOUT = 5000; // milliseconds
    private TimerTask mTimerTask = null;
    private Timer mTimer = new Timer();
    private Handler mHandler = new Handler();
    private int count = 0, room_1d=0;
    private Runnable mRunnable =
            new Runnable() {
                public void run() {
                    try {
                        if (MainApplication.Navigation != null) {

                            final long timeNow = NavigineSDK.currentTimeMillis();

                            if (mErrorMessageTime > 0 && timeNow > mErrorMessageTime + ERROR_MESSAGE_TIMEOUT) {
                                mErrorMessageTime = 0;
                            }

                            if (MainApplication.Navigation.getMode() == NavigationThread.MODE_IDLE)
                                MainApplication.Navigation.setMode(NavigationThread.MODE_NORMAL);

                            DeviceInfo mDeviceInfo = MainApplication.Navigation.getDeviceInfo();
                            UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

                            if (mDeviceInfo != null) {
                                if (mDeviceInfo.errorCode == 0) {
                                    count++;
                                    if (meetingDetail == null && count > 1) {
                                        //binding.ivQrCode.setVisibility(View.GONE);
                                    }

                                    calculateZone(mDeviceInfo);
                                } else {
                                    if (meetingDetail == null)
                                        binding.ivQrCode.setVisibility(View.VISIBLE);
                                }
                            }

                            if(isInRoom){
                                binding.tvOpenDoor.setTextColor(getResources().getColor(R.color.text_blue));
                            }else{
                                binding.tvOpenDoor.setTextColor(getResources().getColor(R.color.text_light_gray));
                            }
                        }
                    } catch (Exception ex) {

                    }
                }
            };
    private Db_Helper db_helper;
    private ArrayList<ZoneInfo> zoneInfos = new ArrayList<>();
    private ArrayList<PointF[]> zoneInfoPoint = new ArrayList<>();

    public ControlCenterDialog() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /*if (getActivity() != null)
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);*/

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_control_center_dialog, container, false);
        initView(binding.getRoot());
        setBluetoothEnable(true, getActivity());
        return binding.getRoot();
    }

    private void initView(View mView) {
        try {
            db_helper = new Db_Helper(getActivity());

            mDialog = new ProgressDialog(getActivity());
            mDialog.setMessage(getResources().getString(R.string.please_wait));

            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            colorPickerView = new ColorPicker(getActivity(), this);
            colorPickerView.setColor(Color.WHITE);
            colorPickerView.setId(R.id.colorpicker);
            binding.rlColorPicker.addView(colorPickerView, layoutParams);

            binding.tvOpenDoor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isInRoom && room_1d==1)
                        doorOpen();

                    if(isInRoom && room_1d==6){
                        openOutdoor();
                    }
                }
            });

            binding.switchLightOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {

                    } else {

                    }
                }
            });

            binding.tvReconnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (MainApplication.getPahoMqttClient(getActivity()) != null && !MainApplication.getPahoMqttClient(getActivity()).isIsConnected()) {
                            MainApplication.getPahoMqttClient(getActivity()).reconnect();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            binding.tvOpenMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    NavigineMapDialogNew mapDialog = new NavigineMapDialogNew();
                    if (mapDialog != null && mapDialog.getDialog() != null
                            && mapDialog.getDialog().isShowing()) {
                        //dialog is showing so do something
                    } else {
                        //dialog is not showing
                        mapDialog.show(getChildFragmentManager(), "mapDialog");
                    }

                }
            });

            UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                if (!userData.getFname().isEmpty()) {
                    binding.tvName.setText(userData.getFname() + " " + userData.getLname());
                }

                if (!userData.getDesignation().isEmpty()) {
                    binding.llDesignation.setVisibility(View.VISIBLE);
                    binding.tvDesignation.setText(String.format(getString(R.string.designation_format), userData.getDesignation()));
                } else {
                    binding.llDesignation.setVisibility(View.GONE);
                }

                if (!userData.getDepartment().isEmpty()) {
                    binding.llDepartment.setVisibility(View.VISIBLE);
                    binding.tvDepartment.setText(String.format(getString(R.string.department_format), userData.getDepartment()));
                } else {
                    binding.llDepartment.setVisibility(View.GONE);
                }

                if (userData.getCompany() != null && (!userData.getCompany().isEmpty()) &&
                        !userData.getCompany().equalsIgnoreCase("none")) {
                    binding.llCompany.setVisibility(View.VISIBLE);
                    binding.tvCompany.setText(String.format(getString(R.string.company_format), userData.getCompany()));
                } else {
                    binding.llCompany.setVisibility(View.GONE);
                }

                ImageLoader.getInstance().displayImage(userData.getProfile_image(),
                        binding.ivProfilePics, CONST.ErrorWithLoaderRoundedCorner);
            }

            if (meetingDetail != null) {
                if (meetingDetail.getLocation() != null && !meetingDetail.getLocation().isEmpty()) {
                    binding.llLocation.setVisibility(View.VISIBLE);
                    binding.tvLocation.setText(String.format(getString(R.string.location_format), meetingDetail.getRoom_name()));
                } else {
                    binding.llLocation.setVisibility(View.GONE);
                }

                if (!meetingDetail.getFromtime().isEmpty()) {
                    binding.llMeetingTime.setVisibility(View.VISIBLE);
                    binding.tvMeetingTime.setText(String.format(getString(R.string.meeting_timing_format),
                            meetingDetail.getFromtime() + " - " + meetingDetail.getTotime()));
                } else {
                    binding.llMeetingTime.setVisibility(View.GONE);
                }

                if (!meetingDetail.getGuest().getContact().isEmpty()) {
                    binding.llMeetingWith.setVisibility(View.VISIBLE);
                    binding.tvMeetingWith.setText(String.format(getString(R.string.meeting_with_format),
                            meetingDetail.getGuest().getContact()));
                } else {
                    binding.llMeetingWith.setVisibility(View.GONE);
                }

            } else {
                binding.llIseUsername.setVisibility(View.GONE);
                binding.llIsePassword.setVisibility(View.GONE);
                binding.llLocation.setVisibility(View.GONE);
                binding.llMeetingWith.setVisibility(View.GONE);
                binding.llMeetingTime.setVisibility(View.GONE);
            }

            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(mRunnable);
                }
            };
            mTimer.schedule(mTimerTask, 1000, 4000);

            if (userData.getUsertype().equalsIgnoreCase("Guest") && meetingDetail != null) {

                if (meetingDetail != null) {
                    ImageLoader.getInstance().displayImage(meetingDetail.getQrUrl(),
                            binding.ivQrCode, CONST.ErrorWithLoaderNormalCorner);
                    binding.ivQrCode.setVisibility(View.VISIBLE);
                } else {
                    binding.ivQrCode.setVisibility(View.GONE);
                }
            } else {
                if (meetingDetail != null) {
                    ImageLoader.getInstance().displayImage(meetingDetail.getQrUrl(),
                            binding.ivQrCode, CONST.ErrorWithLoaderNormalCorner);
                    binding.ivQrCode.setVisibility(View.VISIBLE);
                } else {
                    ImageLoader.getInstance().displayImage(userData.getQr_image(),
                            binding.ivQrCode, CONST.ErrorWithLoaderNormalCorner);
                    binding.ivQrCode.setVisibility(View.VISIBLE);

                }
            }

            binding.ivMakePasswordVisible.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isPasswordVisible) {
                        //password is visible
                        isPasswordVisible = false;
                        binding.tvIsePassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                        binding.ivMakePasswordVisible.setImageResource(R.drawable.ic_password_hide);
                    } else {
                        //password gets hided
                        isPasswordVisible = true;
                        binding.tvIsePassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        binding.ivMakePasswordVisible.setImageResource(R.drawable.ic_password_visible);
                    }
                }
            });

            if(meetingDetail!=null) {
                if (meetingDetail.getWifiUsername().isEmpty()) {

                    binding.tvIseUsername.setVisibility(View.VISIBLE);

                    Random random = new Random();
                    int value = random.nextInt(9) + 1;

                    binding.tvIseUsername.setText(String.format(getString(R.string.wifi_username_format), db_helper.getWiFiUserName(value)));

                    meetingDetail.setWifiUsername(db_helper.getWiFiUserName(value));

                } else {
                    binding.tvIseUsername.setText(String.format(getString(R.string.wifi_username_format), meetingDetail.getWifiUsername()));
                }

                if (meetingDetail.getWifiPassword().isEmpty()) {

                    Random random = new Random();
                    int value = random.nextInt(9) + 1;

                    binding.tvIsePassword.setVisibility(View.VISIBLE);

                    binding.tvIsePassword.setText(db_helper.getWiFiPassword(value));

                    meetingDetail.setWifiPassword(db_helper.getWiFiPassword(value));

                } else {
                    binding.tvIsePassword.setText(meetingDetail.getWifiPassword());
                }

                Paper.book().write(CONST.CURRENT_MEETING_DATA, meetingDetail);
            }

            if (db_helper != null) {

                zoneInfos = db_helper.getAllZoneInfo();

                for (ZoneInfo zoneInfo :
                        zoneInfos) {
                    zoneInfoPoint.add(convertToPoints(zoneInfo));
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //super.onCreateDialog(savedInstanceState);

        Dialog dialog = new Dialog(getActivity());
        final RelativeLayout root = new RelativeLayout(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        try {

            if (mTimer != null && mTimerTask != null) {
                mTimerTask.cancel();
                mTimer.cancel();

                mTimer = null;
                mTimerTask = null;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (mListener != null) {
            mListener.onDismissListener();
        }
    }

    public interface OnDialogEvent {
        void onDismissListener();
    }

    public void setOnDismissClickListener(OnDialogEvent mListener) {
        this.mListener = mListener;
    }


    @Override
    public void onColorChanged(int color) {

    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        try {

            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            switch (type) {
                case "openDoor":

                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.optString("status").equalsIgnoreCase("success") &&
                            jsonObject.optString("message").equalsIgnoreCase("Door Open")) {

                    } else {
                        openDoor();
                    }

                    break;

                case "getISEWiFI":

                    JSONObject resJsonObject1 = new JSONObject(result);
                    if (resJsonObject1.has("InternalUser")) {
                        UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);
                        //meetingDetail.setWifiUsername(resJsonObject1.getJSONObject("InternalUser").optString("name", ""));
                        //meetingDetail.setWifiPassword(resJsonObject1.getJSONObject("InternalUser").optString("password", ""));

                        Paper.book().write(CONST.CURRENT_MEETING_DATA, meetingDetail);

                        if (!meetingDetail.getWifiUsername().isEmpty()) {
                            binding.tvIseUsername.setVisibility(View.VISIBLE);
                            //binding.tvIseUsername.setText(String.format(getString(R.string.wifi_username_format), meetingDetail.getWifiUsername()));
                        } else {
                            binding.tvIseUsername.setVisibility(View.GONE);
                        }

                        if (!meetingDetail.getWifiPassword().isEmpty()) {
                            binding.tvIsePassword.setVisibility(View.VISIBLE);
                            //binding.tvIsePassword.setText(String.format(getString(R.string.wifi_username_format), meetingDetail.getWifiPassword()));
                        } else {
                            binding.tvIsePassword.setVisibility(View.GONE);

                            if (meetingDetail != null) {
                                //getIseWifiPassword();
                            }
                        }
                    }

                    break;

                case "lightOnOne":

                    break;

                case "lightCloseOne":

                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onStarted() {

    }

    public interface OnColorSelectedListener {
        public void onColorSelected(int color);
    }

    public void openDoor() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("doorstatus", String.format("%s", 0));
                requestParameter.put("firefrom", String.format("%s", "app"));

                new PostStringRequest(getActivity(), requestParameter, ControlCenterDialog.this, "openDoor",
                        NetworkUtility.BASEURL + NetworkUtility.OPEN_DOOR);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }

    private void doorOpen() {
        try {

            if (MainApplication.getPahoMqttClient(getActivity()) != null
                    && MainApplication.getMqttClient(getActivity()) != null) {
                if (MainApplication.getPahoMqttClient(getActivity()).isIsConnected()) {

                    MainApplication.getPahoMqttClient(getActivity()).publishMessage(MainApplication.
                            getMqttClient(getActivity()), "on", 1, CONST.PUBLISH_TOPIC_DOOR_OPEN1);

                    CONST.showNotificationForLocation(getActivity(), getString(R.string.app_name),
                            getString(R.string.door_will_open_inside), 1010);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            doorClose();
                        }
                    }, 5000);
                } else {
                    MainApplication.getPahoMqttClient(getActivity()).reconnect();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private void doorClose() {
        try {

            if (MainApplication.getPahoMqttClient(getActivity()) != null) {

                MainApplication.getPahoMqttClient(getActivity()).publishMessage(MainApplication.
                        getMqttClient(getActivity()), "off", 1, CONST.PUBLISH_TOPIC_DOOR_OPEN1);
            }

        } catch (MqttException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void getIseWifiPassword() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {

                try {
                    RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
                    String URL = NetworkUtility.CREATE_ISE_WIFI_CREDENTIALS;
                    JSONObject jsonBody = new JSONObject();
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Title", "Android Volley Demo");
                    jsonObject.put("Author", "BNK");

                    final String requestBody = jsonBody.toString();

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("VOLLEY", response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("VOLLEY", error.toString());
                        }
                    }) {
                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";
                        }

                        @Override
                        public byte[] getBody() throws AuthFailureError {
                            try {
                                return requestBody == null ? null : requestBody.getBytes("utf-8");
                            } catch (UnsupportedEncodingException uee) {
                                VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                                return null;
                            }
                        }

                        @Override
                        protected Response<String> parseNetworkResponse(NetworkResponse response) {
                            String responseString = "";
                            if (response != null) {
                                responseString = String.valueOf(response.statusCode);
                                // can get more details such as response.headers
                            }
                            return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                        }
                    };

                    requestQueue.add(stringRequest);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception ex) {

        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() != null) {

            IntentFilter intentFilter = new IntentFilter(
                    "android.intent.action.IN_ROOM");

            IntentFilter intentFilter2 = new IntentFilter(
                    "android.intent.action.MAIN");

            IntentFilter intentFilter3 = new IntentFilter(
                    "android.intent.action.NOT_IN_ROOM");

            IntentFilter intentFilter1 = new IntentFilter(
                    "android.intent.action.SUCCESSLOCATION");


            mReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    //extract our message from intent

                    binding.tvOpenDoor.setTextColor(getResources().getColor(R.color.text_blue));
                    isInRoom = true;
                }
            };

            mNotInRoom = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    //extract our message from intent

                    binding.tvOpenDoor.setTextColor(getResources().getColor(R.color.text_light_gray));
                    isInRoom = false;
                }
            };

            inFloorReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    //extract our message from intent
                    UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);
                    UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

                    if (userData != null && !userData.getUsertype().equalsIgnoreCase("Guest")) {

                    }
                }
            };

            mErrorReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    //extract our message from intent

                    binding.tvOpenDoor.setTextColor(getResources().getColor(R.color.text_light_gray));
                    isInRoom = false;


                }
            };

            getActivity().registerReceiver(mReceiver, intentFilter);
            getActivity().registerReceiver(mErrorReceiver, intentFilter1);
            getActivity().registerReceiver(inFloorReceiver, intentFilter2);
            getActivity().registerReceiver(mNotInRoom, intentFilter3);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (getActivity() != null && mReceiver != null) {
            getActivity().unregisterReceiver(mReceiver);
        }

        if (getActivity() != null && mErrorReceiver != null) {
            getActivity().unregisterReceiver(mErrorReceiver);
        }

        if (getActivity() != null && inFloorReceiver != null) {
            getActivity().unregisterReceiver(inFloorReceiver);
        }

        if (getActivity() != null && mNotInRoom != null) {
            getActivity().unregisterReceiver(mNotInRoom);
        }
    }

    private boolean setBluetoothEnable(boolean enable, Context mContext) {
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

    private void calculateZone(DeviceInfo mDeviceInfo) {
        Log.d("Coordinates", "X : " + mDeviceInfo.x + " Y: " + mDeviceInfo.y);
        for (int index = 0; index < zoneInfoPoint.size(); index++) {
            boolean inZone = contains(zoneInfoPoint.get(index), new PointF(mDeviceInfo.x, mDeviceInfo.y));

            if (inZone && zoneInfos.get(index).getId() == 1) {

                //Is in inside conference room
                isInRoom = true;
                room_1d = 1;

                break;
            } else if (inZone && zoneInfos.get(index).getId() == 6) {

                //Is in inside conference room
                isInRoom = true;
                room_1d = 6;

                break;
            } else if (inZone && zoneInfos.get(index).getId() != 1) {
                isInRoom = false;
                room_1d = 0;
            }
        }
    }

    private class IseCredentials extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                /************** For getting response from HTTP URL start ***************/
                URL object = new URL(NetworkUtility.CREATE_ISE_WIFI_CREDENTIALS);

                HttpURLConnection connection = (HttpURLConnection) object
                        .openConnection();
                // int timeOut = connection.getReadTimeout();
                connection.setReadTimeout(60 * 1000);
                connection.setConnectTimeout(60 * 1000);
                String authorization="user1 : Fnspl@2018";
                String encodedAuth="Basic "+ Base64.encode(authorization.getBytes(),Base64.DEFAULT);
                connection.setRequestProperty("Authorization", encodedAuth);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");

                JSONObject parent = new JSONObject();
                JSONObject guestUser = new JSONObject();
                guestUser.put("id","");
                guestUser.put("name","");
                guestUser.put("description","ERS Example user");
                guestUser.put("guestType","Contractor");

                JSONObject guestInfo = new JSONObject();
                guestInfo.put("userName", "");
                guestInfo.put("emailAddress", "");
                guestInfo.put("phoneNumber", "");
                guestInfo.put("password", "Fnspl@2018");
                guestInfo.put("enabled", true);
                guestInfo.put("smsServiceProvider", "Verizon");

                JSONObject guestAccessInfo = new JSONObject();
                guestAccessInfo.put("validDays", "1");
                guestAccessInfo.put("fromDate", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.add(Calendar.DAY_OF_YEAR, 1);
                guestAccessInfo.put("toDate", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(calendar.getTime()));
                guestAccessInfo.put("location", "TCS Banyan Park");

                guestUser.put("guestInfo", guestUser);
                guestUser.put("guestAccessInfo", guestAccessInfo);
                guestUser.put("portalId","");

                parent.put("GuestUser", guestUser);

                OutputStream os = connection.getOutputStream();
                os.write(parent.toString().getBytes("UTF-8"));
                os.close();

                int responseCode = connection.getResponseCode();
                //String responseMsg = connection.getResponseMessage();

                if (responseCode == 200) {
                    InputStream inputStr = connection.getInputStream();

                    return "";
                }else{
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();

                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(s!=null){

            }else{

            }
        }
    }

    private void openOutdoor() {
        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

        String urlParameters = "userid=" + userData.getId() +
                "&doorstatus=0" +
                "&firefrom=app";

        new RoutineFetch().execute(urlParameters);
    }

    private class RoutineFetch extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                Log.d("Tester", "Before request");
                URL url = new URL(NetworkUtility.OPEN_DOOR);
                String urlParameters = params[0];

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");
                connection.setDoOutput(true);

                DataOutputStream dStream = new DataOutputStream(connection.getOutputStream());
                dStream.writeBytes(urlParameters);
                dStream.flush();
                dStream.close();
                int responseCode = connection.getResponseCode();

                final StringBuilder output = new StringBuilder("Request URL " + url);
                output.append(System.getProperty("line.separator") + "Request Parameters " + urlParameters);
                output.append(System.getProperty("line.separator") + "Response Code " + responseCode);
                output.append(System.getProperty("line.separator") + "Type " + "POST");
                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = "";
                StringBuilder responseOutput = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    responseOutput.append(line);
                }
                br.close();

                result = responseOutput.toString();

            } catch (Exception e1) {
                e1.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                return result;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
            //macAddress = getMacAddr();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
               JSONObject jsonObject = new JSONObject(s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
