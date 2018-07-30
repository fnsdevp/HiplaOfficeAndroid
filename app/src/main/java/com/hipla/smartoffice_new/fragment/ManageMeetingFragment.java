package com.hipla.smartoffice_new.fragment;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.activity.DashboardActivity;
import com.hipla.smartoffice_new.databinding.FragmentManageMeetingBinding;
import com.hipla.smartoffice_new.dialogs.Dialogs;

/**
 * A simple {@link Fragment} subclass.
 */
public class ManageMeetingFragment extends Fragment {

    private FragmentManageMeetingBinding binding;
    private MyAppointmentsFragment mMyAppointmentsFragment;
    private RequestedMeetingsFragment mRequestedMeetingsFragment;
    private GroupMeetingsFragment mGroupMeetingFragment;
    private int selectedItem = 0, selectedItemOutbox=0, selectedItemGroupMeeting=0;
    private MyPagerAdapter myPagerAdapter;
    private Dialog dialog;
    private NetworkChangeReceiver mNetworkReceiver;

    public ManageMeetingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_manage_meeting, container, false);
        binding.setFragment(ManageMeetingFragment.this);
        initView();
        return binding.getRoot();
    }

    private void initView() {
        mNetworkReceiver = new NetworkChangeReceiver();

        mMyAppointmentsFragment = new MyAppointmentsFragment();
        mRequestedMeetingsFragment = new RequestedMeetingsFragment();
        mGroupMeetingFragment = new GroupMeetingsFragment();
        myPagerAdapter = new MyPagerAdapter(getChildFragmentManager());

        binding.viewPagerManageMeetings.setAdapter(myPagerAdapter);

        binding.viewPagerManageMeetings.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position==0){
                    binding.line1.setVisibility(View.VISIBLE);
                    binding.tvMyappointment.setTextColor(getResources().getColor(R.color.text_blue));

                    binding.line2.setVisibility(View.GONE);
                    binding.tvRequestReceived.setTextColor(getResources().getColor(R.color.text_white));

                    binding.line3.setVisibility(View.GONE);
                    binding.tvGroupMeeting.setTextColor(getResources().getColor(R.color.text_white));
                }else if(position==1){
                    binding.line1.setVisibility(View.GONE);
                    binding.tvMyappointment.setTextColor(getResources().getColor(R.color.text_white));

                    binding.line2.setVisibility(View.VISIBLE);
                    binding.tvRequestReceived.setTextColor(getResources().getColor(R.color.text_blue));

                    binding.line3.setVisibility(View.GONE);
                    binding.tvGroupMeeting.setTextColor(getResources().getColor(R.color.text_white));
                }else if(position==2){
                    binding.line1.setVisibility(View.GONE);
                    binding.tvMyappointment.setTextColor(getResources().getColor(R.color.text_white));

                    binding.line2.setVisibility(View.GONE);
                    binding.tvRequestReceived.setTextColor(getResources().getColor(R.color.text_white));

                    binding.line3.setVisibility(View.VISIBLE);
                    binding.tvGroupMeeting.setTextColor(getResources().getColor(R.color.text_blue));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        binding.viewPagerManageMeetings.setOffscreenPageLimit(3);

        binding.tvMyappointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.viewPagerManageMeetings.setCurrentItem(0);
            }
        });

        binding.tvRequestReceived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.viewPagerManageMeetings.setCurrentItem(1);
            }
        });

        binding.tvGroupMeeting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.viewPagerManageMeetings.setCurrentItem(2);
            }
        });

        binding.tvMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShowPopupWindow(v);
            }
        });

        binding.etSpotlightSerch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(! binding.etSpotlightSerch.getText().toString().trim().isEmpty()){
                    if(binding.viewPagerManageMeetings.getCurrentItem()==0){
                        mMyAppointmentsFragment.filterByName(binding.etSpotlightSerch.getText().toString().trim());
                    }else{
                        mRequestedMeetingsFragment.filterByName(binding.etSpotlightSerch.getText().toString().trim());
                    }
                }else{
                    if(binding.viewPagerManageMeetings.getCurrentItem()==0){
                        mMyAppointmentsFragment.filterByName("");
                    }else{
                        mRequestedMeetingsFragment.filterByName("");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.tvcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.etSpotlightSerch.setText("");

                if(binding.viewPagerManageMeetings.getCurrentItem()==0){
                    mMyAppointmentsFragment.filterByName("");
                }else if(binding.viewPagerManageMeetings.getCurrentItem()==1){
                    mRequestedMeetingsFragment.filterByName("");
                }else if(binding.viewPagerManageMeetings.getCurrentItem()==2){
                    mGroupMeetingFragment.filterByName("");
                }
            }
        });
    }

    public void selectMyAppointment(){

    }

    public void selectRequestReceived(){

    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int pos) {
            switch (pos) {
                case 0:
                    return mMyAppointmentsFragment;
                case 1:
                    return mRequestedMeetingsFragment;
                case 2:
                    return mGroupMeetingFragment;
                default:
                    return mMyAppointmentsFragment;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    public void onShowPopupWindow(View v) {
        final PopupWindow popup = new PopupWindow(getActivity());
        View layout = getLayoutInflater().inflate(R.layout.meeting_popup_window, null);
        popup.setContentView(layout);
        // Set content width and height
        popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        // Closes the popup window when touch outside of it - when looses focus
        popup.setOutsideTouchable(true);
        popup.setFocusable(true);
        // Show anchored to button
        popup.setBackgroundDrawable(new BitmapDrawable());
        popup.showAsDropDown(v);

        TextView tv_all = layout.findViewById(R.id.tv_all);
        TextView tv_pending = layout.findViewById(R.id.tv_pending);
        TextView tv_confirm = layout.findViewById(R.id.tv_confirm);

        if(binding.viewPagerManageMeetings.getCurrentItem()==0) {
            if (selectedItem == 0) {
                tv_all.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItem == 1) {
                tv_pending.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItem == 2) {
                tv_confirm.setTextColor(getResources().getColor(R.color.text_blue));
            }
        }else if(binding.viewPagerManageMeetings.getCurrentItem()==1){
            if (selectedItemOutbox == 0) {
                tv_all.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItemOutbox == 1) {
                tv_pending.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItemOutbox == 2) {
                tv_confirm.setTextColor(getResources().getColor(R.color.text_blue));
            }
        }else if(binding.viewPagerManageMeetings.getCurrentItem()==2){
            if (selectedItemGroupMeeting == 0) {
                tv_all.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItemGroupMeeting == 1) {
                tv_pending.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItemGroupMeeting == 2) {
                tv_confirm.setTextColor(getResources().getColor(R.color.text_blue));
            }
        }

        tv_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();

                if(binding.viewPagerManageMeetings.getCurrentItem()==0) {
                    if (mMyAppointmentsFragment != null) {
                        selectedItem = 0;
                        mMyAppointmentsFragment.filterMessage("");
                    }
                }else if(binding.viewPagerManageMeetings.getCurrentItem()==1){
                    if (mRequestedMeetingsFragment != null) {
                        selectedItemOutbox = 0;
                        mRequestedMeetingsFragment.filterMessage("");
                    }
                }else if(binding.viewPagerManageMeetings.getCurrentItem()==2){
                    if (mGroupMeetingFragment != null) {
                        selectedItemGroupMeeting = 0;
                        mGroupMeetingFragment.filterMessage("");
                    }
                }
            }
        });

        tv_pending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();

                if(binding.viewPagerManageMeetings.getCurrentItem()==0) {
                    if (mMyAppointmentsFragment != null) {
                        selectedItem = 1;
                        mMyAppointmentsFragment.filterMessage("pending");
                    }
                }else if(binding.viewPagerManageMeetings.getCurrentItem()==1){
                    if (mRequestedMeetingsFragment != null) {
                        selectedItemOutbox=1;
                        mRequestedMeetingsFragment.filterMessage("pending");
                    }
                }else if(binding.viewPagerManageMeetings.getCurrentItem()==2){
                    if (mGroupMeetingFragment != null) {
                        selectedItemGroupMeeting=1;
                        mGroupMeetingFragment.filterMessage("pending");
                    }
                }
            }
        });

        tv_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();

                if(binding.viewPagerManageMeetings.getCurrentItem()==0) {
                    if (mMyAppointmentsFragment != null) {
                        selectedItem = 2;
                        mMyAppointmentsFragment.filterMessage("confirm");
                    }
                }else if(binding.viewPagerManageMeetings.getCurrentItem()==1){
                    if (mRequestedMeetingsFragment != null) {
                        selectedItemOutbox=2;
                        mRequestedMeetingsFragment.filterMessage("confirm");
                    }
                }else if(binding.viewPagerManageMeetings.getCurrentItem()==2){
                    if (mGroupMeetingFragment != null) {
                        selectedItemGroupMeeting=2;
                        mGroupMeetingFragment.filterMessage("confirm");
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if((DashboardActivity)getActivity()!=null){
            ((DashboardActivity)getActivity()).setManageMeetingIcon();

            ((DashboardActivity) getActivity()).registerReceiver(mNetworkReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        try {
            if (getActivity()!=null && ((DashboardActivity)getActivity()).isDataAvailable()) {
                if(dialog!=null){
                    dialog.dismiss();
                }

                if(mMyAppointmentsFragment!=null){
                    mMyAppointmentsFragment.getMyAppointment();
                }

                if(mRequestedMeetingsFragment!=null){
                    mRequestedMeetingsFragment.getMyAppointment();
                }

                if(mRequestedMeetingsFragment!=null){
                    mGroupMeetingFragment.getGroupAppointment();
                }

            } else if(!Dialogs.isDialogShowing){
                dialog = Dialogs.dialogNoConnection(((DashboardActivity)getActivity()), new Dialogs.OnCallback() {
                    @Override
                    public void onSubmit(String password) {

                    }
                });
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        if (mNetworkReceiver != null) {
            ((DashboardActivity) getActivity()).unregisterReceiver(mNetworkReceiver);
        }
    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (getActivity()!=null && ((DashboardActivity)getActivity()).isDataAvailable()) {
                    if(dialog!=null){
                        dialog.dismiss();
                    }

                    if(mMyAppointmentsFragment!=null){
                        mMyAppointmentsFragment.getMyAppointment();
                    }

                    if(mRequestedMeetingsFragment!=null){
                        mRequestedMeetingsFragment.getMyAppointment();
                    }

                    if(mRequestedMeetingsFragment!=null){
                        mGroupMeetingFragment.getGroupAppointment();
                    }

                } else if(!Dialogs.isDialogShowing){
                    dialog = Dialogs.dialogNoConnection(((DashboardActivity)getActivity()), new Dialogs.OnCallback() {
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
