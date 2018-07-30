package com.hipla.smartoffice_new.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hipla.smartoffice_new.activity.BaseActivity;
import com.hipla.smartoffice_new.dialogs.Dialogs;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.activity.DashboardActivity;
import com.hipla.smartoffice_new.adapter.UpcomingMeetingsAdapter;
import com.hipla.smartoffice_new.database.Db_Helper;
import com.hipla.smartoffice_new.databinding.FragmentHomeBinding;
import com.hipla.smartoffice_new.dialogs.RescheduleUpcomingMeetingDialog;
import com.hipla.smartoffice_new.model.UpcomingMeetings;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.model.ZoneInfo;
import com.hipla.smartoffice_new.utils.CONST;
import com.hipla.smartoffice_new.utils.InternetConnectionDetector;
import com.hipla.smartoffice_new.utils.MarshmallowPermissionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
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
public class HomeFragment extends BlankFragment implements StringRequestListener, UpcomingMeetingsAdapter.OnDateClickListener, RescheduleUpcomingMeetingDialog.OnDialogEvent {

    private UpcomingMeetingsAdapter mAdapter;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private FragmentHomeBinding binding;
    private MyReceiver myReceiver;
    private static final int REQUEST_CALL_PERMISSION = 100;
    private UpcomingMeetings upcomingData;
    private NetworkChangeReceiver mNetworkReceiver;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false);
        binding.setFragment(HomeFragment.this);

        initView();
        return binding.getRoot();
    }

    private void initView() {

        mNetworkReceiver = new NetworkChangeReceiver();

        mAdapter = new UpcomingMeetingsAdapter(getActivity());
        mAdapter.setOnDateClickListener(this);
        binding.llUpcomingMeetings.setAdapter(mAdapter);

        binding.pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (InternetConnectionDetector.getInstant(getActivity()).isConnectingToInternet()) {
                    getUpcomingMeetings();
                }
            }
        });

        binding.llUpcomingMeetings.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (binding.llUpcomingMeetings.getChildAt(0) != null) {
                    binding.pullToRefresh.setEnabled(binding.llUpcomingMeetings.getFirstVisiblePosition() == 0
                            && binding.llUpcomingMeetings.getChildAt(0).getTop() == 0);
                }
            }
        });
    }

    public void scheduleMeeting() {

        if (getActivity() != null) {
            ((DashboardActivity) getActivity()).scheduleMeeting();
        }

    }

    public void indboxMeeting() {
        if (getActivity() != null) {
            ((DashboardActivity) getActivity()).indboxMessages();
        }
    }

    public void manageMeeting() {

        if (getActivity() != null) {
            ((DashboardActivity) getActivity()).manageMeeting();
        }

    }

    public void hotDesking() {

        if (getActivity() != null) {
            ((DashboardActivity) getActivity()).setHotDesking();
        }

    }

    public void orderFood() {
        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
        if (userData.getUsertype().equalsIgnoreCase("Guest")) {
            if (Paper.book().read(CONST.IS_DETECTION_STARTED, false)) {
                if (getActivity() != null) {
                    ((DashboardActivity) getActivity()).orderFoodFragment();
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.you_cant_order_food_now), Toast.LENGTH_SHORT).show();
            }
        } else {
            if (Paper.book().read(CONST.IS_DETECTION_STARTED, false)) {
                if (getActivity() != null) {
                    ((DashboardActivity) getActivity()).orderFoodFragment();
                }
            } else {
                Toast.makeText(getActivity(), getString(R.string.you_cant_order_food_now), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getUpcomingMeetings() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));

                new PostStringRequest(getActivity(), requestParameter, HomeFragment.this, "upcomingMeetings",
                        NetworkUtility.BASEURL + NetworkUtility.UPCOMING_MEETINGS);
            }

        } catch (Exception ex) {

        }
    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        try {

            switch (type) {

                case "upcomingMeetings":

                    binding.pullToRefresh.setRefreshing(false);

                    JSONObject responseObject = new JSONObject(result);
                    if (responseObject.optString("status").equalsIgnoreCase("success")) {

                        JSONArray upcomingAppointments = responseObject.getJSONArray("apointments");

                        GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();

                        UpcomingMeetings[] upcomingMeetings = gson.fromJson(upcomingAppointments.toString(), UpcomingMeetings[].class);
                        List<UpcomingMeetings> upcomingMeetingsList = Arrays.asList(upcomingMeetings);

                        upcomingMeetingsList = getUpdatedMeeting(upcomingMeetingsList);

                        if (upcomingMeetingsList != null && upcomingMeetingsList.size() > 0) {
                            binding.llUpcomingMeetings.setVisibility(View.VISIBLE);
                            binding.tvMsg.setVisibility(View.GONE);
                            mAdapter.notifyDataChange(upcomingMeetingsList);
                        } else {
                            binding.tvMsg.setText(getString(R.string.no_upcoming_meetings));
                            binding.tvMsg.setVisibility(View.VISIBLE);
                            binding.llUpcomingMeetings.setVisibility(View.GONE);
                            mAdapter.notifyDataChange(new ArrayList<UpcomingMeetings>());
                        }

                    } else {
                        binding.tvMsg.setVisibility(View.VISIBLE);
                        mAdapter.notifyDataChange(new ArrayList<UpcomingMeetings>());
                    }

                    break;

                case "setMeetingStatus":

                    JSONObject meetingStatusResponse = new JSONObject(result);
                    if (meetingStatusResponse.optString("status").equalsIgnoreCase("success") &&
                            meetingStatusResponse.optString("message").equalsIgnoreCase("your appointment status updated")) {

                        getUpcomingMeetings();
                    } else {
                        Toast.makeText(getActivity(), meetingStatusResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {

    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onResume() {
        super.onResume();

        if ((DashboardActivity) getActivity() != null) {
            ((DashboardActivity) getActivity()).setHomeIcon();

            ((DashboardActivity) getActivity()).registerReceiver(mNetworkReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        if (getActivity()!=null && !((BaseActivity)getActivity()).isDataAvailable()) {
            binding.tvMsg.setText(getString(R.string.no_internet_connection));
            binding.tvMsg.setVisibility(View.VISIBLE);
            binding.llUpcomingMeetings.setVisibility(View.GONE);
        } else {
            binding.tvMsg.setVisibility(View.GONE);
            binding.llUpcomingMeetings.setVisibility(View.VISIBLE);
            getUpcomingMeetings();
        }

        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("intent.start.NewMessage");
        getActivity().registerReceiver(myReceiver, intentFilter);

        if (Paper.book().read(CONST.NEW_MESSAGE, false)) {
            binding.ivNewMessage.setVisibility(View.VISIBLE);
        } else {
            binding.ivNewMessage.setVisibility(View.GONE);
        }

        Paper.book().delete(CONST.FOOD_ITEMS_IN_CART);

        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
        if (userData.getUsertype().equalsIgnoreCase("Guest")) {
            binding.flinbox.setVisibility(View.VISIBLE);
            binding.flhotdesking.setVisibility(View.GONE);
        } else {
            binding.flinbox.setVisibility(View.VISIBLE);
            binding.flhotdesking.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (myReceiver != null && getActivity()!=null) {
            getActivity().unregisterReceiver(myReceiver);
        }

        if (mNetworkReceiver != null && getActivity()!=null) {
            getActivity().unregisterReceiver(mNetworkReceiver);
        }

    }

    @Override
    public void onDateClick(String date) {

    }

    @Override
    public void onAddToCalender(int position, UpcomingMeetings data) {
        onAddEventClicked(data);
    }

    @Override
    public void onOpenDetails(int position, UpcomingMeetings data) {
        Paper.book().write(CONST.APPOINTMENT_DATA, data);

        if (getActivity() != null) {
            ((DashboardActivity) getActivity()).meetingsDetail();
        }
    }

    @Override
    public void onCall(int position, UpcomingMeetings data) {
        upcomingData = data;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (MarshmallowPermissionHelper.getCallPermission(HomeFragment.this,
                    getActivity(), REQUEST_CALL_PERMISSION)) {
                makeCall(data.getGuest().getPhone());
            }
        } else {
            makeCall(data.getGuest().getPhone());
        }
    }

    @Override
    public void onNavigate(int position, UpcomingMeetings data) {

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
    public void onCancelMeeting(int position, UpcomingMeetings data) {
        try {
            Date meetinDate = dateFormat.parse(data.getFdate() + " " + data.getFromtime());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(meetinDate);
            calendar.set(Calendar.MINUTE, CONST.TIME_BEFORE_CANCEL_MEETING_IN_MIN);

            if (new Date().compareTo(calendar.getTime()) < 0) {
                setMeetingStatus(data, "cancel");
            } else {
                Toast.makeText(getActivity(), getString(R.string.you_cant_cancel_meeting_now), Toast.LENGTH_SHORT).show();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onRescheduleMeeting(int position, UpcomingMeetings data) {

        RescheduleUpcomingMeetingDialog mDialog = new RescheduleUpcomingMeetingDialog();
        mDialog.setOnDismissClickListener(HomeFragment.this);
        mDialog.show(getChildFragmentManager(), "dialog");
    }

    @Override
    public void onDismissListener() {

        if (InternetConnectionDetector.getInstant(getActivity()).isConnectingToInternet()) {
            getUpcomingMeetings();
        }
    }

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equalsIgnoreCase("intent.start.NewMessage")) {
                binding.ivNewMessage.setVisibility(View.VISIBLE);
            }

        }
    }

    public void onAddEventClicked(UpcomingMeetings data) {
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
        } catch (Exception ex) {

        }
    }

    private void makeCall(String mobileNo) {
        String uri = "tel:" + mobileNo.trim();

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CALL_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (upcomingData != null)
                        makeCall(upcomingData.getGuest().getPhone());
                }
                return;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void setMeetingStatus(UpcomingMeetings data, String status) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", data.getId()));
                requestParameter.put("status", String.format("%s", status));

                new PostStringRequest(getActivity(), requestParameter, HomeFragment.this, "setMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

                /*if(!mDialog.isShowing()){
                    mDialog.show();
                }*/
            }

        } catch (Exception ex) {

        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (getActivity()!=null && !((BaseActivity)getActivity()).isDataAvailable()) {
                    binding.tvMsg.setText(getString(R.string.no_internet_connection));
                    binding.tvMsg.setVisibility(View.VISIBLE);
                    binding.llUpcomingMeetings.setVisibility(View.GONE);
                } else {
                    binding.tvMsg.setVisibility(View.GONE);
                    binding.llUpcomingMeetings.setVisibility(View.VISIBLE);
                    getUpcomingMeetings();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

    }

    public List<UpcomingMeetings>  getUpdatedMeeting(List<UpcomingMeetings> data){

        List<UpcomingMeetings> meetings = new ArrayList<>();
        for (UpcomingMeetings upcomingMeetings: data){
            try {
                Date date2 = dateFormat.parse(upcomingMeetings.getFdate()+" "+upcomingMeetings.getTotime());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date2);
                calendar.add(Calendar.MINUTE, CONST.EXTRA_TIME_BEFORE_DETECTION_MINS);

                if (calendar.getTime().compareTo(new Date())>=0){
                    meetings.add(upcomingMeetings);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return  meetings;

    }

}
