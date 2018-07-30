package com.hipla.smartoffice_new.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.BottomSheetDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.activity.BaseActivity;
import com.hipla.smartoffice_new.adapter.AvailableDatesAdapter;
import com.hipla.smartoffice_new.adapter.AvailableRoomsAdapter;
import com.hipla.smartoffice_new.model.ContactModel;
import com.hipla.smartoffice_new.model.RoomData;
import com.hipla.smartoffice_new.model.TimeSlot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by FNSPL on 1/14/2018.
 */

public class Dialogs {

    public static boolean isDialogShowing = false;
    private static String[] weekDay = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug", "Sep","Oct","Nov","Dec"};

    public static void dialogShowRooms(Context context,List<RoomData> roomDataList,
                                        final OnOptionSelect _callback) {
        View modalbottomsheet = ((Activity)context).getLayoutInflater().inflate(R.layout.dialog_all_rooms, null);

       final BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(modalbottomsheet);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        //dialog.show();

        ListView roomsList = (ListView) dialog.findViewById(R.id.listview_rooms);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);

        AvailableRoomsAdapter mAdapter = new AvailableRoomsAdapter(roomDataList, context, new AvailableRoomsAdapter.SelectContactToInvite() {
            @Override
            public void selectedRoom(RoomData roomData) {

                dialog.dismiss();
                isDialogShowing = false;

                if(_callback!=null){
                    _callback.setRowClick(roomData);
                }

            }
        });

        roomsList.setAdapter(mAdapter);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                isDialogShowing = false;
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDialogShowing = false;
            }
        });

        if (!isDialogShowing)
            dialog.show();

        isDialogShowing=true;
    }

    public static Dialog dialogNoConnection(final Context context,
                                            final OnCallback _callback) {

        View modalbottomsheet = ((Activity)context).getLayoutInflater().inflate(R.layout.dialog_no_connection, null);

        final BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(modalbottomsheet);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);
        TextView tv_try_again = (TextView) dialog.findViewById(R.id.tv_try_again);

        tv_try_again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((BaseActivity)context).isDataAvailable()){
                    dialog.dismiss();
                }
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDialogShowing = false;
            }
        });

        if (!isDialogShowing)
            dialog.show();

        isDialogShowing=true;

        return dialog;
    }


    public static void dialogFetchImage(Context context,
                                       final OnImageSelect _callback) {

        View modalbottomsheet = ((Activity)context).getLayoutInflater().inflate(R.layout.dialog_fetch_image, null);

        final BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(modalbottomsheet);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);

        TextView tv_open_gallery = (TextView) dialog.findViewById(R.id.tv_open_gallery);
        TextView tv_open_camera = (TextView) dialog.findViewById(R.id.tv_open_camera);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                isDialogShowing = false;
            }
        });

        tv_open_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                _callback.openCamera();
                isDialogShowing = false;
            }
        });

        tv_open_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                _callback.openGallery();
                isDialogShowing = false;
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDialogShowing = false;
            }
        });

        if (!isDialogShowing)
            dialog.show();

        isDialogShowing=true;
    }

    public static void dialogChangePassword(final Context context,
                                            final OnCallback _callback) {

        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_change_passord);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        dialog.setCanceledOnTouchOutside(false);

        TextView btn_submit = dialog.findViewById(R.id.btn_submit);
        TextView btn_cancel = dialog.findViewById(R.id.btn_cancel);
        final EditText et_password = dialog.findViewById(R.id.et_password);

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                isDialogShowing = false;

                if(_callback!=null && et_password.getText().toString().trim().length()>6){
                    _callback.onSubmit(et_password.getText().toString().trim());
                }else {
                    Toast.makeText(context, context.getResources().getString(R.string.check_your_password), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                isDialogShowing = false;
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDialogShowing = false;
            }
        });

        if (!isDialogShowing)
            dialog.show();

        isDialogShowing=true;
    }

    public static void dialogShowDates(Context context,String endTime,String startDate, String endDate,
                                       final OnDateSelect _callback) {

        View modalbottomsheet = ((Activity)context).getLayoutInflater().inflate(R.layout.dialog_meeting_dates, null);

        final BottomSheetDialog dialog = new BottomSheetDialog(context);
        dialog.setContentView(modalbottomsheet);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);


        ListView roomsList = (ListView) dialog.findViewById(R.id.listview_rooms);
        Button btn_cancel = (Button) dialog.findViewById(R.id.btn_cancel);

        List<String> result = new ArrayList<>();
        try {
            Calendar start = Calendar.getInstance();
            start.setTime(new SimpleDateFormat("yyyy-MM-dd hh:mm a").parse(startDate+" "+endTime));
            Calendar end = Calendar.getInstance();
            end.setTime(new SimpleDateFormat("yyyy-MM-dd hh:mm a").parse(endDate+" "+endTime));
            end.add(Calendar.DAY_OF_YEAR, 1); //Add 1 day to endDate to make sure endDate is included into the final list
            while (start.before(end)) {
                if(new Date().compareTo(start.getTime())<0) {
                    result.add(new SimpleDateFormat("yyyy-MM-dd").format(start.getTime()));
                }
                start.add(Calendar.DAY_OF_YEAR, 1);
            }
        }catch (Exception ex){

        }

        AvailableDatesAdapter mAdapter = new AvailableDatesAdapter(result, context, new AvailableDatesAdapter.SelectContactToInvite() {
            @Override
            public void selectedRoom(String date) {

                dialog.dismiss();
                isDialogShowing = false;

                if(_callback!=null){
                    _callback.setRowClick(date);
                }

            }
        });

        roomsList.setAdapter(mAdapter);

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                isDialogShowing = false;
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDialogShowing = false;
            }
        });

        if (!isDialogShowing)
            dialog.show();

        isDialogShowing=true;
    }

    public static void dialogFixedMeetingSuccess(Context context,TimeSlot timeSlot, ContactModel contact,
                                          final OnMeetingConfirm _callback){
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_success_fixed);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        dialog.setCanceledOnTouchOutside(false);

        TextView tv_date_week = (TextView) dialog.findViewById(R.id.tv_date_week);
        TextView tv_date_day = (TextView) dialog.findViewById(R.id.tv_date_day);
        TextView tv_date_month = (TextView) dialog.findViewById(R.id.tv_date_month);
        TextView tv_meeting_with = (TextView) dialog.findViewById(R.id.tv_meeting_with);

        ImageView btn_ok = (ImageView) dialog.findViewById(R.id.btn_ok);

        try {
            tv_meeting_with.setText(String.format(context.getString(R.string.meeting_with), contact.getName()));

            Date meetingDate = new SimpleDateFormat("yyyy-MM-dd").parse(timeSlot.getDate());
            Calendar cal = Calendar.getInstance();
            cal.setTime(meetingDate);

            tv_date_week.setText(String.format("%s", weekDay[cal.get(Calendar.DAY_OF_WEEK)-1]));
            tv_date_day.setText(String.format("%s", cal.get(Calendar.DAY_OF_MONTH)));
            tv_date_month.setText(String.format("%s", months[cal.get(Calendar.MONTH)]));

        }catch (Exception ex){

        }

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                if(_callback!=null){
                    _callback.onMeetingOk();
                }

                isDialogShowing = false;
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDialogShowing = false;
            }
        });

        if (!isDialogShowing)
            dialog.show();

        isDialogShowing=true;
    }

    public static void dialogFlexibleMeetingSuccess(Context context,String startDate, String endDate, ContactModel contact,
                                                 final OnMeetingConfirm _callback){
        final Dialog dialog = new Dialog(context, R.style.dialog);
        dialog.setContentView(R.layout.dialog_success_flexible);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
        dialog.getWindow().setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        dialog.setCanceledOnTouchOutside(false);

        TextView tv_date_week = (TextView) dialog.findViewById(R.id.tv_date_week);
        TextView tv_date_day = (TextView) dialog.findViewById(R.id.tv_date_day);
        TextView tv_date_month = (TextView) dialog.findViewById(R.id.tv_date_month);
        TextView tv_meeting_with = (TextView) dialog.findViewById(R.id.tv_meeting_with);
        TextView tv_date_week_end = (TextView) dialog.findViewById(R.id.tv_date_week_end);
        TextView tv_date_day_end = (TextView) dialog.findViewById(R.id.tv_date_day_end);
        TextView tv_date_month_end = (TextView) dialog.findViewById(R.id.tv_date_month_end);

        ImageView btn_ok = (ImageView) dialog.findViewById(R.id.btn_ok);

        try {
            tv_meeting_with.setText(String.format(context.getString(R.string.meeting_with), contact.getName()));

            Date meetingDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(meetingDate);

            tv_date_week.setText(String.format("%s", weekDay[cal.get(Calendar.DAY_OF_WEEK)-1]));
            tv_date_day.setText(String.format("%s", cal.get(Calendar.DAY_OF_MONTH)));
            tv_date_month.setText(String.format("%s", months[cal.get(Calendar.MONTH)]));

            Date meetingDateEnd = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(meetingDateEnd);

            tv_date_week_end.setText(String.format("%s", weekDay[calEnd.get(Calendar.DAY_OF_WEEK)-1]));
            tv_date_day_end.setText(String.format("%s", calEnd.get(Calendar.DAY_OF_MONTH)));
            tv_date_month_end.setText(String.format("%s", months[calEnd.get(Calendar.MONTH)]));

        }catch (Exception ex){

        }

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                if(_callback!=null){
                    _callback.onMeetingOk();
                }

                isDialogShowing = false;
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                isDialogShowing = false;
            }
        });

        if (!isDialogShowing)
            dialog.show();

        isDialogShowing=true;
    }


    public interface OnOptionSelect {
        void setRowClick(RoomData roomData);
    }

    public interface OnImageSelect {
        void openCamera();
        void openGallery();
    }

    public interface OnCallback {
        void onSubmit(String password);
    }

    public interface OnDateSelect {
        void setRowClick(String date);
    }


    public interface OnMeetingConfirm{
        void onMeetingOk();
    }
}
