package com.hipla.smartoffice_new.fragment;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.activity.DashboardActivity;
import com.hipla.smartoffice_new.adapter.NotificationAdapter;
import com.hipla.smartoffice_new.database.Db_Helper;
import com.hipla.smartoffice_new.databinding.FragmentNotificationBinding;
import com.hipla.smartoffice_new.model.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment {

    private FragmentNotificationBinding binding;
    private NotificationAdapter mAdapter;
    private Db_Helper db_helper;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false);

        initView();
        return binding.getRoot();
    }

    private void initView() {

        binding.rvNotifications.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new NotificationAdapter(getActivity(), new ArrayList<Notification>());
        binding.rvNotifications.setAdapter(mAdapter);

        db_helper = new Db_Helper(getActivity());
        List<Notification> notificationList = db_helper.getAllNotification();

        if(notificationList != null){
            mAdapter.notifyDataChange(notificationList);
        }

        binding.tvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(db_helper!=null){
                    db_helper.deleteNotificationInfo();
                    mAdapter.notifyDataChange(new ArrayList<Notification>());
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if((DashboardActivity)getActivity()!=null){
            ((DashboardActivity)getActivity()).setNotificationIcon();
        }
    }
}
