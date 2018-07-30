package com.hipla.smartoffice_new.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.model.TimeSlot;
import com.hipla.smartoffice_new.utils.CONST;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.paperdb.Paper;

/**
 * Created by FNSPL on 2/21/2018.
 */

public class FlexibleTimeSlotAdapter extends RecyclerView.Adapter<FlexibleTimeSlotAdapter.ViewHolder> {

    private List<TimeSlot> timeSlotList = new ArrayList<>();
    private List<TimeSlot> unavailableTimeSlotList = new ArrayList<>();
    private Context context;
    private OnTimeSlotClickListener mListener;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private int selectedStartPosition = -1, selectedEndPosition = -1;
    private String selectedDate = "", availableStartTime = "", availableEndTime = "";
    private int clickCount=0;

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tv_time_slot;
        public View layout;

        public ViewHolder(View v) {
            super(v);

            layout = v;
            tv_time_slot = (TextView) v.findViewById(R.id.tv_time_slot);

        }
    }

    public void notifyDataChange(List<TimeSlot> data, String selectedDate, String unavailableStartTime, String unavailableEndTime) {
        this.availableStartTime = unavailableStartTime;
        this.availableEndTime = unavailableEndTime;
        this.selectedDate = selectedDate;
        this.selectedStartPosition = -1;
        this.selectedEndPosition = -1;
        this.unavailableTimeSlotList = data;
        this.clickCount = 0;

        this.timeSlotList.clear();

        getDayTimeSlot();

        notifyDataSetChanged();
    }

    public void clearData(String selectedDate){
        this.availableStartTime = "";
        this.availableEndTime = "";
        this.selectedDate = selectedDate;
        this.selectedStartPosition = -1;
        this.selectedEndPosition = -1;
        this.unavailableTimeSlotList.clear();
        this.clickCount = 0;

        this.timeSlotList.clear();
    }

    public FlexibleTimeSlotAdapter(Context context) {
        this.context = context;

    }


    @Override
    public FlexibleTimeSlotAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(
                parent.getContext());

        View v = inflater.inflate(R.layout.time_slot_row, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        try {
            final Date currentDate;
            if (new SimpleDateFormat("yyyy-MM-dd").format(new Date()).equalsIgnoreCase(selectedDate)) {
                currentDate = new Date();
            } else {
                currentDate = new SimpleDateFormat("yyyy-MM-dd").parse(selectedDate);
            }

            final Date startTime = dateFormat.parse(timeSlotList.get(position).getDate()
                    + " " + timeSlotList.get(position).getFrom());

            final Date endTime = dateFormat.parse(timeSlotList.get(position).getDate()
                    + " " + timeSlotList.get(position).getTo());

            holder.tv_time_slot.setTextColor(context.getResources().getColor(R.color.text_black));

            holder.layout.setBackground(context.getResources().getDrawable(R.drawable.date_selected_white_rounded));

            if (!availableStartTime.isEmpty() && !availableEndTime.isEmpty()) {

                Date availableStart = dateFormat.parse(timeSlotList.get(position).getDate()
                        + " " + availableStartTime);

                Date availableEnd = dateFormat.parse(timeSlotList.get(position).getDate()
                        + " " + availableEndTime);

                if (startTime.compareTo(availableStart) >= 0 && startTime.compareTo(availableEnd) < 0) {
                    if (currentDate.compareTo(startTime) < 0) {
                        holder.layout.setBackground(context.getResources().getDrawable(R.drawable.date_selected_white_rounded));
                    }
                } else {
                    holder.layout.setBackground(context.getResources().getDrawable(R.drawable.normal_card_rounded));
                }

            }

            if (checkForAvaibility(position, currentDate, startTime)) {
                holder.layout.setBackground(context.getResources().getDrawable(R.drawable.normal_card_rounded));
            }

            if (selectedStartPosition != -1 && selectedStartPosition == position) {

                holder.layout.setBackground(context.getResources().getDrawable(R.drawable.date_selected_rounded));
                holder.tv_time_slot.setTextColor(context.getResources().getColor(R.color.text_white));
            }else if(selectedEndPosition != -1 && selectedEndPosition>=position && position>selectedStartPosition){

                holder.layout.setBackground(context.getResources().getDrawable(R.drawable.date_selected_rounded));
                holder.tv_time_slot.setTextColor(context.getResources().getColor(R.color.text_white));
            }

            holder.tv_time_slot.setText(timeSlotList.get(position).getFrom() + " - "
                    + timeSlotList.get(position).getTo());

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        if (availableStartTime.isEmpty() && availableEndTime.isEmpty()) {

                            if (!checkForAvaibility(position, currentDate, startTime)) {
                                clickCount++;

                                if (clickCount % 2 == 0) {

                                    selectedEndPosition = position;

                                    if (selectedStartPosition > selectedEndPosition) {
                                        int j = selectedEndPosition;
                                        selectedEndPosition = selectedStartPosition;
                                        selectedStartPosition = j;
                                    }

                                    if (mListener != null && selectedStartPosition != selectedEndPosition) {
                                        mListener.onTimeSlotClick(timeSlotList.get(selectedStartPosition),
                                                timeSlotList.get(selectedEndPosition));
                                    }

                                } else {
                                    selectedStartPosition = position;
                                    selectedEndPosition = -1;

                                    if (mListener != null && selectedStartPosition != selectedEndPosition) {
                                        mListener.onTimeSlotClick(timeSlotList.get(selectedStartPosition), null);
                                    }
                                }

                                notifyDataSetChanged();
                            }

                        } else {

                            Date availableStart = dateFormat.parse(timeSlotList.get(position).getDate()
                                    + " " + availableStartTime);

                            Date availableEnd = dateFormat.parse(timeSlotList.get(position).getDate()
                                    + " " + availableEndTime);

                            if (startTime.compareTo(availableStart) >= 0 && startTime.compareTo(availableEnd) < 0) {
                                if (currentDate.compareTo(startTime) < 0) {

                                    if (mListener != null) {

                                        if (!checkForAvaibility(position, currentDate, startTime)) {
                                            clickCount++;

                                            if(clickCount%2==0) {

                                                selectedEndPosition = position;

                                                if (selectedStartPosition > selectedEndPosition) {
                                                    int j = selectedEndPosition;
                                                    selectedEndPosition = selectedStartPosition;
                                                    selectedStartPosition = j;
                                                }

                                                mListener.onTimeSlotClick(timeSlotList.get(selectedStartPosition), timeSlotList.get(selectedEndPosition));

                                            }else{
                                                selectedStartPosition = position;
                                                selectedEndPosition = -1;

                                                if (mListener != null && selectedStartPosition!=selectedEndPosition) {
                                                    mListener.onTimeSlotClick(timeSlotList.get(selectedStartPosition), null);
                                                }
                                            }

                                            notifyDataSetChanged();
                                        }

                                    }

                                }
                            }

                        }
                    } catch (Exception ex) {

                    }

                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean checkForAvaibility(int position, Date currentDate, Date startTime) throws ParseException {
        for (TimeSlot timeSlot :
                unavailableTimeSlotList) {

            Date unavailableStart = dateFormat.parse(timeSlotList.get(position).getDate()
                    + " " + timeSlot.getFrom());

            Date unavailableEnd = dateFormat.parse(timeSlotList.get(position).getDate()
                    + " " + timeSlot.getTo());

            if (startTime.compareTo(unavailableStart) >= 0 && startTime.compareTo(unavailableEnd) < 0) {
                if (currentDate.compareTo(startTime) < 0) {
                    return true;
                }
            }

        }

        return false;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return timeSlotList.size();
    }

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(TimeSlot startTimeSlot, TimeSlot stopTimeSlot);
    }

    public void setOnTimeSlotClickListener(OnTimeSlotClickListener mListener) {
        this.mListener = mListener;
    }

    public void getDayTimeSlot() {
        try {
            Date startTime, endTime;

            if(availableStartTime.isEmpty() && availableEndTime.isEmpty()) {
                startTime = dateFormat.parse(selectedDate + " " + CONST.START_TIME);
                endTime = dateFormat.parse(selectedDate + " " + CONST.END_TIME);
            }else{

                startTime = dateFormat.parse(selectedDate + " " + CONST.START_TIME);
                endTime = dateFormat.parse(selectedDate + " " + CONST.END_TIME);

                Date tempStartTime = dateFormat.parse(selectedDate + " " + availableStartTime);
                Date tempEndTime = dateFormat.parse(selectedDate + " " + availableEndTime);

                if(tempStartTime.compareTo(startTime)<0){
                    startTime = tempStartTime;
                }

                if(tempEndTime.compareTo(startTime)>0){
                    endTime = tempEndTime;
                }
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(startTime);

            while (cal.getTime().compareTo(endTime) < 0) {
                TimeSlot timeSlot = new TimeSlot();
                timeSlot.setDate(selectedDate);

                Date slotStartTime = cal.getTime();
                timeSlot.setFrom(new SimpleDateFormat("hh:mm a").format(slotStartTime));

                if(Paper.book().read(CONST.IS_HALF_HOUR_SLOT, true)) {
                    cal.add(Calendar.MINUTE, 30);
                }else{
                    cal.add(Calendar.MINUTE, 60);
                }

                Date slotEndTime = cal.getTime();
                timeSlot.setTo(new SimpleDateFormat("hh:mm a").format(slotEndTime));

                timeSlotList.add(timeSlot);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
