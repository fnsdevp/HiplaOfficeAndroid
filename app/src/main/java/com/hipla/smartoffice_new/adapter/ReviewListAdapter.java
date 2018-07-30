package com.hipla.smartoffice_new.adapter;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.model.ReviewMessage;

import java.util.List;

/**
 * Created by avishek on 15/11/16.
 */

public class ReviewListAdapter extends BaseAdapter {

    public List<ReviewMessage> _data;
    private Context _c;
    private ViewHolder v;
    private SelectContactToInvite callback;

    public ReviewListAdapter(List<ReviewMessage> selectUsers, Context context) {
        _data = selectUsers;
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
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.post_review, viewGroup, false);
            Log.e("Inside", "here--------------------------- In view1");
        } else {
            view = convertView;
            Log.e("Inside", "here--------------------------- In view2");
        }

        v = new ViewHolder();

        v.tv_name = (TextView) view.findViewById(R.id.tv_name);
        v.tv_message = (TextView) view.findViewById(R.id.tv_message);
        v.rowLayout = (LinearLayout) view.findViewById(R.id.contactinfo_ll_parent);

        final ReviewMessage data = (ReviewMessage) _data.get(i);
        if (data.getUsername() != null)
            v.tv_name.setText("" + data.getUsername().charAt(0));
        else
            v.tv_name.setText("");

        v.tv_message.setText(Html.fromHtml(data.getReview()));

        v.rowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (callback != null)
                    callback.selectedRoom(data);

            }
        });

        view.setTag(data);
        return view;
    }

    public void notifyAdpater(List<ReviewMessage> selectUsers) {
        _data = selectUsers;
        notifyDataSetChanged();
    }

    public interface SelectContactToInvite {
        void selectedRoom(ReviewMessage review);
    }

    static class ViewHolder {
        TextView tv_name, tv_message;
        LinearLayout rowLayout;
    }

}

