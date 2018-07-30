package com.hipla.smartoffice_new.activity;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import com.hipla.smartoffice_new.application.MainApplication;
import com.hipla.smartoffice_new.database.Db_Helper;
import com.hipla.smartoffice_new.dialogs.Dialogs;
import com.hipla.smartoffice_new.fragment.AboutUsFragment;
import com.hipla.smartoffice_new.fragment.EditProfileFragment;
import com.hipla.smartoffice_new.fragment.GroupMeetingDetailFragment;
import com.hipla.smartoffice_new.fragment.HomeFragment;
import com.hipla.smartoffice_new.fragment.IndboxMessagesFragment;
import com.hipla.smartoffice_new.fragment.ManageMeetingFragment;
import com.hipla.smartoffice_new.fragment.MeetingDetailFragment;
import com.hipla.smartoffice_new.fragment.NavigineMapDialogNew;
import com.hipla.smartoffice_new.fragment.NotificationFragment;
import com.hipla.smartoffice_new.fragment.OrderFoodFragment;
import com.hipla.smartoffice_new.fragment.RequestMeetingDetailFragment;
import com.hipla.smartoffice_new.fragment.ScheduleMeetingFragment;
import com.hipla.smartoffice_new.fragment.SetAvaibilityFragment;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.services.MyNavigationService;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.databinding.ActivityDashboardBinding;
import com.hipla.smartoffice_new.dialogs.ControlCenterDialog;
import com.hipla.smartoffice_new.dialogs.ExtendMeetingDialog;
import com.hipla.smartoffice_new.dialogs.HotDeskingDialog;
import com.hipla.smartoffice_new.model.UpcomingMeetings;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.utils.CONST;
import com.hipla.smartoffice_new.utils.ErrorMessageDialog;
import com.hipla.smartoffice_new.utils.InternetConnectionDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import io.paperdb.Paper;

public class DashboardActivity extends BaseActivity implements StringRequestListener, ControlCenterDialog.OnDialogEvent {

    private ActivityDashboardBinding binding;
    private MyReceiver myReceiver;
    private boolean isShowingControlCenter = false, isShowingPopup = false, hideControlCenter = false;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private boolean isShowingHotDesking = false;
    private NetworkChangeReceiver mNetworkReceiver;
    private Dialog dialog;

    public DashboardActivity() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainApplication.createMqttConnection(getApplicationContext());

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard);
        binding.setActivity(DashboardActivity.this);

        initView();

    }

    private void initView() {

        //mNetworkReceiver = new NetworkChangeReceiver();

        binding.ivScheduleSettings.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //Button Pressed

                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    scheduleMeeting();
                }

                return true;
            }
        });

        goToHome();

        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
        if (userData.getUsertype().equalsIgnoreCase("Guest")) {
            binding.llSetAvaibility.setVisibility(View.GONE);
            binding.llHotDesking.setVisibility(View.GONE);
            if (Paper.book().read(CONST.IS_DETECTION_STARTED, false)) {
                binding.llControlPanel.setVisibility(View.VISIBLE);
            } else {
                binding.llControlPanel.setVisibility(View.GONE);
            }
        } else {
            binding.llSetAvaibility.setVisibility(View.VISIBLE);
            binding.llControlPanel.setVisibility(View.VISIBLE);
            binding.llHotDesking.setVisibility(View.GONE);
        }

        binding.drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                try {
                    UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
                    if (userData.getUsertype().equalsIgnoreCase("Guest")) {
                        if (Paper.book().read(CONST.IS_DETECTION_STARTED, false) &&
                                Paper.book().read(CONST.CURRENT_MEETING_DATA) != null) {

                            UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);
                            Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate()
                                    + " " + meetingDetail.getTotime());

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(meetingDateTime);
                            cal.add(Calendar.MINUTE, CONST.EXTRA_TIME_BEFORE_DETECTION_MINS);

                            if (new Date().compareTo(cal.getTime()) < 0)
                                binding.llControlPanel.setVisibility(View.VISIBLE);
                            else
                                binding.llControlPanel.setVisibility(View.GONE);

                        } else {
                            binding.llControlPanel.setVisibility(View.GONE);
                        }

                        binding.llHotDesking.setVisibility(View.GONE);

                    } else {
                        binding.llControlPanel.setVisibility(View.VISIBLE);
                        binding.llHotDesking.setVisibility(View.GONE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    public void scheduleMeeting() {
        scheduleMeetingIcon();

        setFragment(new ScheduleMeetingFragment(), CONST.SCHEDULE_MEETING);
    }

    public void scheduleMeetingIcon() {
        binding.ivHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_home));
        binding.ivMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_manage_meeting));
        binding.ivNotification.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification));
        binding.ivAddMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_meeting_ac));
        binding.ivScheduleSettings.setImageResource(R.drawable.ic_action_schedule_meetings_active);

        binding.tvTab1.setTextColor(getResources().getColor(R.color.text_white));
        binding.tvTab2.setTextColor(getResources().getColor(R.color.text_white));
        binding.tvTab3.setTextColor(getResources().getColor(R.color.text_blue));
        binding.tvTab4.setTextColor(getResources().getColor(R.color.text_white));
        binding.tvTab5.setTextColor(getResources().getColor(R.color.text_white));
    }

    public void manageMeeting() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        setManageMeetingIcon();

        setFragment(new ManageMeetingFragment(), CONST.MANAGE_MEETING);
    }

    public void setManageMeetingIcon() {
        binding.ivHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_home));
        binding.ivMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_manage_meeting_active));
        binding.ivNotification.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification));
        binding.ivAddMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_meeting));
        binding.ivScheduleSettings.setImageResource(R.drawable.ic_action_schedule_meetings);

        binding.tvTab1.setTextColor(getResources().getColor(R.color.text_white));
        binding.tvTab2.setTextColor(getResources().getColor(R.color.text_blue));
        binding.tvTab3.setTextColor(getResources().getColor(R.color.text_white));
        binding.tvTab4.setTextColor(getResources().getColor(R.color.text_white));
        binding.tvTab5.setTextColor(getResources().getColor(R.color.text_white));
    }

    public void meetingsDetail() {
        setManageMeetingIcon();

        setFragment(new MeetingDetailFragment(), CONST.MEETING_DETAILS);
    }

    public void requestedMeetingsDetail() {
        binding.ivHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_home));
        binding.ivMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_manage_meeting_active));
        binding.ivNotification.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification));
        binding.ivAddMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_meeting));
        binding.ivScheduleSettings.setImageResource(R.drawable.ic_action_schedule_meetings);

        setFragment(new RequestMeetingDetailFragment(), CONST.REQUESTED_MEETING_DETAILS);
    }

    public void requestedGroupMeetingsDetail() {
        binding.ivHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_home));
        binding.ivMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_manage_meeting_active));
        binding.ivNotification.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification));
        binding.ivAddMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_meeting));
        binding.ivScheduleSettings.setImageResource(R.drawable.ic_action_schedule_meetings);

        setFragment(new GroupMeetingDetailFragment(), CONST.REQUESTED_GROUP_MEETING_DETAILS);
    }

    public void indboxMessages() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        setHomeIcon();

        setFragment(new IndboxMessagesFragment(), CONST.INDBOX_MESSGAES);
    }

    public void profileManagement() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        setFragment(new EditProfileFragment(), CONST.PROFILE_MANAGMENT);
    }

    public void aboutUs() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        setFragment(new AboutUsFragment(), CONST.ABOUT_US);
    }

    public void logout() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        Paper.book().delete(NetworkUtility.USER_INFO);
        Paper.book().delete(CONST.CURRENT_MEETING_DATA);
        Paper.book().delete(CONST.IS_DETECTION_STARTED);

        Db_Helper db_helper = new Db_Helper(DashboardActivity.this);
        db_helper.deleteNotificationInfo();

        CONST.cancelEndMeetingJob(DashboardActivity.this, CONST.SCHEDULE_END_MEETING_JOB_ID);
        CONST.cancelExtendMeetingJob(DashboardActivity.this, CONST.SCHEDULE_EXTEND_MEETING_JOB_ID);
        CONST.cancelStartMeetingJob(DashboardActivity.this, CONST.SCHEDULE_START_MEETING_JOB_ID);
        CONST.cancelMeetingETAJob(DashboardActivity.this, CONST.SCHEDULE_ETA_DETECTION_JOB_ID);

        startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
        supportFinishAfterTransition();
        overridePendingTransition(R.anim.slideinfromleft, R.anim.slideouttoright);
    }

    public void orderFoodFragment() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        setFragment(new OrderFoodFragment(), CONST.ORDER_FOOD);
    }


    public void setAvaibility() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        setFragment(new SetAvaibilityFragment(), CONST.SET_AVAIBILITY);
    }

    public void setNotification() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        binding.viewNotification.setVisibility(View.GONE);

        setNotificationIcon();

        setFragment(new NotificationFragment(), CONST.SET_NOTIFICATION);
    }

    public void setNotificationIcon() {
        binding.ivHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_home));
        binding.ivMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_manage_meeting));
        binding.ivNotification.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification_active));
        binding.ivAddMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_meeting));
        binding.ivScheduleSettings.setImageResource(R.drawable.ic_action_schedule_meetings);

        binding.tvTab1.setTextColor(getResources().getColor(R.color.text_white));
        binding.tvTab2.setTextColor(getResources().getColor(R.color.text_white));
        binding.tvTab3.setTextColor(getResources().getColor(R.color.text_white));
        binding.tvTab4.setTextColor(getResources().getColor(R.color.text_blue));
        binding.tvTab5.setTextColor(getResources().getColor(R.color.text_white));
    }

    public void setControlPanel() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        if (!isShowingControlCenter) {
            ControlCenterDialog mDialog = new ControlCenterDialog();
            mDialog.setOnDismissClickListener(new ControlCenterDialog.OnDialogEvent() {
                @Override
                public void onDismissListener() {
                    isShowingControlCenter = false;
                }
            });
            mDialog.show(getSupportFragmentManager(), "contolCenter");
        }
    }

    public void setHotDesking() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        if (!isShowingHotDesking) {
            isShowingHotDesking = true;

            HotDeskingDialog mDialog = new HotDeskingDialog();
            mDialog.setOnDismissClickListener(new HotDeskingDialog.OnDialogEvent() {
                @Override
                public void onDismissListener() {
                    isShowingHotDesking = false;
                }
            });
            mDialog.show(getSupportFragmentManager(), "hotDesking");
        }
    }


    public void openDrawerOnClick() {
        if (!binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.openDrawer(Gravity.RIGHT);
        }
    }

    public void backFromFragment() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            boolean done = getSupportFragmentManager().popBackStackImmediate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        CONST.scheduleMeetingFetchJob(DashboardActivity.this, 1300, CONST.FETCH_JOB_ID);

        requestPostData();

        //registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        if (Paper.book().read(CONST.IS_DETECTION_STARTED, false)) {

            if (Paper.book().read(CONST.CURRENT_MEETING_DATA) != null) {

                UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

                try {

                    Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate()
                            + " " + meetingDetail.getFromtime());

                    Date meetingDateTimeEnd = dateFormat.parse(meetingDetail.getFdate()
                            + " " + meetingDetail.getTotime());

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(meetingDateTimeEnd);

                    if (cal.getTimeInMillis() + ((CONST.TIME_AFTER_MEETING_IN_SEC + 600) * 1000) > System.currentTimeMillis()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (!isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
                                startForegroundService(new Intent(DashboardActivity.this, MyNavigationService.class));
                        } else {
                            if (!isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
                                startService(new Intent(DashboardActivity.this, MyNavigationService.class));
                        }

                        if (!hideControlCenter) {
                            hideControlCenter = true;

                            cal.add(Calendar.MINUTE, CONST.EXTRA_TIME_BEFORE_DETECTION_MINS);

                            if (new Date().compareTo(cal.getTime()) < 0)
                                setControlPanel();
                        }

                        Paper.book().delete(CONST.RUNNING_LATE);

                    } else {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (!isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
                                stopService(new Intent(DashboardActivity.this, MyNavigationService.class));
                        } else {
                            if (!isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
                                stopService(new Intent(DashboardActivity.this, MyNavigationService.class));
                        }

                        Paper.book().delete(CONST.IS_DETECTION_STARTED);
                        Paper.book().delete(CONST.CURRENT_MEETING_DATA);

                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
                    stopService(new Intent(DashboardActivity.this, MyNavigationService.class));
            } else {
                if (!isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
                    stopService(new Intent(DashboardActivity.this, MyNavigationService.class));
            }

            if (Paper.book().read(CONST.CURRENT_MEETING_DATA) != null) {

                UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

                try {

                    Date meetingDateTime = dateFormat.parse(meetingDetail.getFdate()
                            + " " + meetingDetail.getFromtime());

                    Date meetingDateTimeEnd = dateFormat.parse(meetingDetail.getFdate()
                            + " " + meetingDetail.getTotime());

                    Calendar cal = Calendar.getInstance();
                    cal.setTime(meetingDateTimeEnd);

                    if (new Date().compareTo(meetingDateTime) >= 0 && new Date().compareTo(meetingDateTimeEnd) <= 0) {
                        //start location update and check if he is under 1 km then start beacon detection and set end
                        CONST.scheduleLocationDetectionJob(getApplicationContext(), 1000, CONST.SCHEDULE_LOCATION_JOB_ID);
                    } else if (cal.getTimeInMillis() + ((CONST.TIME_AFTER_MEETING_IN_SEC + 600) * 1000) < System.currentTimeMillis()) {

                        Paper.book().delete(CONST.CURRENT_MEETING_DATA);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }

        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("intent.start.Navigation");
        intentFilter.addAction("intent.start.Reschedule.Or.Finish");
        intentFilter.addAction("intent.start.Finish.Navigation");
        intentFilter.addAction("intent.start.NewNotification");
        registerReceiver(myReceiver, intentFilter);

        if (Paper.book().read(CONST.NEEDS_TO_RESCHEDULE, false) && !isShowingPopup) {
            isShowingPopup = true;

            ErrorMessageDialog.getInstant(DashboardActivity.this, new ErrorMessageDialog.OkCancelClickListener() {
                @Override
                public void onOkClickListener() {
                    isShowingPopup = false;

                    ExtendMeetingDialog mDialog = new ExtendMeetingDialog();
                    mDialog.setOnDismissClickListener(new ExtendMeetingDialog.OnDialogEvent() {
                        @Override
                        public void onDismissListener() {
                            Paper.book().delete(CONST.NEEDS_TO_RESCHEDULE);
                        }
                    });
                    mDialog.show(getSupportFragmentManager(), "extendDialog");
                }

                @Override
                public void onCancelClickListener() {
                    isShowingPopup = false;
                    Paper.book().delete(CONST.NEEDS_TO_RESCHEDULE);
                }
            }).show(getString(R.string.want_to_extend), getString(R.string.app_name), "Yes", "No, Thanks");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (myReceiver != null) {
            unregisterReceiver(myReceiver);
        }

        if (mNetworkReceiver != null) {
            //unregisterReceiver(mNetworkReceiver);
        }
    }

    private void requestPostData() {

        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

        if (userData != null) {

            HashMap<String, String> requestParameter = new HashMap<>();
            requestParameter.put("userid", "" + userData.getId());
            requestParameter.put("reg", "" + Paper.book().read(NetworkUtility.TOKEN, ""));
            requestParameter.put("type", "Android");

            new PostStringRequest(DashboardActivity.this, requestParameter, DashboardActivity.this, "Login",
                    NetworkUtility.BASEURL + NetworkUtility.DEVICE_UPDATE);

        }

    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        switch (type) {
            case "setMeetingStatus":
                JSONObject response = new JSONObject(result);


                break;
        }
    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {

    }

    @Override
    public void onStarted() {

    }

    public void setFragment(final Fragment fragment, final String fragmentName) {
        try {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
                    Fragment oldfragment = getSupportFragmentManager().findFragmentByTag(fragmentName);

                    t.replace(R.id.realtabcontent, fragment, fragmentName);
                    t.addToBackStack(null);

                    t.commit();
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void goToHome() {
        if (binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT);
        }

        setHomeIcon();

        setFragment(new HomeFragment(), CONST.HOME_FRAGMENT);
    }

    public void setHomeIcon() {
        binding.ivHome.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_home_active));
        binding.ivMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_manage_meeting));
        binding.ivNotification.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_notification));
        binding.ivAddMeeting.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_meeting));
        binding.ivScheduleSettings.setImageResource(R.drawable.ic_action_schedule_meetings);

        binding.tvTab1.setTextColor(getResources().getColor(R.color.text_blue));
        binding.tvTab2.setTextColor(getResources().getColor(R.color.text_white));
        binding.tvTab3.setTextColor(getResources().getColor(R.color.text_white));
        binding.tvTab4.setTextColor(getResources().getColor(R.color.text_white));
        binding.tvTab5.setTextColor(getResources().getColor(R.color.text_white));
    }

    @Override
    public void onBackPressed() {

        Fragment oldfragment = getSupportFragmentManager().findFragmentByTag(CONST.HOME_FRAGMENT);
        if (oldfragment != null && oldfragment.isVisible()) {
            finish();
        } else {
            //super.onBackPressed();
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                boolean done = getSupportFragmentManager().popBackStackImmediate();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            if (isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
                stopService(new Intent(DashboardActivity.this, MyNavigationService.class));

            Paper.book().delete(CONST.RUNNING_LATE);

            if (MainApplication.getPahoMqttClient(DashboardActivity.this) != null &&
                    MainApplication.getMqttClient(DashboardActivity.this) != null) {

                MainApplication.getPahoMqttClient(DashboardActivity.this).disconnect(MainApplication.getMqttClient(DashboardActivity.this));
            }
        } catch (Exception ex) {

        }

    }

    private boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDismissListener() {
        isShowingControlCenter = false;
    }

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {

            //Toast.makeText(context, "Action: " + intent.getAction(), Toast.LENGTH_SHORT).show();

            if (intent.getAction().equalsIgnoreCase("intent.start.Navigation")) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
                        startForegroundService(new Intent(DashboardActivity.this, MyNavigationService.class));
                } else {
                    if (!isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
                        startService(new Intent(DashboardActivity.this, MyNavigationService.class));
                }

                if (!hideControlCenter) {
                    hideControlCenter = true;
                    setControlPanel();
                }

                UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
                if (userData.getUsertype().equalsIgnoreCase("Guest")) {
                    if (Paper.book().read(CONST.IS_DETECTION_STARTED, false)) {
                        binding.llControlPanel.setVisibility(View.VISIBLE);
                    } else {
                        binding.llControlPanel.setVisibility(View.GONE);
                    }
                } else {
                    binding.llControlPanel.setVisibility(View.VISIBLE);
                }

            } else if (intent.getAction().equalsIgnoreCase("intent.start.Reschedule.Or.Finish") && !isShowingPopup) {

                isShowingPopup = true;

                ErrorMessageDialog.getInstant(DashboardActivity.this, new ErrorMessageDialog.OkCancelClickListener() {
                    @Override
                    public void onOkClickListener() {
                        isShowingPopup = false;

                        ExtendMeetingDialog mDialog = new ExtendMeetingDialog();
                        mDialog.setOnDismissClickListener(new ExtendMeetingDialog.OnDialogEvent() {
                            @Override
                            public void onDismissListener() {
                                Paper.book().delete(CONST.NEEDS_TO_RESCHEDULE);
                            }
                        });
                        mDialog.show(getSupportFragmentManager(), "extendDialog");
                    }

                    @Override
                    public void onCancelClickListener() {
                        isShowingPopup = false;

                        Paper.book().delete(CONST.NEEDS_TO_RESCHEDULE);

                    }
                }).show(getString(R.string.want_to_extend), getString(R.string.app_name), "Yes", "No, Thanks");
            } else if (intent.getAction().equalsIgnoreCase("intent.start.Reschedule.Or.Finish")) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (!isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
                        stopService(new Intent(DashboardActivity.this, MyNavigationService.class));
                } else {
                    if (!isMyServiceRunning(DashboardActivity.this, MyNavigationService.class))
                        stopService(new Intent(DashboardActivity.this, MyNavigationService.class));
                }
            } else if (intent.getAction().equalsIgnoreCase("intent.start.NewNotification")) {

                binding.viewNotification.setVisibility(View.VISIBLE);
            }

        }

    }

    private void stopCurrentMeeting() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
            UpcomingMeetings upcomingMeetings = Paper.book().read(CONST.CURRENT_MEETING_DATA);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", upcomingMeetings.getId()));
                requestParameter.put("status", String.format("%s", "end"));

                new PostStringRequest(DashboardActivity.this, requestParameter,
                        DashboardActivity.this, "setMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void openIndoorNavigation() {
        NavigineMapDialogNew mapDialogNew = new NavigineMapDialogNew();
        mapDialogNew.show(getSupportFragmentManager(), "indoorMap");
    }

    public String getDeviceID() {
        String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        return deviceId;
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (isDataAvailable()) {
                    if(dialog!=null){
                        dialog.dismiss();
                    }
                } else if(!Dialogs.isDialogShowing){
                    dialog = Dialogs.dialogNoConnection(DashboardActivity.this, new Dialogs.OnCallback() {
                        @Override
                        public void onSubmit(String password) {

                        }
                    });
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

    }

}
