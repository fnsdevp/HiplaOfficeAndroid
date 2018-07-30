package com.hipla.smartoffice_new.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hipla.smartoffice_new.R;

import java.util.List;

/**
 * Created by avishek on 15/11/16.
 */

public class AvailableDatesAdapter extends BaseAdapter {

    public List<String> _data;
    private Context _c;
    private ViewHolder v;
    private SelectContactToInvite callback;

    public AvailableDatesAdapter(List<String> selectUsers, Context context, SelectContactToInvite mCallback) {
        _data = selectUsers;
        _c = context;
        this.callback = mCallback;
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
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_room, viewGroup, false);
            Log.e("Inside", "here--------------------------- In view1");
        } else {
            view = convertView;
            Log.e("Inside", "here--------------------------- In view2");
        }

        v = new ViewHolder();

        v.title = (TextView) view.findViewById(R.id.name);
        v.rowLayout = (LinearLayout) view.findViewById(R.id.contactinfo_ll_parent);

        final String date = (String) _data.get(i);
        v.title.setText(date);

        v.rowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (callback != null)
                    callback.selectedRoom(_data.get(i));

            }
        });

        view.setTag(date);
        return view;
    }

    public void notifyAdpater(List<String> selectUsers) {
        _data = selectUsers;
        notifyDataSetChanged();
    }

    public interface SelectContactToInvite {
        void selectedRoom(String date);
    }

    static class ViewHolder {
        TextView title;
        LinearLayout rowLayout;
    }

}

