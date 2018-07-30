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
import com.hipla.smartoffice_new.databinding.FragmentIndboxBinding;
import com.hipla.smartoffice_new.dialogs.CreateMessageDialogFragment;
import com.hipla.smartoffice_new.dialogs.Dialogs;

/**
 * A simple {@link Fragment} subclass.
 */
public class IndboxMessagesFragment extends BlankFragment implements CreateMessageDialogFragment.OnDialogEvent {

    private FragmentIndboxBinding binding;
    private IndboxFragment indboxFragment;
    private OutboxFragment outboxFragment;
    private MyPagerAdapter myPagerAdapter;
    private int selectedItem = 0, selectedItemOutbox=0;
    private Dialog dialog;
    private NetworkChangeReceiver mNetworkReceiver;

    public IndboxMessagesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_indbox, container, false);
        initView();
        return binding.getRoot();
    }

    private void initView() {

        mNetworkReceiver = new NetworkChangeReceiver();

        indboxFragment = new IndboxFragment();
        outboxFragment = new OutboxFragment();

        myPagerAdapter = new MyPagerAdapter(getChildFragmentManager());
        binding.vpMessages.setAdapter(myPagerAdapter);

        binding.vpMessages.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if(position==0){
                    binding.line1.setVisibility(View.VISIBLE);
                    binding.tvIndbox.setTextColor(getResources().getColor(R.color.text_blue));

                    binding.line2.setVisibility(View.GONE);
                    binding.tvOutbox.setTextColor(getResources().getColor(R.color.text_white));
                }else if(position==1){
                    binding.line1.setVisibility(View.GONE);
                    binding.tvIndbox.setTextColor(getResources().getColor(R.color.text_white));

                    binding.line2.setVisibility(View.VISIBLE);
                    binding.tvOutbox.setTextColor(getResources().getColor(R.color.text_blue));
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        binding.tvIndbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.vpMessages.setCurrentItem(0);
            }
        });

        binding.tvOutbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.vpMessages.setCurrentItem(1);
            }
        });

        binding.ivWriteMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createMessage();
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
                    if(binding.vpMessages.getCurrentItem()==0){
                        indboxFragment.filterMessageByName(binding.etSpotlightSerch.getText().toString().trim());
                    }else{
                        outboxFragment.filterMessageByName(binding.etSpotlightSerch.getText().toString().trim());
                    }
                }else{
                    if(binding.vpMessages.getCurrentItem()==0){
                        indboxFragment.filterMessageByName("");
                    }else{
                        outboxFragment.filterMessageByName("");
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

                if(binding.vpMessages.getCurrentItem()==0){
                    indboxFragment.filterMessageByName("");
                }else{
                    outboxFragment.filterMessageByName("");
                }

            }
        });

    }

    private void createMessage() {
        CreateMessageDialogFragment mDialog = new CreateMessageDialogFragment();
        mDialog.setOnDismissClickListener(IndboxMessagesFragment.this);
        mDialog.show(getChildFragmentManager(), "dialog");
    }

    @Override
    public void onDismissListener() {

    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int pos) {
            switch (pos) {
                case 0:
                    return indboxFragment;
                case 1:
                    return outboxFragment;
                default:
                    return indboxFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public void onShowPopupWindow(View v) {
        final PopupWindow popup = new PopupWindow(getActivity());
        View layout = getLayoutInflater().inflate(R.layout.menu_popup_window, null);
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
        TextView tv_unread = layout.findViewById(R.id.tv_unread);
        TextView tv_read = layout.findViewById(R.id.tv_read);

        if(binding.vpMessages.getCurrentItem()==0) {
            if (selectedItem == 0) {
                tv_all.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItem == 1) {
                tv_read.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItem == 2) {
                tv_unread.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItem == 3) {
                //tv_create_messgae.setTextColor(getResources().getColor(R.color.text_blue));
            }
        }else{
            if (selectedItemOutbox == 0) {
                tv_all.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItemOutbox == 1) {
                tv_read.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItemOutbox == 2) {
                tv_unread.setTextColor(getResources().getColor(R.color.text_blue));
            } else if (selectedItemOutbox == 3) {
                //tv_create_messgae.setTextColor(getResources().getColor(R.color.text_blue));
            }
        }

       /* tv_create_messgae.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //selectedItem = 3;
                createMessage();
                popup.dismiss();
            }
        });*/

        tv_all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();

                if(binding.vpMessages.getCurrentItem()==0) {
                    if (indboxFragment != null) {
                        selectedItem = 0;
                        indboxFragment.filterMessage("");
                    }
                }else{
                    if (outboxFragment != null) {
                        selectedItemOutbox = 0;
                        outboxFragment.filterMessage("");
                    }
                }
            }
        });

        tv_read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();

                if(binding.vpMessages.getCurrentItem()==0) {
                    if (indboxFragment != null) {
                        selectedItem = 1;
                        indboxFragment.filterMessage("read");
                    }
                }else{
                    if (outboxFragment != null) {
                        selectedItemOutbox=1;
                        outboxFragment.filterMessage("read");
                    }
                }
            }
        });

        tv_unread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();

                if(binding.vpMessages.getCurrentItem()==0) {
                    if (indboxFragment != null) {
                        selectedItem = 2;
                        indboxFragment.filterMessage("unread");
                    }
                }else{
                    if (outboxFragment != null) {
                        selectedItemOutbox=2;
                        outboxFragment.filterMessage("unread");
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if ((DashboardActivity) getActivity() != null) {

            ((DashboardActivity) getActivity()).registerReceiver(mNetworkReceiver,
                    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }

        try {
            if (getActivity()!=null && ((DashboardActivity)getActivity()).isDataAvailable()) {
                if(dialog!=null){
                    dialog.dismiss();
                }

                if(indboxFragment!=null){
                    indboxFragment.getAllReview();
                }

                if(outboxFragment!=null){
                    outboxFragment.getAllReview();
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

                    if(indboxFragment!=null){
                        indboxFragment.getAllReview();
                    }

                    if(outboxFragment!=null){
                        outboxFragment.getAllReview();
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
