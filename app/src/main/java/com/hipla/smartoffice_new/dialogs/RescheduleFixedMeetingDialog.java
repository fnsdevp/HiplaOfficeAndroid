package com.hipla.smartoffice_new.dialogs;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hipla.smartoffice_new.fragment.SelectContactFragment;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.adapter.TimeSlotAdapter;
import com.hipla.smartoffice_new.adapter.WeekListAdapter;
import com.hipla.smartoffice_new.databinding.DialogFragmentFixedMeetingBinding;
import com.hipla.smartoffice_new.model.Appointments;
import com.hipla.smartoffice_new.model.ContactModel;
import com.hipla.smartoffice_new.model.RoomData;
import com.hipla.smartoffice_new.model.TimeSlot;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.utils.CONST;
import com.hipla.smartoffice_new.utils.InternetConnectionDetector;
import com.hipla.smartoffice_new.utils.SpacesItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.DecimalFormat;
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
public class RescheduleFixedMeetingDialog extends android.support.v4.app.DialogFragment implements SelectContactFragment.OnContactSelected, WeekListAdapter.OnDateClickListener, StringRequestListener, TimeSlotAdapter.OnTimeSlotClickListener, DatePickerDialog.OnDateSetListener {

    private static final int READ_CONTACT_PERMISSION = 101;
    private DialogFragmentFixedMeetingBinding binding;
    private WeekListAdapter mAdapter;
    private ContactModel selectedContact;
    private TimeSlotAdapter mTimeAdapter;
    private String selectedDates = "";
    private TimeSlot selectedStartTimeSlot, selectedEndTimeSlot;
    private ProgressDialog mDialog;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private Appointments appointmentsData;
    private OnDialogEvent mListener;

    public RescheduleFixedMeetingDialog() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_fixed_meeting, container, false);
        binding.setFragment(RescheduleFixedMeetingDialog.this);

        initView();
        return binding.getRoot();
    }

    private void initView() {

        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage(getResources().getString(R.string.please_wait));

        appointmentsData = Paper.book().read(CONST.APPOINTMENT_DATA);
        if (appointmentsData != null) {
            binding.tvSelectedName.setText("" + appointmentsData.getUserdetails().getContact());
            binding.etAgenda.setText("" + appointmentsData.getAgenda());

            selectedContact = new ContactModel();
            selectedContact.setName(appointmentsData.getUserdetails().getContact());
            selectedContact.setPhone(appointmentsData.getUserdetails().getPhone());
        }

        selectedDates = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        mAdapter = new WeekListAdapter(getActivity());
        mAdapter.setOnDateClickListener(RescheduleFixedMeetingDialog.this);
        binding.llDates.setAdapter(mAdapter);

        /*binding.ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(MarshmallowPermissionHelper.getReadContactPermission(RescheduleFixedMeetingDialog.this, getActivity(),
                        READ_CONTACT_PERMISSION)) {
                    SelectContactFragment mDialog = new SelectContactFragment();
                    mDialog.setOnContactSelected(RescheduleFixedMeetingDialog.this);
                    mDialog.show(getChildFragmentManager(), CONST.CONTACT_DIALOG);
                }

            }
        });*/

        int spanCount = 2; // 2 columns
        int spacing = getResources().getDimensionPixelSize(R.dimen._10sdp);
        boolean includeEdge = true;

        binding.rvAvailableDate.addItemDecoration(new SpacesItemDecoration(spanCount, spacing, includeEdge));
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getActivity(), spanCount);
        binding.rvAvailableDate.setLayoutManager(layoutManager);

        mTimeAdapter = new TimeSlotAdapter(getActivity());
        mTimeAdapter.setOnTimeSlotClickListener(RescheduleFixedMeetingDialog.this);
        binding.rvAvailableDate.setAdapter(mTimeAdapter);

        binding.llChooseDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDatePicker();
            }
        });

        getAvailableTimes(selectedDates);
    }

    private void showDatePicker() {
        Calendar c1 = Calendar.getInstance();

        DatePickerDialog pickerDialog = new DatePickerDialog(getActivity(), RescheduleFixedMeetingDialog.this,
                c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH));
        pickerDialog.getDatePicker().setMinDate(new Date().getTime());
        pickerDialog.show();
    }

    @Override
    public void onContactSelected(ContactModel contactModel) {
        selectedContact = contactModel;

        binding.tvSelectedName.setText(String.format("%s", contactModel.getName()));

        if (InternetConnectionDetector.getInstant(getActivity()).isConnectingToInternet()) {
            getAvailableTimes(selectedDates);
        }

    }

    @Override
    public void onDateClick(String date) {

        if (InternetConnectionDetector.getInstant(getActivity()).isConnectingToInternet()) {
            if (!selectedDates.equalsIgnoreCase(date) && selectedContact != null) {
                selectedDates = date;
                getAvailableTimes(selectedDates);
            } else {
                selectedDates = date;
            }
        }

    }

    public void getAvailableTimes(String date) {

        if (!mDialog.isShowing()) {
            mDialog.show();
        }

        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

        if (selectedContact != null && userData.getUsertype().equalsIgnoreCase("Guest")) {
            HashMap<String, String> requestParameter = new HashMap<>();
            requestParameter.put("phone", String.format("%s", selectedContact.getPhone()));
            requestParameter.put("date", String.format("%s", date));

            new PostStringRequest(getActivity(), requestParameter, RescheduleFixedMeetingDialog.this, "getTimeSlots",
                    NetworkUtility.BASEURL + NetworkUtility.GUEST_GET_AVAILABLE_TIME_BY_PHONE);
        } else {
            HashMap<String, String> requestParameter = new HashMap<>();
            requestParameter.put("userid", String.format("%s", userData.getId()));
            requestParameter.put("date", String.format("%s", date));

            new PostStringRequest(getActivity(), requestParameter, RescheduleFixedMeetingDialog.this, "getEmployeeTimeSlots",
                    NetworkUtility.BASEURL + NetworkUtility.GET_AVAILABLE_TIME_BY_USERID);
        }

    }

    public void getUpcomingTime(String date) {
        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

        if (selectedContact != null) {
            HashMap<String, String> requestParameter = new HashMap<>();
            requestParameter.put("phone", String.format("%s", userData.getId()));
            requestParameter.put("date", String.format("%s", date));

            new PostStringRequest(getActivity(), requestParameter, RescheduleFixedMeetingDialog.this, "getUpcomingMeeting",
                    NetworkUtility.BASEURL + NetworkUtility.UPCOMING_MEETINGS_BY_DATE);
        }

    }

    public void getAvailableRooms(String date) {

        if (!mDialog.isShowing()) {
            mDialog.show();
        }

        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

        if (selectedContact != null) {
            HashMap<String, String> requestParameter = new HashMap<>();
            requestParameter.put("date", String.format("%s", date));

            new PostStringRequest(getActivity(), requestParameter, RescheduleFixedMeetingDialog.this, "getAvailableRooms",
                    NetworkUtility.BASEURL + NetworkUtility.GET_ALL_PLACE_ROOM);
        }

    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {

        try {

            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            switch (type) {

                case "getTimeSlots":

                    JSONObject response = new JSONObject(result);

                    if (response.optString("message").equalsIgnoreCase("Sorry This user doesn't exist.")) {
                        Toast.makeText(getActivity(), response.optString("message"), Toast.LENGTH_SHORT).show();
                    } else if (response.optString("message").equalsIgnoreCase("Sorry you cannot fixed any meeting with this user")) {
                        Toast.makeText(getActivity(), response.optString("message"), Toast.LENGTH_SHORT).show();
                    } else if (response.optString("message").equalsIgnoreCase("Sorry This user doesn't set any availability.")) {
                        //Toast.makeText(getActivity(), response.optString("message"), Toast.LENGTH_SHORT).show();

                        JSONArray timing = response.getJSONArray("timing");
                        JSONObject timingResponse = timing.getJSONObject(0);

                        GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();

                        List<TimeSlot> meetingTimeSlotList = new ArrayList<>();

                        if (timingResponse.optJSONArray("meetings") != null) {
                            TimeSlot[] meetingTimeSlot = gson.fromJson(timingResponse.optJSONArray("meetings").toString(), TimeSlot[].class);
                            meetingTimeSlotList = Arrays.asList(meetingTimeSlot);
                        }

                        mTimeAdapter.notifyDataChange(meetingTimeSlotList, timingResponse.optString("date"),
                                "", "");
                    } else {
                        JSONArray timing = response.getJSONArray("timing");
                        JSONObject timingResponse = timing.getJSONObject(0);

                        GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();

                        List<TimeSlot> meetingTimeSlotList = new ArrayList<>();

                        if (timingResponse.optJSONArray("meetings") != null) {
                            TimeSlot[] meetingTimeSlot = gson.fromJson(timingResponse.optJSONArray("meetings").toString(), TimeSlot[].class);
                            meetingTimeSlotList = Arrays.asList(meetingTimeSlot);
                        }

                        mTimeAdapter.notifyDataChange(meetingTimeSlotList, timingResponse.optString("date"),
                                timingResponse.optString("from"), timingResponse.optString("to"));
                    }

                    break;

                case "getEmployeeTimeSlots":

                    JSONObject responseEmployee = new JSONObject(result);

                    if (responseEmployee.optString("message").equalsIgnoreCase("Sorry This user doesn't exist.")) {
                        Toast.makeText(getActivity(), responseEmployee.optString("message"), Toast.LENGTH_SHORT).show();
                    } else if (responseEmployee.optString("message").equalsIgnoreCase("Sorry you cannot fixed any meeting with this user")) {
                        Toast.makeText(getActivity(), responseEmployee.optString("message"), Toast.LENGTH_SHORT).show();
                    } else if (responseEmployee.optString("message").equalsIgnoreCase("Sorry This user doesn't set any availability.")) {
                        //Toast.makeText(getActivity(), response.optString("message"), Toast.LENGTH_SHORT).show();

                        JSONArray timing = responseEmployee.getJSONArray("timing");
                        JSONObject timingResponse = timing.getJSONObject(0);

                        GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();

                        List<TimeSlot> meetingTimeSlotList = new ArrayList<>();

                        if (timingResponse.optJSONArray("meetings") != null) {
                            TimeSlot[] meetingTimeSlot = gson.fromJson(timingResponse.optJSONArray("meetings").toString(), TimeSlot[].class);
                            meetingTimeSlotList = Arrays.asList(meetingTimeSlot);
                        }

                        mTimeAdapter.notifyDataChange(meetingTimeSlotList, timingResponse.optString("date"),
                                "", "");
                    } else {
                        JSONArray timing = responseEmployee.getJSONArray("timing");
                        JSONObject timingResponse = timing.getJSONObject(0);

                        GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();

                        List<TimeSlot> meetingTimeSlotList = new ArrayList<>();

                        if (timingResponse.optJSONArray("meetings") != null) {
                            TimeSlot[] meetingTimeSlot = gson.fromJson(timingResponse.optJSONArray("meetings").toString(), TimeSlot[].class);
                            meetingTimeSlotList = Arrays.asList(meetingTimeSlot);
                        }

                        mTimeAdapter.notifyDataChange(meetingTimeSlotList, timingResponse.optString("date"),
                                timingResponse.optString("from"), timingResponse.optString("to"));
                    }

                    break;

                case "getAvailableRooms":

                    JSONObject responseRooms = new JSONObject(result);

                    GsonBuilder builder = new GsonBuilder();
                    builder.setPrettyPrinting();
                    Gson gson = builder.create();

                    if (responseRooms.optString("status").equalsIgnoreCase("success")) {
                        RoomData[] meetingTimeSlot = gson.fromJson(responseRooms.optJSONArray("device_list").toString(), RoomData[].class);
                        List<RoomData> roomDataList = Arrays.asList(meetingTimeSlot);

                        if (roomDataList != null)
                            showRoomDialogs(roomDataList);
                    }

                    break;

                case "getUpcomingMeeting":

                    JSONObject responseUpcomingMeeting = new JSONObject(result);


                    break;

                case "bookMeeting":

                    JSONObject bookMeeting = new JSONObject(result);
                    if (bookMeeting.optString("message").equalsIgnoreCase("your request has been sent")) {
                        CONST.scheduleMeetingFetchJob(getActivity(), 1300, CONST.FETCH_JOB_ID);

                        Dialogs.dialogFixedMeetingSuccess(getActivity(), selectedStartTimeSlot, selectedContact, new Dialogs.OnMeetingConfirm() {
                            @Override
                            public void onMeetingOk() {
                                dismiss();
                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), bookMeeting.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {
        Log.d("Response", "Response code: " + responseCode + " " + responseMessage);
    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onTimeSlotClick(TimeSlot startTimeSlot, TimeSlot endTimeSlot) {
        this.selectedStartTimeSlot = startTimeSlot;
        this.selectedEndTimeSlot = endTimeSlot;
    }

    public void submitCreateMeeting() {

        if (selectedContact != null && !selectedDates.isEmpty() && selectedStartTimeSlot != null
                && !binding.etAgenda.getText().toString().isEmpty()) {

            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
            if (userData.getUsertype().equalsIgnoreCase("Guest")) {
                createFixedMeeting();
            } else {
                getAvailableRooms(selectedDates);
            }

        } else if (selectedContact == null) {
            Toast.makeText(getActivity(), getResources().getString(R.string.please_select_contact), Toast.LENGTH_SHORT).show();
        } else if (selectedStartTimeSlot == null) {
            Toast.makeText(getActivity(), getResources().getString(R.string.please_select_timeslot), Toast.LENGTH_SHORT).show();
        } else if (binding.etAgenda.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), getResources().getString(R.string.enter_agenda_for_the_meeting), Toast.LENGTH_SHORT).show();
        }

    }

    private void showRoomDialogs(List<RoomData> roomDataList) {
        Dialogs.dialogShowRooms(getActivity(), roomDataList, new Dialogs.OnOptionSelect() {
            @Override
            public void setRowClick(RoomData roomData) {

                createFixedMeeting(roomData.getPlace_unique_id());
            }
        });
    }

    public void createFixedMeeting() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
            int totalTime = 0;
            Date availableStart, availableEnd;

            if (selectedContact != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("location", String.format("%s", ""));
                requestParameter.put("from_time", String.format("%s", selectedStartTimeSlot.getFrom()));

                availableStart = dateFormat.parse(selectedStartTimeSlot.getDate()
                        + " " + selectedStartTimeSlot.getFrom());

                if (selectedEndTimeSlot != null) {
                    requestParameter.put("to_time", String.format("%s", selectedEndTimeSlot.getTo()));

                    availableEnd = dateFormat.parse(selectedStartTimeSlot.getDate()
                            + " " + selectedEndTimeSlot.getTo());
                } else {
                    requestParameter.put("to_time", String.format("%s", selectedStartTimeSlot.getTo()));

                    availableEnd = dateFormat.parse(selectedStartTimeSlot.getDate()
                            + " " + selectedStartTimeSlot.getTo());
                }

                long secs = (availableEnd.getTime() - availableStart.getTime()) / 1000;
                float hours = (float) secs / 3600;

                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(1);

                if (appointmentsData != null)
                    requestParameter.put("appid", String.format("%s", appointmentsData.getId()));

                requestParameter.put("fdate", String.format("%s", selectedStartTimeSlot.getDate()));
                requestParameter.put("sdate", String.format("%s", ""));
                requestParameter.put("pushid_g", String.format("%s", ""));
                requestParameter.put("phone", String.format("%s", selectedContact.getPhone()));
                requestParameter.put("duration", String.format("%s", ""+df.format(hours)));
                requestParameter.put("designation", String.format("%s", "" + userData.getDesignation()));
                requestParameter.put("department", String.format("%s", ""));
                requestParameter.put("contact", String.format("%s", "" + userData.getId()));
                requestParameter.put("agenda", String.format("%s", binding.etAgenda.getText().toString()));

                new PostStringRequest(getActivity(), requestParameter, RescheduleFixedMeetingDialog.this, "bookMeeting",
                        NetworkUtility.BASEURL + NetworkUtility.RESCHEDULE_MEETING);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void createFixedMeeting(String roomName) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
            int totalTime = 0;
            Date availableStart, availableEnd;

            if (selectedContact != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("location", String.format("%s", "" + roomName));
                requestParameter.put("from_time", String.format("%s", selectedStartTimeSlot.getFrom()));

                availableStart = dateFormat.parse(selectedStartTimeSlot.getDate()
                        + " " + selectedStartTimeSlot.getFrom());

                if (selectedEndTimeSlot != null) {
                    requestParameter.put("to_time", String.format("%s", selectedEndTimeSlot.getTo()));

                    availableEnd = dateFormat.parse(selectedStartTimeSlot.getDate()
                            + " " + selectedEndTimeSlot.getTo());
                } else {
                    requestParameter.put("to_time", String.format("%s", selectedStartTimeSlot.getTo()));

                    availableEnd = dateFormat.parse(selectedStartTimeSlot.getDate()
                            + " " + selectedStartTimeSlot.getTo());
                }

                long secs = (availableEnd.getTime() - availableStart.getTime()) / 1000;
                float hours = (float) secs / 3600;

                DecimalFormat df = new DecimalFormat();
                df.setMaximumFractionDigits(1);

                if (appointmentsData != null)
                    requestParameter.put("appid", String.format("%s", appointmentsData.getId()));

                requestParameter.put("fdate", String.format("%s", selectedStartTimeSlot.getDate()));
                requestParameter.put("sdate", String.format("%s", ""));
                requestParameter.put("pushid_g", String.format("%s", ""));
                requestParameter.put("phone", String.format("%s", selectedContact.getPhone()));
                requestParameter.put("duration", String.format("%s", ""+df.format(hours)));
                requestParameter.put("designation", String.format("%s", "" + userData.getDesignation()));
                requestParameter.put("department", String.format("%s", "" + userData.getDepartment()));
                requestParameter.put("contact", String.format("%s", "" + userData.getId()));
                requestParameter.put("agenda", String.format("%s", binding.etAgenda.getText().toString()));

                new PostStringRequest(getActivity(), requestParameter, RescheduleFixedMeetingDialog.this, "bookMeeting",
                        NetworkUtility.BASEURL + NetworkUtility.RESCHEDULE_MEETING);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case READ_CONTACT_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    /*SelectContactFragment mDialog = new SelectContactFragment();
                    mDialog.setOnContactSelected(RescheduleFixedMeetingDialog.this);
                    mDialog.show(getChildFragmentManager(), CONST.CONTACT_DIALOG);*/
                }
                return;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String stringYear, stringMonth, stringDayOfMonth;

        stringYear = "" + year;

        if (month < 9) {
            stringMonth = "0" + (month + 1);
        } else {
            stringMonth = "" + (month + 1);
        }

        if (dayOfMonth < 10) {
            stringDayOfMonth = "0" + dayOfMonth;
        } else {
            stringDayOfMonth = "" + dayOfMonth;
        }

        selectedDates = stringYear + "-" + stringMonth + "-" + stringDayOfMonth;
        selectedStartTimeSlot = selectedEndTimeSlot = null;

        if (InternetConnectionDetector.getInstant(getActivity()).isConnectingToInternet()) {
            if (selectedContact != null) {
                getAvailableTimes(selectedDates);
            }
        }

        mAdapter.getAllWeekAddress(selectedDates);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //super.onCreateDialog(savedInstanceState);

        Dialog dialog = new Dialog(getActivity());
        final RelativeLayout root = new RelativeLayout(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (mListener != null) {
            mListener.onDismissListener();
        }
    }

    public interface OnDialogEvent {
        void onDismissListener();
    }

    public void setOnDismissClickListener(OnDialogEvent mListener) {
        this.mListener = mListener;
    }

}
