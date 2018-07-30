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
import com.hipla.smartoffice_new.adapter.ExtendMeetingTimeSlotAdapter;
import com.hipla.smartoffice_new.adapter.WeekListAdapter;
import com.hipla.smartoffice_new.databinding.DialogFragmentExtendMeetingBinding;
import com.hipla.smartoffice_new.model.ContactModel;
import com.hipla.smartoffice_new.model.TimeSlot;
import com.hipla.smartoffice_new.model.UpcomingMeetings;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.utils.CONST;
import com.hipla.smartoffice_new.utils.InternetConnectionDetector;
import com.hipla.smartoffice_new.utils.SpacesItemDecoration;

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
public class ExtendMeetingDialog extends android.support.v4.app.DialogFragment implements SelectContactFragment.OnContactSelected,
        WeekListAdapter.OnDateClickListener, StringRequestListener, ExtendMeetingTimeSlotAdapter.OnTimeSlotClickListener, DatePickerDialog.OnDateSetListener {

    private static final int READ_CONTACT_PERMISSION = 101;
    private DialogFragmentExtendMeetingBinding binding;
    private WeekListAdapter mAdapter;
    private ContactModel selectedContact;
    private ExtendMeetingTimeSlotAdapter mTimeAdapter;
    private String selectedDates = "";
    private TimeSlot selectedStartTimeSlot, selectedEndTimeSlot;
    private ProgressDialog mDialog;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private UpcomingMeetings appointmentsData;
    private OnDialogEvent mListener;
    private boolean isExtented = false;

    public ExtendMeetingDialog() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_fragment_extend_meeting, container, false);
        binding.setFragment(ExtendMeetingDialog.this);

        try {
            initView();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return binding.getRoot();
    }

    private void initView() throws ParseException {

        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage(getResources().getString(R.string.please_wait));

        appointmentsData = Paper.book().read(CONST.CURRENT_MEETING_DATA);
        if (appointmentsData != null) {
            binding.tvSelectedName.setText("" + appointmentsData.getGuest().getContact());
            binding.etAgenda.setText("" + appointmentsData.getAgenda());

            selectedContact = new ContactModel();
            selectedContact.setName(appointmentsData.getGuest().getContact());
            selectedContact.setPhone(appointmentsData.getGuest().getPhone());
        }

        selectedDates = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        binding.tvMeetingDate.setText("" + selectedDates);

        mAdapter = new WeekListAdapter(getActivity(), false);
        mAdapter.setOnDateClickListener(ExtendMeetingDialog.this);
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

        Date endTime = dateFormat.parse(appointmentsData.getFdate() + " " + appointmentsData.getTotime());

        Calendar cal = Calendar.getInstance();
        cal.setTime(endTime);

        if (Paper.book().read(CONST.IS_HALF_HOUR_SLOT, false)) {
            cal.add(Calendar.MINUTE, 30 * 2);
        } else {
            cal.add(Calendar.MINUTE, 60 * 2);
        }

        mTimeAdapter = new ExtendMeetingTimeSlotAdapter(getActivity(), appointmentsData.getTotime(),
                new SimpleDateFormat("hh:mm a").format(cal.getTime()));
        mTimeAdapter.setOnTimeSlotClickListener(ExtendMeetingDialog.this);
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

        DatePickerDialog pickerDialog = new DatePickerDialog(getActivity(), ExtendMeetingDialog.this,
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

            new PostStringRequest(getActivity(), requestParameter, ExtendMeetingDialog.this, "getTimeSlots",
                    NetworkUtility.BASEURL + NetworkUtility.GUEST_GET_AVAILABLE_TIME_BY_PHONE);
        } else {
            HashMap<String, String> requestParameter = new HashMap<>();
            requestParameter.put("userid", String.format("%s", userData.getId()));
            requestParameter.put("date", String.format("%s", date));

            new PostStringRequest(getActivity(), requestParameter, ExtendMeetingDialog.this, "getEmployeeTimeSlots",
                    NetworkUtility.BASEURL + NetworkUtility.GET_AVAILABLE_TIME_BY_USERID);
        }

    }

    public void getUpcomingTime(String date) {
        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

        if (selectedContact != null) {
            HashMap<String, String> requestParameter = new HashMap<>();
            requestParameter.put("phone", String.format("%s", userData.getId()));
            requestParameter.put("date", String.format("%s", date));

            new PostStringRequest(getActivity(), requestParameter, ExtendMeetingDialog.this, "getUpcomingMeeting",
                    NetworkUtility.BASEURL + NetworkUtility.UPCOMING_MEETINGS_BY_DATE);
        }

    }

    public void extendMeeting(String to_time) {
        appointmentsData = Paper.book().read(CONST.CURRENT_MEETING_DATA);

        if (selectedContact != null) {
            HashMap<String, String> requestParameter = new HashMap<>();
            requestParameter.put("to_time", String.format("%s", to_time));
            requestParameter.put("meeting_id", String.format("%s", "" + appointmentsData.getId()));

            new PostStringRequest(getActivity(), requestParameter, ExtendMeetingDialog.this, "bookMeeting",
                    NetworkUtility.BASEURL + NetworkUtility.EXTEND_MEETING);
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


                    break;

                case "getUpcomingMeeting":

                    JSONObject responseUpcomingMeeting = new JSONObject(result);


                    break;

                case "bookMeeting":

                    JSONObject bookMeeting = new JSONObject(result);
                    if (bookMeeting.optString("status").equalsIgnoreCase("success")) {
                        Paper.book().delete(CONST.NEEDS_TO_RESCHEDULE);
                        CONST.cancelEndMeetingJob(getActivity(), CONST.SCHEDULE_END_MEETING_JOB_ID);

                        UpcomingMeetings appointmentsData = Paper.book().read(CONST.CURRENT_MEETING_DATA);
                        if (selectedEndTimeSlot != null)
                            appointmentsData.setTotime("" + selectedEndTimeSlot.getTo());
                        else
                            appointmentsData.setTotime("" + selectedStartTimeSlot.getTo());

                        Paper.book().write(CONST.CURRENT_MEETING_DATA, appointmentsData);

                        //schedule extend meeting again
                        Date meetingDateTime = dateFormat.parse(appointmentsData.getFdate() + " " + appointmentsData.getTotime());

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(meetingDateTime);
                        cal.add(Calendar.MINUTE, CONST.TIME_BEFORE_EXTEND_MEETING_IN_MIN);

                        Calendar cal1 = Calendar.getInstance();
                        cal1.setTime(new Date());

                        if ((cal.getTimeInMillis() - cal1.getTimeInMillis()) > 0) {
                            CONST.scheduleExtendMeetingJob(getActivity(),
                                    cal.getTimeInMillis() - cal1.getTimeInMillis(), CONST.SCHEDULE_EXTEND_MEETING_JOB_ID);
                        } else {
                            CONST.scheduleExtendMeetingJob(getActivity(),
                                    1300, CONST.SCHEDULE_EXTEND_MEETING_JOB_ID);
                        }

                        isExtented = true;
                        dismiss();
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

            } else {

            }

            if (selectedEndTimeSlot != null)
                extendMeeting("" + selectedEndTimeSlot.getTo());
            else
                extendMeeting("" + selectedStartTimeSlot.getTo());

        } else if (selectedContact == null) {
            Toast.makeText(getActivity(), getResources().getString(R.string.please_select_contact), Toast.LENGTH_SHORT).show();
        } else if (selectedStartTimeSlot == null) {
            Toast.makeText(getActivity(), getResources().getString(R.string.please_select_timeslot), Toast.LENGTH_SHORT).show();
        } else if (binding.etAgenda.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), getResources().getString(R.string.enter_agenda_for_the_meeting), Toast.LENGTH_SHORT).show();
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

        if (mListener != null && isExtented) {
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
