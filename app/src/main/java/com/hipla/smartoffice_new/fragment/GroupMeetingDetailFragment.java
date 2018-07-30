package com.hipla.smartoffice_new.fragment;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.activity.DashboardActivity;
import com.hipla.smartoffice_new.adapter.AddBelongingsAdapter;
import com.hipla.smartoffice_new.adapter.AddRecipientAdapter;
import com.hipla.smartoffice_new.adapter.ReviewListAdapter;
import com.hipla.smartoffice_new.database.Db_Helper;
import com.hipla.smartoffice_new.databinding.FragmentMeetingDetailBinding;
import com.hipla.smartoffice_new.dialogs.Dialogs;
import com.hipla.smartoffice_new.dialogs.RescheduleFixedMeetingDialog;
import com.hipla.smartoffice_new.dialogs.RescheduleFlexibleMeetingDialog;
import com.hipla.smartoffice_new.model.AddBelongingsModel;
import com.hipla.smartoffice_new.model.AddRecipientsModel;
import com.hipla.smartoffice_new.model.ContactModel;
import com.hipla.smartoffice_new.model.GroupMeetingData;
import com.hipla.smartoffice_new.model.Recipient;
import com.hipla.smartoffice_new.model.ReviewMessage;
import com.hipla.smartoffice_new.model.RoomData;
import com.hipla.smartoffice_new.model.UpcomingMeetings;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.model.ZoneInfo;
import com.hipla.smartoffice_new.utils.CONST;
import com.hipla.smartoffice_new.utils.InternetConnectionDetector;
import com.hipla.smartoffice_new.utils.MarshmallowPermissionHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupMeetingDetailFragment extends Fragment implements StringRequestListener, RescheduleFixedMeetingDialog.OnDialogEvent, RescheduleFlexibleMeetingDialog.OnDialogEvent, AddBelongingsAdapter.ManageRowCallback, AddRecipientAdapter.ManageRecipientCallback, SelectContactFragment.OnContactSelected {

    private static final int REQUEST_CALL_PERMISSION = 100;
    private FragmentMeetingDetailBinding binding;
    private ProgressDialog mDialog;
    private GroupMeetingData appointmentsData;
    private ReviewListAdapter mAdapter;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private List<RoomData> roomDataList = new ArrayList<>();
    private List<AddBelongingsModel> addBelongingsModelsList;
    private List<AddRecipientsModel> addRecipientsModelsList;
    private AddBelongingsAdapter addBelongingsAdapter;
    private AddRecipientAdapter addRecipientAdapter;
    private boolean visibleAddBelongings = false, visibleAddRecipients = false;
    private AddBelongingsModel addBelongingsModel = null;
    private static final int READ_CONTACT_PERMISSION = 101;
    private ContactModel selectedContact = null;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 10000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 10 secs */
    private float DISPLACEMENT = 1;

    public GroupMeetingDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_meeting_detail, container, false);
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

        appointmentsData = Paper.book().read(CONST.APPOINTMENT_DATA);
        UpcomingMeetings upcomingMeetings = Paper.book().read(CONST.CURRENT_MEETING_DATA);

        if (upcomingMeetings != null && upcomingMeetings.getId() == appointmentsData.getId()) {
            //getETA(Paper.book().read(CONST.LATITUDE, CONST.DESTINATION_LAT), Paper.book().read(CONST.LONGITUDE, CONST.DESTINATION_LONG));


        } else {
            binding.llEta.setVisibility(View.GONE);
        }


        if (appointmentsData != null) {
            setAppointmentData(appointmentsData);

            mAdapter = new ReviewListAdapter(new ArrayList<ReviewMessage>(), getActivity());
            binding.listComments.setAdapter(mAdapter);

            binding.btnPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Date meetinDate = mDateFormat.parse(appointmentsData.getFdate() + " " + appointmentsData.getTotime());
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(meetinDate);
                        calendar.set(Calendar.MINUTE, CONST.TIME_BEFORE_DETECTION_HRS*60);

                        if (new Date().compareTo(calendar.getTime()) < 0)
                            createReview(appointmentsData, binding.etMessage.getText().toString());
                        else
                            Toast.makeText(getActivity(), getString(R.string.not_able_to_post_now), Toast.LENGTH_LONG).show();

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            binding.llReschedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (appointmentsData.getAppointmentType().equalsIgnoreCase("flexible")) {

                        RescheduleFlexibleMeetingDialog mDialog = new RescheduleFlexibleMeetingDialog();
                        mDialog.setOnDismissClickListener(GroupMeetingDetailFragment.this);
                        mDialog.show(getChildFragmentManager(), "dialog");
                    } else {

                        RescheduleFixedMeetingDialog mDialog = new RescheduleFixedMeetingDialog();
                        mDialog.setOnDismissClickListener(GroupMeetingDetailFragment.this);
                        mDialog.show(getChildFragmentManager(), "dialog");
                    }

                }
            });
        }

        getAllReview(appointmentsData);
        getAvailableRooms(appointmentsData.getFdate());

        binding.ivAdd.setVisibility(View.GONE);

        addBelongingsModelsList = new ArrayList<>();
        if (appointmentsData.getGroup().getOthers() != null && appointmentsData.getGroup().getOthers().length > 0) {

            for (Recipient data :
                    appointmentsData.getGroup().getOthers()) {

                UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

                if (userData.getId() != data.getId()) {
                    AddRecipientsModel addRecipientsModel = new AddRecipientsModel();
                    addRecipientsModel.setClient_id("" + data.getId());
                    addRecipientsModel.setClient_name("" + data.getId());
                    addRecipientsModel.setEmail("" + data.getId());
                    addRecipientsModel.setPhone("" + data.getId());
                    addRecipientsModel.setStatus("" + data.getResponse());

                    addRecipientsModelsList.add(addRecipientsModel);
                }
            }

        } else {
            addRecipientsModelsList = new ArrayList<>();
        }

        addBelongingsAdapter = new AddBelongingsAdapter(this, addBelongingsModelsList);
        addRecipientAdapter = new AddRecipientAdapter(this, addRecipientsModelsList);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.addBelongingsRecyclerView.setLayoutManager(layoutManager);
        binding.addBelongingsRecyclerView.setAdapter(addBelongingsAdapter);

        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getContext());
        binding.addRecipientsRecyclerView.setLayoutManager(layoutManager1);
        binding.addRecipientsRecyclerView.setAdapter(addRecipientAdapter);

        binding.tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Date meetinDate = mDateFormat.parse(appointmentsData.getFdate() + " " + appointmentsData.getFromtime());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(meetinDate);
                    calendar.set(Calendar.MINUTE, CONST.TIME_BEFORE_CANCEL_MEETING_IN_MIN);

                    if (new Date().compareTo(calendar.getTime()) < 0) {
                        setMeetingStatus(appointmentsData, "cancel");
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.you_cant_cancel_meeting_now), Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        binding.tvAddBelongings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.llAddBelongings.setVisibility(View.VISIBLE);
                binding.llAddRecepients.setVisibility(View.GONE);
                binding.llMeetingsReviews.setVisibility(View.GONE);
                getBelongings();
                prepareDataBelongings();
            }
        });

        binding.tvAddReceipent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.llAddBelongings.setVisibility(View.GONE);
                binding.llAddRecepients.setVisibility(View.VISIBLE);
                binding.llMeetingsReviews.setVisibility(View.GONE);

                prepareDataRecipients();
            }
        });
        binding.closeBelongings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.llAddBelongings.setVisibility(View.GONE);
                binding.llAddRecepients.setVisibility(View.GONE);
                binding.llMeetingsReviews.setVisibility(View.VISIBLE);
            }
        });
        binding.closeReceipents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.llAddBelongings.setVisibility(View.GONE);
                binding.llAddRecepients.setVisibility(View.GONE);
                binding.llMeetingsReviews.setVisibility(View.VISIBLE);
            }
        });
        binding.ivAddBelonging.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.etBelongingName.getText() != null && binding.etBelongingValue.getText() != null && !TextUtils.isEmpty(binding.etBelongingName.getText().toString()) && !TextUtils.isEmpty(binding.etBelongingValue.getText().toString())) {
                    addBelongingsModel = new AddBelongingsModel(binding.etBelongingName.getText().toString().trim(), binding.etBelongingValue.getText().toString().trim());
                    addBelongings();
                }
            }
        });

        binding.ivNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                binding.llAddBelongings.setVisibility(View.GONE);
                binding.llAddRecepients.setVisibility(View.GONE);
                binding.llMeetingsReviews.setVisibility(View.VISIBLE);
            }
        });

    }

    public void setAppointmentData(final GroupMeetingData appointments) {
        try {
            final Date meetingDate = new SimpleDateFormat("yyyy-MM-dd").parse(appointments.getFdate());

            binding.tvDay.setText("" + new SimpleDateFormat("dd").format(meetingDate));
            binding.tvDate.setText("" + new SimpleDateFormat("MMM yyyy").format(meetingDate));

            final Date meetingDateTime = mDateFormat.parse(appointmentsData.getFdate()
                    + " " + appointmentsData.getFromtime());

            final Date meetingDateEndTime = mDateFormat.parse(appointmentsData.getFdate()
                    + " " + appointmentsData.getTotime());

            if (getMeetingStatus(appointmentsData).equalsIgnoreCase("end")) {
                binding.ivMeetingStatus.setImageResource(R.drawable.ic_end);

                binding.llActions.setVisibility(View.GONE);
                binding.tvAddReceipent.setVisibility(View.GONE);
                binding.tvAddBelongings.setVisibility(View.GONE);
                binding.tvCancel.setVisibility(View.GONE);
                binding.rlDate.setBackgroundResource(R.drawable.normal_card_red);

            } else if (getMeetingStatus(appointmentsData).equalsIgnoreCase("pending")) {
                binding.ivMeetingStatus.setImageResource(R.drawable.ic_pending);

                binding.llActions.setVisibility(View.VISIBLE);
                binding.ivAction.setImageResource(R.drawable.ic_pendings);
                binding.llReschedule.setVisibility(View.VISIBLE);
                binding.llCall.setVisibility(View.GONE);
                binding.llIndoorNavigation.setVisibility(View.GONE);
                binding.llOutdoorNavigation.setVisibility(View.GONE);
                binding.tvCancel.setVisibility(View.VISIBLE);
                binding.tvAddReceipent.setVisibility(View.GONE);
                binding.tvAddBelongings.setVisibility(View.GONE);

                binding.rlDate.setBackgroundResource(R.drawable.normal_card_orange);

                if (new Date().compareTo(meetingDateEndTime) <= 0) {
                    binding.llActions.setVisibility(View.VISIBLE);
                } else {
                    binding.llActions.setVisibility(View.GONE);
                }

            } else if (getMeetingStatus(appointmentsData).equalsIgnoreCase("confirm")) {
                binding.ivMeetingStatus.setImageResource(R.drawable.ic_meetingconfirm);

                binding.llActions.setVisibility(View.VISIBLE);
                binding.ivAction.setImageResource(R.drawable.ic_ends);
                binding.llReschedule.setVisibility(View.VISIBLE);
                binding.llCall.setVisibility(View.VISIBLE);
                binding.llIndoorNavigation.setVisibility(View.VISIBLE);
                binding.llOutdoorNavigation.setVisibility(View.VISIBLE);
                binding.tvAddReceipent.setVisibility(View.GONE);
                binding.tvAddBelongings.setVisibility(View.VISIBLE);
                binding.tvCancel.setVisibility(View.VISIBLE);
                binding.rlDate.setBackgroundResource(R.drawable.normal_card_green);
                binding.tvAddReceipent.setVisibility(View.GONE);

                Calendar calendar = Calendar.getInstance();
                calendar.setTime(meetingDateEndTime);

                if (new Date().compareTo(calendar.getTime()) <= 0) {
                    binding.tvAddBelongings.setVisibility(View.VISIBLE);
                    binding.llActions.setVisibility(View.VISIBLE);
                } else {
                    binding.tvAddBelongings.setVisibility(View.GONE);
                    binding.llActions.setVisibility(View.GONE);
                }

            } else if (getMeetingStatus(appointmentsData).equalsIgnoreCase("cancel")) {
                binding.ivMeetingStatus.setImageResource(R.drawable.ic_cancel);

                binding.llActions.setVisibility(View.GONE);
                binding.tvAddReceipent.setVisibility(View.GONE);
                binding.tvAddBelongings.setVisibility(View.GONE);
                binding.tvCancel.setVisibility(View.GONE);
                binding.rlDate.setBackgroundResource(R.drawable.normal_card_red);
            }

            if (new Date().compareTo(meetingDateTime) <= 0 && (!getMeetingStatus(appointments).equalsIgnoreCase("end") &&
                    !getMeetingStatus(appointments).equalsIgnoreCase("cancel"))) {

                UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

                if (meetingDetail != null && meetingDetail.getId() == appointmentsData.getId() && (Paper.book().read(CONST.IS_DETECTION_STARTED, false))) {
                    binding.tvCancel.setVisibility(View.GONE);
                } else {
                    binding.tvCancel.setVisibility(View.VISIBLE);
                }
            } else {
                binding.tvCancel.setVisibility(View.GONE);
            }

            binding.tvFname.setText("" + appointments.getGroup().getCreator1().getContact());
            binding.tvEmail.setText("" + appointments.getGroup().getCreator1().getEmail());
            binding.tvPhone.setText("" + appointments.getGroup().getCreator1().getPhone());
            binding.tvMeetingTime.setText(String.format(getString(R.string.timing_format),
                    appointments.getFromtime(), appointments.getTotime()));
            //binding.tvMeetingDate.setText(String.format(getString(R.string.meeting_date_value), appointments.getFdate()));
            binding.tvMeetingType.setText(String.format(getString(R.string.meeting_value), appointments.getAppointmentType()));
            //binding.tvLocation.setText(String.format(getString(R.string.meeting_location_value), appointments.getLocation()));
            binding.tvAgenda.setText(String.format(getString(R.string.agenda_value), appointments.getAgenda()));

            binding.ivAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getMeetingStatus(appointments).equalsIgnoreCase("pending")) {
                        try {
                            Date meetingDateTime = mDateFormat.parse(appointments.getFdate() + " " + appointments.getTotime());

                            if (new Date().compareTo(meetingDateTime) < 0) {
                                setMeetingStatus(appointments, "confirm");
                            } else {
                                Toast.makeText(getActivity(), getString(R.string.you_cant_start_meeting_now), Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception ex) {

                        }

                    } else if (getMeetingStatus(appointments).equalsIgnoreCase("confirm")) {
                        try {
                            Date meetingDateTime = mDateFormat.parse(appointments.getFdate() + " " + appointments.getTotime());

                            if (new Date().compareTo(meetingDateTime) > 0) {
                                setMeetingStatus(appointments, "end");
                            } else {
                                Toast.makeText(getActivity(), getString(R.string.you_cant_end_meeting_now), Toast.LENGTH_SHORT).show();
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });

            binding.ivCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Date meetinDate = mDateFormat.parse(appointments.getFdate() + " " + appointments.getFromtime());
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(meetinDate);
                        calendar.set(Calendar.MINUTE, CONST.TIME_BEFORE_CANCEL_MEETING_IN_MIN);

                        if (new Date().compareTo(calendar.getTime()) < 0) {
                            setMeetingStatus(appointments, "cancel");
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.you_cant_cancel_meeting_now), Toast.LENGTH_SHORT).show();
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            binding.llCall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (MarshmallowPermissionHelper.getCallPermission(GroupMeetingDetailFragment.this,
                            getActivity(), REQUEST_CALL_PERMISSION)) {
                        makeCall(appointments.getGroup().getCreator1().getPhone());
                    }
                }
            });

            binding.llOutdoorNavigation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    openGoogleMaps();
                }
            });

            binding.llIndoorNavigation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    openIndoorMap();
                }
            });

            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
            if (userData != null && !userData.getUsertype().equalsIgnoreCase("Guest")) {
                binding.tvAddBelongings.setVisibility(View.GONE);
            } else {
                binding.tvAddBelongings.setVisibility(View.VISIBLE);
            }

            UpcomingMeetings upcomingMeetings = Paper.book().read(CONST.CURRENT_MEETING_DATA);
            if(upcomingMeetings!=null && upcomingMeetings.getId()==appointments.getId()) {
                if (Paper.book().read(CONST.IS_DETECTION_STARTED, false)) {
                    binding.llAction.setVisibility(View.VISIBLE);
                    binding.llEta.setVisibility(View.GONE);
                } else {
                    binding.llAction.setVisibility(View.GONE);
                    binding.llEta.setVisibility(View.VISIBLE);
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
            case REQUEST_CALL_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (appointmentsData != null)
                        makeCall(appointmentsData.getGroup().getCreator1().getPhone());
                }
                return;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void makeCall(String mobileNo) {
        String uri = "tel:" + mobileNo.trim();

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(uri));
        startActivity(intent);
    }

    private void getETA(double latitude, double longitude) {

        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            String DESTINATION_URL = "https://maps.googleapis.com/maps/api/distancematrix/json?" +
                    "origins=" + latitude + "," + longitude + "&destinations=" + CONST.DESTINATION_LAT + "," + CONST.DESTINATION_LONG +
                    "&mode=driving&key=" + CONST.DISTANCE_API_KEY;


            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();

                new PostStringRequest(getActivity(), requestParameter, GroupMeetingDetailFragment.this, "getETA",
                        DESTINATION_URL);
            }

        } catch (Exception ex) {

        } finally {

        }
    }

    private void confirmMeeting(final GroupMeetingData data) {
        if (data.getAppointmentType().equalsIgnoreCase("flexible")) {
            Dialogs.dialogShowDates(getActivity(),data.getTotime(), data.getFdate(), data.getSdate(), new Dialogs.OnDateSelect() {
                @Override
                public void setRowClick(String date) {

                    UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

                    if (userData.getUsertype().equalsIgnoreCase("Guest")) {
                        confirmFlexibleMeetingRequest(data, "confirm", date);
                    } else {
                        showRoomDialogs(data, roomDataList, "confirm", date);
                    }

                }
            });
        } else {

            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData.getUsertype().equalsIgnoreCase("Guest")) {
                confirmFixedMeetingRequest(data, "confirm");
            } else {
                showRoomDialogs(data, roomDataList, "confirm");
            }

        }
    }

    private void showRoomDialogs(final GroupMeetingData data, List<RoomData> roomDataList, final String status, final String date) {
        Dialogs.dialogShowRooms(getActivity(), roomDataList, new Dialogs.OnOptionSelect() {
            @Override
            public void setRowClick(RoomData roomData) {

                confirmFlexibleMeetingRequest(data, status, date, roomData.getPlace_unique_id());
            }
        });
    }

    private void showRoomDialogs(final GroupMeetingData data, List<RoomData> roomDataList, final String status) {
        Dialogs.dialogShowRooms(getActivity(), roomDataList, new Dialogs.OnOptionSelect() {
            @Override
            public void setRowClick(RoomData roomData) {

                confirmFixedMeetingRequest(data, status, roomData.getPlace_unique_id());
            }
        });
    }

    private void setMeetingStatus(GroupMeetingData data, String status) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("meeting_id", String.format("%s", data.getId()));
                requestParameter.put("status", String.format("%s", status));

                new PostStringRequest(getActivity(), requestParameter, GroupMeetingDetailFragment.this, "setMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_GROUP_MEETING_STATUS);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }

    private void createReview(GroupMeetingData data, String review) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", data.getId()));
                requestParameter.put("review", String.format("%s", review));

                new PostStringRequest(getActivity(), requestParameter, GroupMeetingDetailFragment.this, "createReview",
                        NetworkUtility.BASEURL + NetworkUtility.CREATE_REVIEW);
            }

        } catch (Exception ex) {

        }
    }

    private void getAllReview(GroupMeetingData data) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("appid", String.format("%s", data.getId()));

                new PostStringRequest(getActivity(), requestParameter, GroupMeetingDetailFragment.this, "getAllReview",
                        NetworkUtility.BASEURL + NetworkUtility.GET_ALL_REVIEW);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }

    public void getAvailableRooms(String date) {
        if (!mDialog.isShowing()) {
            mDialog.show();
        }

        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

        if (userData != null) {
            HashMap<String, String> requestParameter = new HashMap<>();
            requestParameter.put("date", String.format("%s", date));

            new PostStringRequest(getActivity(), requestParameter, GroupMeetingDetailFragment.this, "getAvailableRooms",
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

                case "getETA":
                    JSONObject response = new JSONObject(result);
                    JSONArray rows = response.getJSONArray("rows");
                    JSONArray elements = rows.getJSONObject(0).getJSONArray("elements");

                    JSONObject elementOnject = elements.getJSONObject(0).getJSONObject("duration");
                    JSONObject distanceElementOnject = elements.getJSONObject(0).getJSONObject("distance");

                    int timeTOReachInSeconds = elementOnject.getInt("value");
                    String timeTOReachInMins = elementOnject.optString("text");

                    String distanceInKm = distanceElementOnject.optString("text");

                    if(timeTOReachInSeconds>0){
                        binding.llEta.setVisibility(View.VISIBLE);

                        binding.tvEtaTime.setText(String.format(getString(R.string.eta_format), timeTOReachInMins));
                        binding.tvEtaDistance.setText(String.format(getString(R.string.distance_format), distanceInKm));
                    }else{
                        binding.llEta.setVisibility(View.GONE);
                    }

                    break;

                case "setMeetingStatus":

                    JSONObject meetingStatusResponse = new JSONObject(result);
                    if (meetingStatusResponse.optString("status").equalsIgnoreCase("success") &&
                            meetingStatusResponse.optString("message").equalsIgnoreCase("you have successfully update your response")) {

                        String status = meetingStatusResponse.getJSONObject("apointment").getString("status");
                        String location = meetingStatusResponse.getJSONObject("apointment").optString("location");
                        appointmentsData.setStatus(status);
                        appointmentsData.setLocation(location);

                        setAppointmentData(appointmentsData);

                        CONST.scheduleMeetingFetchJob(getActivity(), 1300, CONST.FETCH_JOB_ID);

                    } else {
                        Toast.makeText(getActivity(), meetingStatusResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;

                case "setFixedMeetingStatus":

                    JSONObject fixedMeetingStatusResponse = new JSONObject(result);
                    if (fixedMeetingStatusResponse.optString("status").equalsIgnoreCase("success") &&
                            fixedMeetingStatusResponse.optString("message").equalsIgnoreCase("your appointment status updated")) {

                        String status = fixedMeetingStatusResponse.getJSONObject("apointment").getString("status");
                        String location = fixedMeetingStatusResponse.getJSONObject("apointment").optString("location");
                        appointmentsData.setStatus(status);
                        appointmentsData.setLocation(location);

                        setAppointmentData(appointmentsData);

                        CONST.scheduleMeetingFetchJob(getActivity(), 1300, CONST.FETCH_JOB_ID);

                    } else {
                        Toast.makeText(getActivity(), fixedMeetingStatusResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;

                case "setFlexibleMeetingStatus":

                    JSONObject flexibleMeetingStatusResponse = new JSONObject(result);
                    if (flexibleMeetingStatusResponse.optString("status").equalsIgnoreCase("success") &&
                            flexibleMeetingStatusResponse.optString("message").equalsIgnoreCase("your appointment status updated")) {

                        String status = flexibleMeetingStatusResponse.getJSONObject("apointment").getString("status");
                        String location = flexibleMeetingStatusResponse.getJSONObject("apointment").optString("location");
                        appointmentsData.setStatus(status);
                        appointmentsData.setLocation(location);

                        setAppointmentData(appointmentsData);

                        CONST.scheduleMeetingFetchJob(getActivity(), 1300, CONST.FETCH_JOB_ID);

                    } else {
                        Toast.makeText(getActivity(), flexibleMeetingStatusResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;

                case "createReview":

                    JSONObject createReviewResponse = new JSONObject(result);
                    if (createReviewResponse.optString("status").equalsIgnoreCase("success") &&
                            createReviewResponse.optString("message").equalsIgnoreCase("Review Saved")) {

                        getAllReview(appointmentsData);

                    } else {
                        Toast.makeText(getActivity(), createReviewResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;

                case "getAllReview":

                    JSONObject getAllReviewResponse = new JSONObject(result);
                    if (getAllReviewResponse.optString("status").equalsIgnoreCase("success")) {

                        binding.etMessage.setText("");

                        GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();

                        ReviewMessage[] reviewMessages = gson.fromJson(getAllReviewResponse.getJSONArray("review").toString()
                                , ReviewMessage[].class);
                        List<ReviewMessage> mData = Arrays.asList(reviewMessages);

                        if (mData.size() > 0) {
                            View footerView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.outbox_footer, null, false);
                            binding.listComments.addFooterView(footerView);
                        }

                        if (mAdapter != null) {
                            mAdapter.notifyAdpater(mData);
                        }

                    } else {
                        Toast.makeText(getActivity(), getAllReviewResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;

                case "getAvailableRooms":

                    JSONObject responseRooms = new JSONObject(result);

                    GsonBuilder builder = new GsonBuilder();
                    builder.setPrettyPrinting();
                    Gson gson = builder.create();

                    if (responseRooms.optString("status").equalsIgnoreCase("success")) {
                        RoomData[] meetingTimeSlot = gson.fromJson(responseRooms.optJSONArray("device_list").toString(), RoomData[].class);
                        roomDataList = Arrays.asList(meetingTimeSlot);

                        if (roomDataList == null) {
                            roomDataList = new ArrayList<>();
                        }
                    } else {

                    }

                    break;

                case "addBelongings":

                    JSONObject addedBelongings = new JSONObject(result);
                    if (addedBelongings.optString("status").equalsIgnoreCase("success")) {

                        addBelongingsModel.setId(addedBelongings.optInt("row_id"));
                        Log.d("jos", "onSuccess: " + addedBelongings.toString());
                        binding.etBelongingName.setText("");
                        binding.etBelongingValue.setText("");
                        addBelongingsModelsList.add(addBelongingsModel);
                        prepareDataBelongings();
                        addBelongingsModel = null;
                    }
                    break;

                case "removeBelongings":
                    JSONObject removeBelongings = new JSONObject(result);
                    Log.d("jos", "onSuccess: " + removeBelongings.toString());
                    addBelongingsModelsList.remove(addBelongingsModel);
                    prepareDataBelongings();
                    addBelongingsModel = null;
                    break;
                case "getBelongings":
                    JSONObject getBelongings = new JSONObject(result);
                    Log.d("jos", "onSuccess: " + getBelongings.toString());

                    if (getBelongings.optString("status").equalsIgnoreCase("success")) {

                        GsonBuilder builder1 = new GsonBuilder();
                        builder1.setPrettyPrinting();
                        Gson gson1 = builder1.create();

                        AddBelongingsModel[] addBelongingsModels = gson1.fromJson(getBelongings.getJSONArray("belongings").toString()
                                , AddBelongingsModel[].class);
                        List<AddBelongingsModel> mData = Arrays.asList(addBelongingsModels);
                        addBelongingsModelsList.clear();
                        addBelongingsModelsList.addAll(mData);

                        prepareDataBelongings();
                    } else {
                        Toast.makeText(getActivity(), getBelongings.optString("message"), Toast.LENGTH_SHORT).show();
                    }


                    break;
                case "addRecipients":
                    JSONObject addedRecipient = new JSONObject(result); // id parsing required
                    if (addedRecipient.optString("status").equalsIgnoreCase("success")) {
                        binding.tvSelectedName.setText("");
                        addRecipientsModelsList.add(new AddRecipientsModel(Integer.toString(addRecipientsModelsList.size()), selectedContact.getName()));
                        prepareDataRecipients();
                        selectedContact = null;
                        //  Log.d("jos", "onSuccess: " + addedRecipient.toString());
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

    private void confirmFixedMeetingRequest(GroupMeetingData data, String status) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", data.getId()));
                requestParameter.put("status", String.format("%s", status));

                new PostStringRequest(getActivity(), requestParameter, GroupMeetingDetailFragment.this, "setFixedMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }

    private void confirmFixedMeetingRequest(GroupMeetingData data, String status, String roomName) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", data.getId()));
                requestParameter.put("status", String.format("%s", status));
                requestParameter.put("location", String.format("%s", roomName));

                new PostStringRequest(getActivity(), requestParameter, GroupMeetingDetailFragment.this, "setFixedMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }

    private void confirmFlexibleMeetingRequest(GroupMeetingData data, String status, String meetingDate) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", data.getId()));
                requestParameter.put("status", String.format("%s", status));
                requestParameter.put("date", String.format("%s", meetingDate));

                new PostStringRequest(getActivity(), requestParameter, GroupMeetingDetailFragment.this, "setFlexibleMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }

    private void confirmFlexibleMeetingRequest(GroupMeetingData data, String status, String meetingDate, String roomName) {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("appid", String.format("%s", data.getId()));
                requestParameter.put("status", String.format("%s", status));
                requestParameter.put("date", String.format("%s", meetingDate));
                requestParameter.put("location", String.format("%s", roomName));

                new PostStringRequest(getActivity(), requestParameter, GroupMeetingDetailFragment.this, "setFlexibleMeetingStatus",
                        NetworkUtility.BASEURL + NetworkUtility.SET_MEETING_STATUS);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

        }
    }

    private void openGoogleMaps() {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("https")
                .authority("www.google.com").appendPath("maps").appendPath("dir").appendPath("").appendQueryParameter("api", "1")
                .appendQueryParameter("destination", CONST.DESTINATION_LAT + "," + CONST.DESTINATION_LONG);
        String url = builder.build().toString();
        Log.d("Directions", url);
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void openIndoorMap() {
        if (Paper.book().read(CONST.IS_DETECTION_STARTED, false)) {
            Db_Helper db_helper = new Db_Helper(getActivity());
            ZoneInfo zoneInfo = db_helper.getZoneInfo(String.format("%s", "1"));
            if (zoneInfo != null) {
                String[] location = zoneInfo.getCenterPoint().split(",");

                NavigineMapDialogNew mapDialog = new NavigineMapDialogNew();
                Bundle bundle = new Bundle();
                bundle.putString(CONST.POINTX, location[0]);
                bundle.putString(CONST.POINTY, location[1]);
                mapDialog.setArguments(bundle);
                if (mapDialog != null && mapDialog.getDialog() != null
                        && mapDialog.getDialog().isShowing()) {
                    //dialog is showing so do something
                } else {
                    //dialog is not showing
                    mapDialog.show(getChildFragmentManager(), "mapDialog");
                }
            }
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.no_info_available), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDismissListener() {
        appointmentsData = Paper.book().read(CONST.APPOINTMENT_DATA);

        if (appointmentsData != null) {
            setAppointmentData(appointmentsData);
        }

        if (getActivity() != null) {
            ((DashboardActivity) getActivity()).backFromFragment();
        }
    }

    @Override
    public void onRowAdd() {

        Log.d("jos", "onRowAdd: ");
        AddBelongingsModel model = new AddBelongingsModel("", "" +
                "");
        addBelongingsModelsList.add(model);

        addBelongingsAdapter.notifyDataSetChanged();
        addBelongings();
    }

    @Override
    public void onRowRemove(int position) {

        Log.d("jos", "onRowRemove: ");
        if (addBelongingsModelsList.get(position).getId() > 0) {
            addBelongingsModel = addBelongingsModelsList.get(position);
            removeBelongings(addBelongingsModel.getId());
        }

    }


    public void addBelongings() {
        try {
            if (appointmentsData != null && addBelongingsModel != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("meeting_id", String.format("%s", appointmentsData.getId()));
                requestParameter.put("property_type", String.format("%s", addBelongingsModel.getPropertyType()));
                requestParameter.put("property_value", String.format("%s", addBelongingsModel.getPropertyValue()));
                requestParameter.put("device_id", String.format("%s", Paper.book().read(NetworkUtility.TOKEN)));
                requestParameter.put("guest_id", String.format("%s", appointmentsData.getGroup().getCreator1().getId()));

                new PostStringRequest(getActivity(), requestParameter,
                        GroupMeetingDetailFragment.this, "addBelongings",
                        NetworkUtility.BASEURL + NetworkUtility.ADD_BELONGINGS);
            }

        } catch (Exception ex) {

        }
    }

    public void removeBelongings(int rowId) {
        HashMap<String, String> requestParameter = new HashMap<>();
        requestParameter.put("row_id", String.format("%s", rowId));

        new PostStringRequest(getActivity(), requestParameter, GroupMeetingDetailFragment.this, "removeBelongings",
                NetworkUtility.BASEURL + NetworkUtility.DELETE_BELONGINGS);
    }

    public void getBelongings() {
        try {

            if (appointmentsData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("meeting_id", String.format("%s", appointmentsData.getId()));
                new PostStringRequest(getActivity(), requestParameter,
                        GroupMeetingDetailFragment.this, "getBelongings",
                        NetworkUtility.BASEURL + NetworkUtility.GET_BELONGINGS);
            }

        } catch (Exception ex) {

        }
    }

    @Override
    public void onAddRecipient() {

    }

    @Override
    public void onRemoveRecipient(int position) {

    }

    @Override
    public void onContactSelected(ContactModel contactModel) {
        selectedContact = contactModel;

        binding.tvSelectedName.setText(String.format("%s", contactModel.getName()));
        if (selectedContact != null && InternetConnectionDetector.getInstant(getActivity()).isConnectingToInternet()) {
            onAddRecipient();
        }
    }

    public void prepareDataBelongings() {
        Log.d("jos", "onRowAdd: ");

        if (addBelongingsModelsList != null) {

            addBelongingsAdapter.notifyDataChange(addBelongingsModelsList);

        }

    }

    public void prepareDataRecipients() {
        Log.d("jos", "onRowAdd: ");

        if (addRecipientsModelsList == null || addRecipientsModelsList.size() < 1) {
            addRecipientsModelsList = new ArrayList<AddRecipientsModel>();
        }
        addRecipientAdapter.notifyDataChange(addRecipientsModelsList);

    }

    private String getMeetingStatus(GroupMeetingData upcomingMeetings) {
        Recipient[] recipientsModel = upcomingMeetings.getGroup().getOthers();
        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
        if (recipientsModel != null && recipientsModel.length > 0) {
            for (Recipient data :
                    recipientsModel) {
                if (data.getId() == userData.getId()) {

                    return data.getResponse();
                }
            }
        }

        return "";
    }

    @Override
    public void onResume() {
        super.onResume();

        UpcomingMeetings upcomingMeetings = Paper.book().read(CONST.CURRENT_MEETING_DATA);

        if(upcomingMeetings!=null && upcomingMeetings.getId()==appointmentsData.getId()) {
            // Register the listener with Location Manager's network provider
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                startLocationUpdates();
            }
        }

    }

    @Override
    public void onPause() {
        super.onPause();

        if(mFusedLocationClient!=null && mLocationCallback!=null){
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        mLocationRequest.setMaxWaitTime(UPDATE_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(getActivity());
        settingsClient.checkLocationSettings(locationSettingsRequest);

        mLocationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                getETA(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
            };

        };
        // Get last known recent location using new Google Play Services SDK (v11+)
        mFusedLocationClient = getFusedLocationProviderClient(getActivity());
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                Looper.myLooper());
    }

}
