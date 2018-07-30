package com.hipla.smartoffice_new.adapter;

import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.model.Availibility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by avishek on 15/11/16.
 */

public class SetAvailibilityListAdapter extends BaseAdapter {

    public List<Availibility> _data;
    public List<Availibility> _dataCopy;
    private Context _c;
    private ViewHolder v;
    private OnCheckedClick callback;

    public SetAvailibilityListAdapter(List<Availibility> selectUsers, Context context) {
        _data = selectUsers;
        _dataCopy = selectUsers;
        _c = context;
    }

    @Override
    public int getCount() {
        return _data.size();
    }

    @Override
    public Object getItem(int i) {
        return _data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_set_availibility, viewGroup, false);
            Log.e("Inside", "here--------------------------- In view1");
        } else {
            view = convertView;
            Log.e("Inside", "here--------------------------- In view2");
        }

        v = new ViewHolder();

        v.tv_day = (TextView) view.findViewById(R.id.tv_day);
        v.tv_date = (TextView) view.findViewById(R.id.tv_date);
        v.tv_to_time = (TextView) view.findViewById(R.id.tv_to_time);
        v.tv_from_time = (TextView) view.findViewById(R.id.tv_from_time);
        v.ll_from_time = (LinearLayout) view.findViewById(R.id.ll_from_time);
        v.sw_switch = (Switch) view.findViewById(R.id.sw_switch);
        v.ll_to_time = (LinearLayout) view.findViewById(R.id.ll_to_time);
        v.rowLayout = (RelativeLayout) view.findViewById(R.id.rl_layout);

        final Availibility data = _data.get(i);

        v.tv_date.setText("" + data.getShowingDate());
        v.tv_day.setText("" + data.getDay());
        v.tv_from_time.setText("" + data.getFrom_time());
        v.tv_to_time.setText("" + data.getTo_time());

        v.sw_switch.setTag((Integer) i);

        if (_data.get((int) v.sw_switch.getTag()).getStatus().equalsIgnoreCase("N")) {
            v.sw_switch.setChecked(false);
        } else {
            v.sw_switch.setChecked(true);
        }

        v.tv_from_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                try {
                    Calendar mcurrentTime = Calendar.getInstance();
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(data.getDate());
                    mcurrentTime.setTime(date);
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    final int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(_c, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                            String AM_PM, minutes="", hours ="";
                            if (selectedHour < 12) {
                                AM_PM = "AM";
                            } else {
                                AM_PM = "PM";
                                selectedHour = selectedHour-12;
                            }

                            if(selectedMinute>9){
                                minutes = ""+selectedMinute;
                            }else{
                                minutes = "0"+selectedMinute;
                            }

                            if(selectedHour>9){
                                hours = ""+selectedHour;
                            }else{
                                hours = "0"+selectedHour;
                            }

                            _data.get(i).setFrom_time(hours + ":" + minutes + " " + AM_PM);

                            if (!_data.get(i).getTo_time().isEmpty()) {
                                callback.selectTime(_data.get(i), "Y");
                            }
                            notifyDataSetChanged();

                        }
                    }, hour, minute, false);//No 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();

                } catch (Exception ex) {

                }
            }
        });

        v.tv_to_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                try {
                    Calendar mcurrentTime = Calendar.getInstance();
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(data.getDate());
                    mcurrentTime.setTime(date);
                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int minute = mcurrentTime.get(Calendar.MINUTE);
                    TimePickerDialog mTimePicker;
                    mTimePicker = new TimePickerDialog(_c, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

                            String AM_PM, minutes="", hours = "";
                            if (selectedHour < 12) {
                                AM_PM = "AM";
                            } else {
                                AM_PM = "PM";
                                selectedHour = selectedHour-12;
                            }

                            if(selectedMinute>9){
                                minutes = ""+selectedMinute;
                            }else{
                                minutes = "0"+selectedMinute;
                            }

                            if(selectedHour>9){
                                hours = ""+selectedHour;
                            }else{
                                hours = "0"+selectedHour;
                            }

                            _data.get(i).setTo_time(hours + ":" + minutes + " " + AM_PM);

                            if (!_data.get(i).getFrom_time().isEmpty()) {
                                callback.selectTime(_data.get(i), "Y");
                            }

                            notifyDataSetChanged();

                        }
                    }, hour, minute, false);//No 24 hour time
                    mTimePicker.setTitle("Select Time");
                    mTimePicker.show();

                } catch (Exception ex) {

                }
            }
        });

        v.sw_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (callback != null) {
                    if (isChecked) {


                    } else {

                    }
                }
            }
        });

        v.sw_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (callback != null) {
                    if(_data.get(i).getStatus().equalsIgnoreCase("N")) {
                        if (_data.get(i).getFrom_time().isEmpty() &&
                                _data.get(i).getTo_time().isEmpty()) {

                            _data.get(i).setFrom_time("09:00 AM");
                            _data.get(i).setTo_time("07:00 PM");
                            _data.get(i).setStatus("Y");

                            callback.selectTime(_data.get(i), "Y");

                            notifyDataSetChanged();
                        } else {

                            _data.get(i).setFrom_time(v.tv_from_time.getText().toString().trim());
                            _data.get(i).setTo_time(v.tv_to_time.getText().toString().trim());
                            _data.get(i).setStatus("Y");

                            callback.selectTime(_data.get(i), "Y");

                            notifyDataSetChanged();
                        }
                    }else {
                        if (!_data.get(i).getFrom_time().isEmpty() &&
                                !_data.get(i).getTo_time().isEmpty()) {

                            _data.get(i).setStatus("N");

                            callback.selectTime(_data.get(i), "N");

                            notifyDataSetChanged();
                        }
                    }
                }

            }
        });

        view.setTag(data);
        return view;
    }

    public void notifyAdpater(List<Availibility> selectUsers) {
        _data = selectUsers;
        _dataCopy = selectUsers;
        notifyDataSetChanged();
    }

    public interface OnCheckedClick {
        void selectTime(Availibility availibility, String status);
    }

    public void setOnCheckedClick(OnCheckedClick callback) {
        this.callback = callback;
    }

    static class ViewHolder {
        TextView tv_day, tv_date, tv_to_time, tv_from_time;
        RelativeLayout rowLayout;
        LinearLayout ll_from_time, ll_to_time;
        Switch sw_switch;
    }

    public void filter(String text) {
        _data = new ArrayList<>();
        if (text.isEmpty()) {
            _data.addAll(_dataCopy);
        } else {
            text = text.toLowerCase();
            for (Availibility item : _dataCopy) {
                /*if(item.getStatus().toLowerCase().equalsIgnoreCase(text)){
                    _data.add(item);
                }*/
            }
        }
        notifyDataSetChanged();
    }

}

