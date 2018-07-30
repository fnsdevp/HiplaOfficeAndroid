package com.hipla.smartoffice_new.fragment;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.activity.DashboardActivity;
import com.hipla.smartoffice_new.databinding.FragmentScheduleMeetingBinding;
import com.hipla.smartoffice_new.dialogs.Dialogs;

/**
 * A simple {@link Fragment} subclass.
 */
public class ScheduleMeetingFragment extends BlankFragment {

    private FragmentScheduleMeetingBinding binding;
    private FixedMeetingFragment fixedMeetingFragment;
    private FlexibleMeetingFragment flexibleMeetingFragment;
    private WebExMeetingFragment webexMeetingFragment;
    private MyPagerAdapter myPagerAdapter;
    private Dialog dialog;
    private NetworkChangeReceiver mNetworkReceiver;

    public ScheduleMeetingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule_meeting, container, false);
        binding.setFragment(ScheduleMeetingFragment.this);

        initView();
        return binding.getRoot();
    }

    private void initView() {

        mNetworkReceiver = new NetworkChangeReceiver();

        fixedMeetingFragment = new FixedMeetingFragment();

        flexibleMeetingFragment = new FlexibleMeetingFragment();

        webexMeetingFragment = new WebExMeetingFragment();

        myPagerAdapter = new MyPagerAdapter(getChildFragmentManager());
        binding.vpMeetings.setAdapter(myPagerAdapter);

        binding.vpMeetings.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    binding.line1.setVisibility(View.VISIBLE);
                    binding.tvFixedMeetings.setTextColor(getResources().getColor(R.color.text_blue));

                    binding.line2.setVisibility(View.GONE);
                    binding.tvFlexibleMeetings.setTextColor(getResources().getColor(R.color.text_white));

                    binding.line3.setVisibility(View.GONE);
                    binding.tvWebexMeetings.setTextColor(getResources().getColor(R.color.text_white));
                } else if (position == 1) {
                    binding.line1.setVisibility(View.GONE);
                    binding.tvFixedMeetings.setTextColor(getResources().getColor(R.color.text_white));

                    binding.line2.setVisibility(View.VISIBLE);
                    binding.tvFlexibleMeetings.setTextColor(getResources().getColor(R.color.text_blue));

                    binding.line3.setVisibility(View.GONE);
                    binding.tvWebexMeetings.setTextColor(getResources().getColor(R.color.text_white));
                } else if (position == 2) {
                    binding.line1.setVisibility(View.GONE);
                    binding.tvFixedMeetings.setTextColor(getResources().getColor(R.color.text_white));

                    binding.line2.setVisibility(View.GONE);
                    binding.tvFlexibleMeetings.setTextColor(getResources().getColor(R.color.text_white));

                    binding.line3.setVisibility(View.VISIBLE);
                    binding.tvWebexMeetings.setTextColor(getResources().getColor(R.color.text_blue));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    public void setFixedMeeting() {
        binding.vpMeetings.setCurrentItem(0);
    }

    public void setFlexibleMeeting() {
        binding.vpMeetings.setCurrentItem(1);
    }

    public void setWebexMeeting() {
        binding.vpMeetings.setCurrentItem(2);
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int pos) {
            switch (pos) {
                case 0:
                    return fixedMeetingFragment;
                case 1:
                    return flexibleMeetingFragment;
                case 2:
                    return webexMeetingFragment;
                default:
                    return fixedMeetingFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if((DashboardActivity)getActivity()!=null){
            ((DashboardActivity)getActivity()).scheduleMeetingIcon();

            ((DashboardActivity) getActivity()).registerReceiver(mNetworkReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        try {
            if (getActivity()!=null && ((DashboardActivity)getActivity()).isDataAvailable()) {
                if(dialog!=null){
                    dialog.dismiss();
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
