package com.hipla.smartoffice_new.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hipla.smartoffice_new.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by FNSPL on 8/21/2017.
 */

public class WeekListAdapter extends BaseAdapter {
    private Context mContext;
    private OnDateClickListener mListener;
    private int selectedSubjectPosition = 0;
    private String[] weekDay = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private String[] months = new String[7];
    private String[] weekDates = new String[7];
    private int[] daysIndex = new int[7];
    private int[] dayOfMonth = new int[7];
    private String updateDate="";
    private boolean notSelectable= true;

    // Constructor
    public WeekListAdapter(Context c) {
        mContext = c;
        getAllWeekAddress();
    }

    public WeekListAdapter(Context c, boolean notSelectable) {
        mContext = c;
        this.notSelectable = notSelectable;

        getAllWeekAddress();
    }

    public int getCount() {
        return dayOfMonth.length;
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
            grid = new View(mContext);
            grid = inflater.inflate(R.layout.date_row, null);
        } else {
            grid = (View) convertView;
        }

        final RelativeLayout rl_subject = (RelativeLayout) grid.findViewById(R.id.rl_item);
        TextView tv_date_week = (TextView) grid.findViewById(R.id.tv_date_week);
        TextView tv_date_month = (TextView) grid.findViewById(R.id.tv_date_month);
        TextView tv_date_day = (TextView) grid.findViewById(R.id.tv_date_day);

        tv_date_week.setText(weekDay[daysIndex[position]-1]);
        tv_date_day.setText(""+dayOfMonth[position]);
        tv_date_month.setText(""+months[position]);

        if (position == selectedSubjectPosition ) {

            rl_subject.setBackground(mContext.getResources().getDrawable(R.drawable.date_selected));

            tv_date_day.setTextColor(mContext.getResources().getColor(R.color.text_white));
            tv_date_week.setTextColor(mContext.getResources().getColor(R.color.text_white));
            tv_date_month.setTextColor(mContext.getResources().getColor(R.color.text_white));

        } else if(weekDay[daysIndex[position]-1].equalsIgnoreCase("Sat")
                || weekDay[daysIndex[position]-1].equalsIgnoreCase("Sun")){

            rl_subject.setBackground(mContext.getResources().getDrawable(R.drawable.date_selected_white));

            tv_date_day.setTextColor(mContext.getResources().getColor(R.color.text_red));
            tv_date_week.setTextColor(mContext.getResources().getColor(R.color.text_red));
            tv_date_month.setTextColor(mContext.getResources().getColor(R.color.text_red));

        } else {

            rl_subject.setBackground(mContext.getResources().getDrawable(R.drawable.normal_card));

            tv_date_day.setTextColor(mContext.getResources().getColor(R.color.text_white));
            tv_date_week.setTextColor(mContext.getResources().getColor(R.color.text_white));
            tv_date_month.setTextColor(mContext.getResources().getColor(R.color.text_white));

        }

        rl_subject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    /*if (!weekDay[daysIndex[position] - 1].equalsIgnoreCase("Sat")
                            && !weekDay[daysIndex[position] - 1].equalsIgnoreCase("Sun")) {
*/
                    if(notSelectable) {
                        rl_subject.setBackground(mContext.getResources().getDrawable(R.drawable.date_selected));

                        selectedSubjectPosition = position;

                        if (mListener != null) {
                            Date date = new SimpleDateFormat("yyyy-M-d").parse(weekDates[position]);

                            mListener.onDateClick(new SimpleDateFormat("yyyy-MM-dd").format(date));
                        }

                        notifyDataSetChanged();
                    }
                    //}
                }catch (Exception ex){

                }

            }
        });

        return grid;
    }

    public interface OnDateClickListener {
        void onDateClick(String date);
    }

    public void setOnDateClickListener(OnDateClickListener mListenere) {
        this.mListener = mListenere;
    }

    public void notifyDataChange(String date) {
        notifyDataSetChanged();
    }

    public void getAllWeekAddress() {

        Calendar c1 = Calendar.getInstance();
        int dayOfYear = c1.get(Calendar.DAY_OF_YEAR);

        for (int i = 0; i < 7; i++) {
            //first day of week

            c1.set(Calendar.DAY_OF_YEAR, dayOfYear+i);

            daysIndex[i] = c1.get(Calendar.DAY_OF_WEEK);

            months[i] = new SimpleDateFormat("MMM").format(c1.getTime());

            int year1 = c1.get(Calendar.YEAR);
            int month1 = c1.get(Calendar.MONTH) + 1;
            int day1 = c1.get(Calendar.DAY_OF_MONTH);

            dayOfMonth[i] = day1;
            weekDates[i] = year1 + "-" + month1 + "-" + day1;

            if (new SimpleDateFormat("yyyy-M-d").format(new Date()).equalsIgnoreCase(weekDates[i])) {
                selectedSubjectPosition = i;
            }

        }

    }

    public void getAllWeekAddress(String updateDateString) {
        try {
            Date updateDate = new SimpleDateFormat("yyyy-MM-dd").parse(updateDateString);

            Calendar c1 = Calendar.getInstance();
            c1.setTime(updateDate);

            int dayOfYear = c1.get(Calendar.DAY_OF_YEAR);

            for (int i = 0; i < 7; i++) {
                //first day of week

                c1.set(Calendar.DAY_OF_YEAR, dayOfYear + i);

                daysIndex[i] = c1.get(Calendar.DAY_OF_WEEK);

                months[i] = new SimpleDateFormat("MMM").format(c1.getTime());

                int year1 = c1.get(Calendar.YEAR);
                int month1 = c1.get(Calendar.MONTH) + 1;
                int day1 = c1.get(Calendar.DAY_OF_MONTH);

                dayOfMonth[i] = day1;
                weekDates[i] = year1 + "-" + month1 + "-" + day1;

                if (new SimpleDateFormat("yyyy-M-d").format(updateDate).equalsIgnoreCase(weekDates[i])) {
                    selectedSubjectPosition = i;
                }

            }

            notifyDataSetChanged();
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}