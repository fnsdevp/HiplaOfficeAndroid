package com.hipla.smartoffice_new.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.activity.DashboardActivity;
import com.hipla.smartoffice_new.adapter.MeetingListAdapter;
import com.hipla.smartoffice_new.database.Db_Helper;
import com.hipla.smartoffice_new.databinding.FragmentMyAppointmentsBinding;
import com.hipla.smartoffice_new.model.Appointments;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.model.ZoneInfo;
import com.hipla.smartoffice_new.utils.CONST;
import com.hipla.smartoffice_new.utils.MarshmallowPermissionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class MyAppointmentsFragment extends Fragment implements StringRequestListener, MeetingListAdapter.OnAppointmentClickListener {

    private static final int REQUEST_CALL_PERMISSION = 100;
    private FragmentMyAppointmentsBinding binding;
    private static MeetingListAdapter meetingListAdapter;
    private List<Appointments> mData = new ArrayList<>();
    private ProgressDialog mDialog;
    private Appointments appointments;
    private final int REQUEST_ACCESS_ACCOUNT_PERMISSION = 1000;
    static final int REQUEST_ACCOUNT_PICKER = 1001;
    static final int REQUEST_AUTHORIZATION = 1002;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1003;
    private GoogleAccountCredential mCredential;
    private static final String[] SCOPES = {CalendarScopes.CALENDAR_READONLY};
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    public MyAppointmentsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_appointments, container, false);
        initView();
        return binding.getRoot();
    }

    private void initView() {

        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage(getResources().getString(R.string.please_wait));

        binding.rvMeetings.setLayoutManager(new LinearLayoutManager(getActivity()));

        meetingListAdapter = new MeetingListAdapter(getActivity(), new ArrayList<Appointments>());
        meetingListAdapter.setOnAttendanceClickListener(this);
        binding.rvMeetings.setAdapter(meetingListAdapter);

        getMyAppointment();

        binding.pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMyAppointment();
            }
        });

        mCredential = GoogleAccountCredential.usingOAuth2(
                getActivity(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
    }

    public void getMyAppointment(){
        try{
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if(userData!=null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s",userData.getId()));

                new PostStringRequest(getActivity(), requestParameter, MyAppointmentsFragment.this, "myAppointments",
                        NetworkUtility.BASEURL + NetworkUtility.GUEST_ALL_APPOINTMENTS);

                if(!mDialog.isShowing()){
                    mDialog.show();
                }
            }

        }catch (Exception ex){

        }
    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        try{

            if(mDialog.isShowing()){
                mDialog.dismiss();
            }

            if (binding.pullToRefresh.isRefreshing()) {
                binding.pullToRefresh.setRefreshing(false);
            }


            switch (type){

                case "myAppointments":

                    JSONObject responseObject = new JSONObject(result);
                    if(responseObject.optString("status").equalsIgnoreCase("success")){

                        JSONArray upcomingAppointments = responseObject.getJSONArray("apointments");

                        GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();

                        Appointments[] upcomingMeetings = gson.fromJson(upcomingAppointments.toString(), Appointments[].class);
                        mData = Arrays.asList(upcomingMeetings);

                        if (mData.size() > 0) {
                            meetingListAdapter.notifyDataChange(mData);
                            binding.tvMsg.setVisibility(View.GONE);
                        }else {
                            binding.tvMsg.setVisibility(View.VISIBLE);
                            //Toast.makeText(getActivity(), responseObject.optString("message"), Toast.LENGTH_SHORT).show();
                        }

                    }else{
                        binding.tvMsg.setVisibility(View.VISIBLE);
                    }

                    break;

                case "setMeetingStatus":

                    JSONObject meetingStatusResponse = new JSONObject(result);
                    if(meetingStatusResponse.optString("status").equalsIgnoreCase("success") &&
                            meetingStatusResponse.optString("message").equalsIgnoreCase("your appointment status updated")){
                        getMyAppointment();
                    }else{
                        Toast.makeText(getActivity(), meetingStatusResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {
        if(mDialog.isShowing()){
            mDialog.dismiss();
        }
    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onAddToCalender(int position, Appointments data) {
        onAddEventClicked(data);
    }

    @Override
    public void onOpenDetails(int position, Appointments data) {
        Paper.book().write(CONST.APPOINTMENT_DATA, data);

        if(getActivity()!=null){
            ((DashboardActivity) getActivity()).meetingsDetail();
        }

    }

    @Override
    public void onCall(int position, Appointments data) {
        appointments = data;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (MarshmallowPermissionHelper.getCallPermission(MyAppointmentsFragment.this,
                    getActivity(), REQUEST_CALL_PERMISSION)) {
                makeCall(appointments.getUserdetails().getPhone());
            }
        }else{
            makeCall(appointments.getUserdetails().getPhone());
        }
    }

    @Override
    public void onNavigate(int position, Appointments data) {
        if (Paper.book().read(CONST.IS_DETECTION_STARTED, false)) {
            Db_Helper db_helper = new Db_Helper(getActivity());
            ZoneInfo zoneInfo = db_helper.getZoneInfo(String.format("%s", "1"));
            if (zoneInfo != null) {
                String[] location = zoneInfo.getCenterPoint().split(",");

                NavigineMapDialogNew mapDialog = new NavigineMapDialogNew();
                Bundle bundle = new Bundle();
                bundle.putString(CONST.POINTX, location[0]);
                bundle.putString(CONST.POINTY, location[1]);
                mapDialog.setArguments(bundle);
                if (mapDialog != null && mapDialog.getDialog() != null
                        && mapDialog.getDialog().isShowing()) {
                    //dialog is showing so do something
                } else {
                    //dialog is not showing
                    mapDialog.show(getChildFragmentManager(), "mapDialog");
                }
            }
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.no_info_available), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCancelMeeting(int position, Appointments data) {
        try {
            Date meetinDate = dateFormat.parse(data.getFdate() + " " + data.getFromtime());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(meetinDate);
            calendar.set(Calendar.MINUTE, CONST.TIME_BEFORE_CANCEL_MEETING_IN_MIN);

            if(new Date().compareTo(calendar.getTime())<0) {
                setMeetingStatus(data, "cancel");
            }else{
                Toast.makeText(getActivity(), getString(R.string.you_cant_cancel_meeting_now), Toast.LENGTH_SHORT).show();
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void setMeetingStatus(Appointments data, String status){
        try{
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if(userData!=null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s",userData.getId()));
                requestParameter.put("appid", String.format("%s",data.getId()));
                requestParameter.put("status", String.format("%s",status));

                new PostStringRequest(getActivity(), requestParameter, MyAppointmentsFragment.this, "setMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

                if(!mDialog.isShowing()){
                    mDialog.show();
                }
            }

        }catch (Exception ex){

        }
    }

    public void filterMessage(String status){
        if(meetingListAdapter!=null){
            meetingListAdapter.filter(status);
        }
    }

    public void filterByName(String name){
        if(meetingListAdapter!=null){
            meetingListAdapter.filterByName(name);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CALL_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (appointments != null)
                        makeCall(appointments.getUserdetails().getPhone());
                }
                return;
            }

            case REQUEST_ACCESS_ACCOUNT_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }
                return;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void makeCall(String mobileNo) {
        String uri = "tel:" + mobileNo.trim();

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void checkAccountPermission() {
        if (Build.VERSION.SDK_INT > 22) {
            if (MarshmallowPermissionHelper.getAccessAccountPermission(MyAppointmentsFragment.this
                    , getActivity(), REQUEST_ACCESS_ACCOUNT_PERMISSION)) {

            }
        } else {
            //call the google account to access Calendar API

        }
    }

    public void onAddEventClicked(Appointments data) {
        try {
            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType("vnd.android.cursor.item/event");

            Calendar cal = Calendar.getInstance();

            Date meetingDateTime = dateFormat.parse(data.getFdate() + " " + data.getFromtime());
            Date meetingDateTimeEnd = dateFormat.parse(data.getFdate() + " " + data.getTotime());

            cal.setTime(meetingDateTime);
            long startTime = cal.getTimeInMillis();

            cal.setTime(meetingDateTimeEnd);
            long endTime = cal.getTimeInMillis();

            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime);
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime);
            //intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

            intent.putExtra(CalendarContract.Events.TITLE, getString(R.string.app_name));
            intent.putExtra(CalendarContract.Events.DESCRIPTION, data.getAgenda());
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "TCS Banyan Park");
            //intent.putExtra(CalendarContract.Events.RRULE, "FREQ=YEARLY");

            startActivity(intent);
        }catch (Exception ex){

        }
    }

}
