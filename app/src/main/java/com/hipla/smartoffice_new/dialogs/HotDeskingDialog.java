package com.hipla.smartoffice_new.dialogs;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.adapter.HotDeskZonesAdapter;
import com.hipla.smartoffice_new.database.Db_Helper;
import com.hipla.smartoffice_new.databinding.FragmentHotDeskingBinding;
import com.hipla.smartoffice_new.fragment.NavigineMapDialogNew;
import com.hipla.smartoffice_new.model.HotDeskObject;
import com.hipla.smartoffice_new.model.HotDeskZone;
import com.hipla.smartoffice_new.model.HotSeatData;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.utils.CONST;
import com.hipla.smartoffice_new.utils.ColorPicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import io.paperdb.Paper;

public class HotDeskingDialog extends DialogFragment implements ColorPicker.OnColorChangedListener, StringRequestListener, HotDeskZonesAdapter.SelectContactToInvite {

    private ColorPicker colorPickerView;
    private OnColorSelectedListener onColorSelectedListener;
    private View view;
    private FragmentHotDeskingBinding binding;
    private ProgressDialog mDialog;
    private OnDialogEvent mListener;
    private int zoneSelected = -1;
    private int selectedSeat = -1;
    private String hourForHotDesking = "";
    private String hotDeskZoneName = "", selectedSeatName = "";
    private List<HotSeatData> hotSeatDataList = new ArrayList<>();
    private boolean isPasswordVisible = true;
    private List<TextView> seatViews = new ArrayList<>();

    public HotDeskingDialog() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getActivity() != null)
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_hot_desking, container, false);
        initView(binding.getRoot());
        return binding.getRoot();
    }

    private void initView(View mView) {
        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage(getResources().getString(R.string.please_wait));

        seatViews.add(binding.ivSeat1);
        seatViews.add(binding.ivSeat2);
        seatViews.add(binding.ivSeat3);
        seatViews.add(binding.ivSeat4);
        seatViews.add(binding.ivSeat5);
        seatViews.add(binding.ivSeat6);
        seatViews.add(binding.ivSeat7);
        seatViews.add(binding.ivSeat8);

        if (Paper.book().read(CONST.HOT_DESK_DATA) != null) {
            HotDeskObject object = Paper.book().read(CONST.HOT_DESK_DATA);

            selectedSeat = object.getSeatId();
            zoneSelected = object.getHotZoneId();
            selectedSeatName = object.getSeatName();
            hotDeskZoneName = object.getHotZoneName();

            binding.tvIsePassword.setText(object.getWifiPassword());
            binding.tvIseUsername.setText(String.format(getString(R.string.wifi_username_format), object.getWifiUserName()));

            binding.llNext.setVisibility(View.INVISIBLE);
            binding.llBtns.setVisibility(View.VISIBLE);
            binding.llDeliveryTime.setVisibility(View.INVISIBLE);
            binding.rlUsername.setVisibility(View.VISIBLE);
        } else {
            binding.llNext.setVisibility(View.VISIBLE);
            binding.llBtns.setVisibility(View.INVISIBLE);
            binding.llDeliveryTime.setVisibility(View.VISIBLE);
            binding.rlUsername.setVisibility(View.INVISIBLE);
        }

        // Spinner click listener
        binding.etTimeOfBooking.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hotDeskZoneName = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        for (int i = 1; i < 11; i++) {
            categories.add("" + i);
        }

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        binding.etTimeOfBooking.setAdapter(dataAdapter);

        binding.btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        binding.btnReleaseDesk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                releaseSeat();
            }
        });

        binding.btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedSeat == -1) {
                    Toast.makeText(getActivity(), getString(R.string.select_seat_to_proceed), Toast.LENGTH_SHORT).show();
                } else if (hotDeskZoneName.isEmpty()) {
                    Toast.makeText(getActivity(), getString(R.string.enter_time_duration), Toast.LENGTH_SHORT).show();
                } else {
                    bookHotDesk(zoneSelected, selectedSeat, hotDeskZoneName);
                }

            }
        });

        binding.btnNavigate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HotDeskObject object = Paper.book().read(CONST.HOT_DESK_DATA);

                NavigineMapDialogNew mapDialog = new NavigineMapDialogNew();
                Bundle bundle = new Bundle();
                if (object.getHotZoneId() == 1) {
                    bundle.putString(CONST.POINTX, "31.12");
                    bundle.putString(CONST.POINTY, "14.46");
                } else if (object.getHotZoneId() == 2) {
                    bundle.putString(CONST.POINTX, "34.32");
                    bundle.putString(CONST.POINTY, "14.60");
                }
                mapDialog.setArguments(bundle);
                if (mapDialog != null && mapDialog.getDialog() != null
                        && mapDialog.getDialog().isShowing()) {
                    //dialog is showing so do something
                } else {
                    //dialog is not showing
                    mapDialog.show(getChildFragmentManager(), "mapDialog");
                }
            }
        });

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

    }

    private void setSeatClickListener(int seatId) {

        for (int i = 0; i < hotSeatDataList.size(); i++) {
            if ((i + 1) != seatId) {

                if (!hotSeatDataList.get(i).getStatus().equalsIgnoreCase("booked")) {
                    seatViews.get(i).setBackgroundResource(R.drawable.ic_seat_empthy);
                } else {
                    seatViews.get(i).setBackgroundResource(R.drawable.ic_seat_booked);
                }

            } else {
                seatViews.get(i).setBackgroundResource(R.drawable.ic_seat_select);
            }
        }
    }

    private void setUpHotDeskData(final List<HotSeatData> hotSeatDataList) {
        /*binding.tvSeat11.setText(zoneSelected+hotSeatDataList.get(0).getRow_name());
        binding.tvSeat21.setText(zoneSelected+hotSeatDataList.get(1).getRow_name());
        binding.tvSeat31.setText(zoneSelected+hotSeatDataList.get(2).getRow_name());
        binding.tvSeat41.setText(zoneSelected+hotSeatDataList.get(3).getRow_name());*/

        for (int i = 0; i < seatViews.size(); i++) {
            final int finalI = i;

            seatViews.get(i).setText(hotSeatDataList.get(i).getZone_id() + hotSeatDataList.get(i).getRow_name());

            seatViews.get(i).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Paper.book().read(CONST.HOT_DESK_DATA) == null) {
                        if (!hotSeatDataList.get(finalI).getStatus().equalsIgnoreCase("booked")) {

                            selectedSeat = hotSeatDataList.get(finalI).getId();
                            zoneSelected = hotSeatDataList.get(finalI).getZone_id();
                            setSeatClickListener(finalI + 1);
                        }
                    }
                }
            });
        }

        for (int i = 0; i < seatViews.size(); i++) {
            if (!hotSeatDataList.get(i).getStatus().equalsIgnoreCase("booked")) {
                seatViews.get(i).setBackgroundResource(R.drawable.ic_seat_empthy);
            } else {
                if (zoneSelected == hotSeatDataList.get(i).getZone_id() && selectedSeat == hotSeatDataList.get(i).getId()) {
                    seatViews.get(i).setBackgroundResource(R.drawable.ic_seat_select);
                } else {
                    seatViews.get(i).setBackgroundResource(R.drawable.ic_seat_booked);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getHotDesking();
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
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        try {

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (mListener != null) {
            mListener.onDismissListener();
        }
    }

    @Override
    public void selectZone(HotDeskZone roomData) {
        zoneSelected = roomData.getId();
        hotDeskZoneName = roomData.getZone_name();
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
                case "hotZones":

                    JSONObject jsonObject = new JSONObject(result);
                    if (jsonObject.optString("status").equalsIgnoreCase("success")) {

                        GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();

                        HotDeskZone[] hotDeskZones = gson.fromJson(jsonObject.getJSONArray("zone_list").toString(), HotDeskZone[].class);
                        List<HotDeskZone> hotDeskZoneList = Arrays.asList(hotDeskZones);

                    } else {
                        dismiss();
                    }

                    break;

                case "hotZoneSeat":

                    JSONObject zoneSeatJsonObject = new JSONObject(result);
                    if (zoneSeatJsonObject.optString("status").equalsIgnoreCase("success")) {

                        GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();

                        HotDeskZone[] hotDeskZones = gson.fromJson(zoneSeatJsonObject.getJSONArray("zone_list").toString(), HotDeskZone[].class);
                        List<HotDeskZone> hotDeskZoneList = Arrays.asList(hotDeskZones);
                        for (HotDeskZone data :
                                hotDeskZoneList) {
                            if (data.getRow_list() != null)
                                hotSeatDataList.addAll(Arrays.asList(data.getRow_list()));
                        }

                        binding.rl1.setVisibility(View.VISIBLE);
                        binding.rl2.setVisibility(View.VISIBLE);

                        setUpHotDeskData(hotSeatDataList);

                        if (Paper.book().read(CONST.HOT_DESK_DATA) == null)
                            getSeatStatus();

                    } else {
                        dismiss();
                    }

                    break;

                case "getSeatStatus":

                    JSONObject seatObj = new JSONObject(result);
                    if (seatObj.optString("status").equalsIgnoreCase("success")) {

                        JSONObject jsonObject1 = seatObj.getJSONArray("row_list").getJSONObject(0);

                        zoneSelected = jsonObject1.optInt("zone_id");
                        selectedSeat = jsonObject1.optInt("row_id");
                        hotDeskZoneName = jsonObject1.optString("zone_name");
                        selectedSeatName = jsonObject1.optString("row_name");

                        binding.llNext.setVisibility(View.INVISIBLE);
                        binding.llBtns.setVisibility(View.VISIBLE);
                        binding.llDeliveryTime.setVisibility(View.INVISIBLE);
                        binding.rlUsername.setVisibility(View.VISIBLE);

                        Db_Helper db_helper = new Db_Helper(getActivity());

                        Random random = new Random();
                        int value = random.nextInt(9) + 1;

                        binding.tvIsePassword.setText(db_helper.getWiFiPassword(value));
                        binding.tvIseUsername.setText(String.format(getString(R.string.wifi_username_format), db_helper.getWiFiUserName(value)));

                        Paper.book().write(CONST.HOT_DESK_DATA,
                                new HotDeskObject(zoneSelected, selectedSeat, jsonObject1.optInt("id", 0),
                                        hotDeskZoneName, selectedSeatName, db_helper.getWiFiUserName(value), db_helper.getWiFiPassword(value)));

                        for (int i = 0; i < seatViews.size(); i++) {
                            if (!hotSeatDataList.get(i).getStatus().equalsIgnoreCase("booked")) {
                                seatViews.get(i).setBackgroundResource(R.drawable.ic_seat_empthy);
                            } else {
                                if (zoneSelected == hotSeatDataList.get(i).getZone_id() && selectedSeat == hotSeatDataList.get(i).getId()) {
                                    seatViews.get(i).setBackgroundResource(R.drawable.ic_seat_select);
                                } else {
                                    seatViews.get(i).setBackgroundResource(R.drawable.ic_seat_booked);
                                }
                            }
                        }

                    } else {
                        //dismiss();
                    }

                    break;

                case "bookHotSeat":
                    JSONObject jsonObject1 = new JSONObject(result);
                    if (jsonObject1.optString("status").equalsIgnoreCase("success")) {

                        binding.llNext.setVisibility(View.INVISIBLE);
                        binding.llBtns.setVisibility(View.VISIBLE);
                        binding.llDeliveryTime.setVisibility(View.INVISIBLE);
                        binding.rlUsername.setVisibility(View.VISIBLE);

                        Db_Helper db_helper = new Db_Helper(getActivity());

                        Random random = new Random();
                        int value = random.nextInt(9) + 1;

                        binding.tvIsePassword.setText(db_helper.getWiFiPassword(value));
                        binding.tvIseUsername.setText(String.format(getString(R.string.wifi_username_format), db_helper.getWiFiUserName(value)));

                        Paper.book().write(CONST.HOT_DESK_DATA,
                                new HotDeskObject(zoneSelected, selectedSeat, jsonObject1.optInt("hot_seat_id", 0),
                                        hotDeskZoneName, selectedSeatName, db_helper.getWiFiUserName(value), db_helper.getWiFiPassword(value)));

                    } else {
                        Toast.makeText(getActivity(), jsonObject1.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;

                case "changeSeatStatus":
                    JSONObject jsonObject2 = new JSONObject(result);
                    if (jsonObject2.optString("status").equalsIgnoreCase("success")) {

                        binding.llNext.setVisibility(View.VISIBLE);
                        binding.llBtns.setVisibility(View.INVISIBLE);
                        binding.llDeliveryTime.setVisibility(View.VISIBLE);
                        binding.rlUsername.setVisibility(View.INVISIBLE);

                        zoneSelected = -1;
                        selectedSeat = -1;
                        selectedSeatName = "";
                        hotDeskZoneName = "";

                        Paper.book().delete(CONST.HOT_DESK_DATA);

                        Toast.makeText(getActivity(), getString(R.string.thank_you), Toast.LENGTH_SHORT).show();
                        dismiss();

                    } else {
                        Toast.makeText(getActivity(), jsonObject2.optString("message"), Toast.LENGTH_SHORT).show();
                    }

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
        void onColorSelected(int color);
    }

    private void getHotDesking() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {

                if (mDialog.isShowing()) {
                    mDialog.show();
                }

                HashMap<String, String> requestParameter = new HashMap<>();

                /*new PostStringRequest(getActivity(), requestParameter,
                        HotDeskingDialog.this, "hotZones",
                        NetworkUtility.BASEURL + NetworkUtility.GET_ZONE_LIST);*/

                new PostStringRequest(getActivity(), requestParameter,
                        HotDeskingDialog.this, "hotZoneSeat",
                        NetworkUtility.BASEURL + NetworkUtility.GET_ZONE_SEAT_LIST);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void getSeatStatus() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                if (!mDialog.isShowing()) {
                    mDialog.show();
                }

                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("user_id", "" + userData.getId());

                new PostStringRequest(getActivity(), requestParameter,
                        HotDeskingDialog.this, "getSeatStatus",
                        NetworkUtility.BASEURL + NetworkUtility.GET_LAST_BOOKED_SEAT);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void bookHotDesk(int zoneSelected, int seatSelected, String time) {
        try {

            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                if (!mDialog.isShowing()) {
                    mDialog.show();
                }

                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("zone_id", "" + zoneSelected);
                requestParameter.put("row_id", "" + seatSelected);
                requestParameter.put("time_duration", "" + time);
                requestParameter.put("user_id", "" + userData.getId());

                new PostStringRequest(getActivity(), requestParameter,
                        HotDeskingDialog.this, "bookHotSeat",
                        NetworkUtility.BASEURL + NetworkUtility.BOOK_HOT_DESKING);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void releaseSeat() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
            HotDeskObject object = Paper.book().read(CONST.HOT_DESK_DATA);

            if (userData != null && object != null) {
                if (!mDialog.isShowing()) {
                    mDialog.show();
                }

                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("hot_seat_id", "" + object.getHot_seat_id());
                requestParameter.put("status", "2");

                new PostStringRequest(getActivity(), requestParameter,
                        HotDeskingDialog.this, "changeSeatStatus",
                        NetworkUtility.BASEURL + NetworkUtility.FREE_HOT_DESKING);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
