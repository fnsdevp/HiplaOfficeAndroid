package com.hipla.smartoffice_new.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.adapter.OutboxMessageListAdapter;
import com.hipla.smartoffice_new.databinding.FragmentOutboxBinding;
import com.hipla.smartoffice_new.dialogs.OutboxReadDialogFragment;
import com.hipla.smartoffice_new.model.OutboxMessages;
import com.hipla.smartoffice_new.model.UserData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class OutboxFragment extends BlankFragment implements StringRequestListener, OutboxMessageListAdapter.OnMessageClick {

    private FragmentOutboxBinding binding;
    private ProgressDialog mDialog;
    private OutboxMessageListAdapter mAdapter;

    public OutboxFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_outbox, container, false);
        initView();
        return binding.getRoot();
    }

    private void initView() {
        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage(getResources().getString(R.string.please_wait));

        mAdapter = new OutboxMessageListAdapter(new ArrayList<OutboxMessages>(), getActivity());
        mAdapter.setOnMessageClick(this);
        binding.lvOutboxMessages.setAdapter(mAdapter);
        binding.pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAllReview();
            }
        });

        getAllReview();
    }

    public void getAllReview() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));

                new PostStringRequest(getActivity(), requestParameter, OutboxFragment.this, "getAllMessages",
                        NetworkUtility.BASEURL + NetworkUtility.GET_ALL_MESSAGES);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        try{

            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            if (binding.pullToRefresh.isRefreshing()) {
                binding.pullToRefresh.setRefreshing(false);
            }

            switch (type){

                case "getAllMessages":

                    JSONObject responseObject = new JSONObject(result);
                    if(responseObject.optString("status").equalsIgnoreCase("success")){
                        if(responseObject.getJSONArray("outbox")!=null){

                            GsonBuilder builder = new GsonBuilder();
                            builder.setPrettyPrinting();
                            Gson gson = builder.create();

                            OutboxMessages[] outboxMessages = gson.fromJson(responseObject.getJSONArray("outbox").toString(), OutboxMessages[].class);
                            List<OutboxMessages> mData = Arrays.asList(outboxMessages);

                            if(mData.size()>0){
                                View footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.outbox_footer, null, false);
                                binding.lvOutboxMessages.addFooterView(footerView);
                            }

                            mAdapter.notifyAdpater(mData);
                        }
                    }

                    break;

            }

        }catch (Exception ex){
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

    @Override
    public void selectMessage(OutboxMessages review) {
        OutboxReadDialogFragment mDialog = new OutboxReadDialogFragment();
        mDialog.setOnDismissClickListener(new OutboxReadDialogFragment.OnDialogEvent() {
            @Override
            public void onDismissListener() {
                getAllReview();
            }
        });
        mDialog.setData(review);
        mDialog.show(getChildFragmentManager(), "dialog");
    }

    public void filterMessage(String status){
        if(mAdapter!=null){
            mAdapter.filter(status);
        }
    }

    public void filterMessageByName(String status){
        if(mAdapter!=null){
            mAdapter.filterByName(status);
        }
    }

}
