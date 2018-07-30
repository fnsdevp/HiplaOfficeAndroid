package com.hipla.smartoffice_new.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.model.AddRecipientsModel;
import com.hipla.smartoffice_new.model.UpcomingMeetings;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.utils.CONST;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import io.paperdb.Paper;

/**
 * Created by FNSPL on 8/21/2017.
 */

public class UpcomingMeetingsAdapter extends BaseAdapter {
    private Context mContext;
    private OnDateClickListener mListener;
    private List<UpcomingMeetings> upcomingMeetingsList = new ArrayList<>();
    private int gridWidth, gridHeight;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");

    // Constructor
    public UpcomingMeetingsAdapter(Context c) {
        mContext = c;

        getDisplayMetrics((Activity) c);
    }

    public int getCount() {
        return upcomingMeetingsList.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(final int position, View convertView, ViewGroup parent) {
        View grid;
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            grid = inflater.inflate(R.layout.upcoming_meeting_row, null);
        } else {
            grid = (View) convertView;
        }

        TextView tv_name = (TextView) grid.findViewById(R.id.tv_name);
        TextView tv_day = (TextView) grid.findViewById(R.id.tv_day);
        TextView tv_date = (TextView) grid.findViewById(R.id.tv_date);
        TextView tv_email = (TextView) grid.findViewById(R.id.tv_email);
        TextView tv_cancel = (TextView) grid.findViewById(R.id.tv_cancel);
        TextView tv_reschedule = (TextView) grid.findViewById(R.id.tv_reschedule);
        TextView tv_phone = (TextView) grid.findViewById(R.id.tv_phone);
        TextView tv_timings = (TextView) grid.findViewById(R.id.tv_timings);

        ImageView iv_calender = (ImageView) grid.findViewById(R.id.iv_calender);
        ImageView iv_memo = (ImageView) grid.findViewById(R.id.iv_memo);
        ImageView iv_call = (ImageView) grid.findViewById(R.id.iv_call);
        ImageView iv_map = (ImageView) grid.findViewById(R.id.iv_map);

        try {
            Date meetingDate = new SimpleDateFormat("yyyy-MM-dd").parse(upcomingMeetingsList.get(position).getFdate());

            tv_day.setText("" + new SimpleDateFormat("dd").format(meetingDate));
            tv_date.setText("" + new SimpleDateFormat("MMM yyyy").format(meetingDate));

            tv_name.setText(String.format("%s", upcomingMeetingsList.get(position).getGuest().getContact()));
            tv_timings.setText(String.format("%s - %s", upcomingMeetingsList.get(position).getFromtime(), upcomingMeetingsList.get(position).getTotime()));
            tv_phone.setText(String.format("%s", upcomingMeetingsList.get(position).getGuest().getPhone()));
            tv_email.setText("" + upcomingMeetingsList.get(position).getGuest().getEmail());

            final Date meetingDateTime = dateFormat.parse(upcomingMeetingsList.get(position).getFdate()
                    + " " + upcomingMeetingsList.get(position).getFromtime());

            Date meetingDateTimeEnd = dateFormat.parse(upcomingMeetingsList.get(position).getFdate()
                    + " " + upcomingMeetingsList.get(position).getTotime());

            final Calendar cal = Calendar.getInstance();
            cal.setTime(meetingDateTime);
            cal.add(Calendar.MINUTE, (CONST.TIME_BEFORE_DETECTION * 60));

            if (new Date().compareTo(cal.getTime()) >= 0 && new Date().compareTo(meetingDateTime) <= 0) {
                UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

                if (meetingDetail != null && meetingDetail.getId() == upcomingMeetingsList.get(position).getId() && Paper.book().read(CONST.RUNNING_LATE, false)) {
                    if(isAddedAsRecipient(upcomingMeetingsList.get(position))){
                        tv_reschedule.setVisibility(View.GONE);
                    }else {
                        tv_reschedule.setVisibility(View.VISIBLE);
                    }
                }else if (meetingDetail != null && meetingDetail.getId() == upcomingMeetingsList.get(position).getId() && !Paper.book().read(CONST.RUNNING_LATE, false)){
                    tv_reschedule.setVisibility(View.GONE);
                }
            } else {
                tv_reschedule.setVisibility(View.GONE);
            }

            if (new Date().compareTo(meetingDateTime) <= 0) {

                UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);

                if (meetingDetail != null && meetingDetail.getId() == upcomingMeetingsList.get(position).getId() && (Paper.book().read(CONST.IS_DETECTION_STARTED, false))) {
                    tv_cancel.setVisibility(View.GONE);
                } else {
                    if(isAddedAsRecipient(upcomingMeetingsList.get(position))){
                        tv_cancel.setVisibility(View.GONE);
                    }else {
                        tv_cancel.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                tv_cancel.setVisibility(View.GONE);
            }

            tv_reschedule.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null && (new Date().compareTo(cal.getTime()) >= 0 && new Date().compareTo(meetingDateTime) <= 0)) {
                        mListener.onRescheduleMeeting(position, upcomingMeetingsList.get(position));
                    }
                }
            });

            iv_calender.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onAddToCalender(position, upcomingMeetingsList.get(position));
                    }
                }
            });

            grid.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*if (mListener != null) {
                        mListener.onOpenDetails(position, upcomingMeetingsList.get(position));
                    }*/
                }
            });

            iv_call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onCall(position, upcomingMeetingsList.get(position));
                    }
                }
            });

            iv_map.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onNavigate(position, upcomingMeetingsList.get(position));
                    }
                }
            });

            tv_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onCancelMeeting(position, upcomingMeetingsList.get(position));
                    }
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return grid;
    }

    public interface OnDateClickListener {
        void onDateClick(String date);

        void onAddToCalender(int position, UpcomingMeetings data);

        void onOpenDetails(int position, UpcomingMeetings data);

        void onCall(int position, UpcomingMeetings data);

        void onNavigate(int position, UpcomingMeetings data);

        void onCancelMeeting(int position, UpcomingMeetings data);

        void onRescheduleMeeting(int position, UpcomingMeetings data);
    }

    public void setOnDateClickListener(OnDateClickListener mListenere) {
        this.mListener = mListenere;
    }

    public void notifyDataChange(List<UpcomingMeetings> data) {
        Collections.sort(data, new Comparator<UpcomingMeetings>() {
            public int compare(UpcomingMeetings m1, UpcomingMeetings m2) {
                try {

                    Date date1 = dateFormat.parse(m1.getFdate() + " " + m1.getFromtime());
                    Date date2 = dateFormat.parse(m2.getFdate() + " " + m2.getFromtime());
                    return date1.compareTo(date2);

                }catch (Exception ex){
                    return 0;
                }
            }
        });

        this.upcomingMeetingsList = data;
        notifyDataSetChanged();
    }

    private boolean isAddedAsRecipient(UpcomingMeetings upcomingMeetings) {
        AddRecipientsModel[] recipientsModel = upcomingMeetings.getRecipient();
        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
        if(recipientsModel!=null && recipientsModel.length>0){
            for (AddRecipientsModel data :
                    recipientsModel) {
                if(data.getClient_id().equalsIgnoreCase(""+userData.getId())){

                    return true;
                }
            }
        }
        return false;
    }

    // Method for converting DP/DIP value to pixels
    public int getPixelsFromDPs(Activity activity, float dps) {
        Resources r = activity.getResources();


        int px = (int) (TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dps, r.getDisplayMetrics()));
        return px;
    }

    public void getDisplayMetrics(Activity activity) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        gridHeight = (int) (displayMetrics.heightPixels / 7.2);
        gridWidth = (int) (displayMetrics.widthPixels / 2.2);
    }

}