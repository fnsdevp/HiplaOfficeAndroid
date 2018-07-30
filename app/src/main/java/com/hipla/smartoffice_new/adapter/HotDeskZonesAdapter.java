package com.hipla.smartoffice_new.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.model.HotDeskZone;

import java.util.List;

/**
 * Created by avishek on 15/11/16.
 */

public class HotDeskZonesAdapter extends BaseAdapter {

    public List<HotDeskZone> _data;
    private Context _c;
    private ViewHolder v;
    private SelectContactToInvite callback;
    private int selectedPos=-1;

    public HotDeskZonesAdapter(List<HotDeskZone> selectUsers, Context context, SelectContactToInvite mCallback) {
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
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_hot_desk_zone, viewGroup, false);
            Log.e("Inside", "here--------------------------- In view1");
        } else {
            view = convertView;
            Log.e("Inside", "here--------------------------- In view2");
        }

        v = new ViewHolder();

        v.tv_desk = (TextView) view.findViewById(R.id.tv_desk);
        v.rl_hot_desk = (RelativeLayout) view.findViewById(R.id.rl_hot_desk);

        final HotDeskZone data = (HotDeskZone) _data.get(i);
        v.tv_desk.setText(data.getZone_name());

        if(selectedPos!=-1 && selectedPos==i){
            v.rl_hot_desk.setBackgroundResource(R.drawable.white_border_blue);
            v.tv_desk .setTextColor(_c.getResources().getColor(R.color.white));
        }else{
            v.rl_hot_desk.setBackgroundResource(R.drawable.white_border);
            v.tv_desk .setTextColor(_c.getResources().getColor(R.color.text_light_gray));
        }

        v.rl_hot_desk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                selectedPos = i;

                if (callback != null)
                    callback.selectZone(data);

                notifyDataSetChanged();

            }
        });

        view.setTag(data);
        return view;
    }

    public void notifyAdpater(List<HotDeskZone> selectUsers, int selectedPos) {
        this.selectedPos = selectedPos;
        _data = selectUsers;
        notifyDataSetChanged();
    }

    public void resetPos(){
        selectedPos = -1;
        notifyDataSetChanged();
    }

    public interface SelectContactToInvite {
        void selectZone(HotDeskZone roomData);
    }

    static class ViewHolder {
        TextView tv_desk;
        RelativeLayout rl_hot_desk;
    }

}

