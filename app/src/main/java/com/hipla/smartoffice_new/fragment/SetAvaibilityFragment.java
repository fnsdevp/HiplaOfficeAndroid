package com.hipla.smartoffice_new.fragment;


import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.adapter.SetAvailibilityListAdapter;
import com.hipla.smartoffice_new.databinding.FragmentSetAvaibilityBinding;
import com.hipla.smartoffice_new.model.Availibility;
import com.hipla.smartoffice_new.model.UserData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class SetAvaibilityFragment extends Fragment implements StringRequestListener, SetAvailibilityListAdapter.OnCheckedClick {

    private FragmentSetAvaibilityBinding binding;
    private List<Availibility> mListData = new ArrayList<>();
    private SetAvailibilityListAdapter mAdapter;

    public SetAvaibilityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_set_avaibility, container, false);
        initView();
        return binding.getRoot();
    }

    private void initView() {

        getAllDate();
        mAdapter = new SetAvailibilityListAdapter(mListData, getActivity());
        mAdapter.setOnCheckedClick(this);
        binding.listAvailableDate.setAdapter(mAdapter);

        getAvailableTime();


    }

    private void getAvailableTime(){
        try{
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if(userData!=null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s",userData.getId()));

                new PostStringRequest(getActivity(), requestParameter, SetAvaibilityFragment.this, "getAvailableTime",
                        NetworkUtility.BASEURL + NetworkUtility.GET_AVAILABLE_TIME);
            }

        }catch (Exception ex){

        }
    }

    private void setAvailableTime(String date, String day, String from, String to, String status){
        try{
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if(userData!=null) {

                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s",userData.getId()));
                requestParameter.put("date", String.format("%s",date));
                requestParameter.put("day", String.format("%s",day));
                requestParameter.put("from", String.format("%s",from));
                requestParameter.put("to", String.format("%s",to));
                requestParameter.put("status", String.format("%s",status));

                new PostStringRequest(getActivity(), requestParameter, SetAvaibilityFragment.this, "setAvailableTime",
                        NetworkUtility.BASEURL + NetworkUtility.SET_AVAILABLE_TIME);
            }

        }catch (Exception ex){

        }
    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        try {

            switch (type){

                case "getAvailableTime":

                    JSONObject resJsonObject = new JSONObject(result);
                    if(resJsonObject.optString("status").equalsIgnoreCase("success") &&
                            resJsonObject.getJSONArray("timing")!=null && resJsonObject.getJSONArray("timing").length()!=0){

                        JSONArray resJsonArray = resJsonObject.getJSONArray("timing");

                        for (int i = 0; i < resJsonArray.length(); i++) {
                            if(resJsonArray.getJSONObject(i).optString("status")!=null &&
                                    resJsonArray.getJSONObject(i).optString("status").equalsIgnoreCase("Y")){
                                mListData.get(i).setFrom_time(resJsonArray.getJSONObject(i).optString("from"));
                                mListData.get(i).setTo_time(resJsonArray.getJSONObject(i).optString("to"));
                                mListData.get(i).setStatus(resJsonArray.getJSONObject(i).optString("status"));
                            }else{
                                mListData.get(i).setFrom_time("");
                                mListData.get(i).setTo_time("");
                                mListData.get(i).setStatus("N");
                            }
                        }

                        mAdapter.notifyAdpater(mListData);
                    }else{

                    }

                    break;

                case "setAvailableTime":

                    JSONObject jsonObject = new JSONObject(result);
                    if(jsonObject.optString("status").equalsIgnoreCase("success") &&
                            jsonObject.optString("message").equalsIgnoreCase("Sucessfully update time") ){
                        Toast.makeText(getActivity(), jsonObject.optString("message"), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(getActivity(), jsonObject.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    getAvailableTime();

                    break;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {

    }

    @Override
    public void onStarted() {

    }

    private void getAllDate(){

        Calendar c1 = Calendar.getInstance();
        int dayOfYear = c1.get(Calendar.DAY_OF_YEAR);

        for (int i = 0; i < 7; i++) {

            c1.set(Calendar.DAY_OF_YEAR, dayOfYear+i);

            Availibility data = new Availibility();
            data.setDate(new SimpleDateFormat("yyyy-MM-dd").format(c1.getTime()));
            data.setShowingDate(new SimpleDateFormat("dd MMM yyyy").format(c1.getTime()));
            data.setDay(new SimpleDateFormat("EEE").format(c1.getTime()));

            mListData.add(data);
        }

    }

    @Override
    public void selectTime(Availibility availibility, String status) {
        setAvailableTime(availibility.getDate(), availibility.getDay(), availibility.getFrom_time(),
                availibility.getTo_time(), status);
    }
}
