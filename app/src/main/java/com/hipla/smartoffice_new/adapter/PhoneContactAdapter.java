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
import com.hipla.smartoffice_new.model.ContactModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by avishek on 15/11/16.
 */

public class PhoneContactAdapter extends BaseAdapter {

    public List<ContactModel> _data;
    private List<ContactModel> arraylist;
    private Context _c;
    private ViewHolder v;
    private SelectContactToInvite callback;

    public PhoneContactAdapter(List<ContactModel> selectUsers, Context context, SelectContactToInvite mCallback) {
        _data = selectUsers;
        _c = context;
        this.arraylist = new ArrayList<>();
        this.arraylist.addAll(_data);
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
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.contact_info, viewGroup, false);
            Log.e("Inside", "here--------------------------- In view1");
        } else {
            view = convertView;
            Log.e("Inside", "here--------------------------- In view2");
        }

        v = new ViewHolder();

        v.title = (TextView) view.findViewById(R.id.name);
        v.phone = (TextView) view.findViewById(R.id.no);
        v.rowLayout = (LinearLayout) view.findViewById(R.id.contactinfo_ll_parent);

        final ContactModel data = (ContactModel) _data.get(i);
        v.title.setText(data.getName());
        v.phone.setText(data.getPhone());

        v.rowLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (callback != null)
                    callback.addContactToInvite(data);

            }
        });

        view.setTag(data);
        return view;
    }

    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        _data.clear();
        if (charText.length() == 0) {
            _data.addAll(arraylist);
        } else {

            for (ContactModel model : arraylist) {

                if (model.getName().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    _data.add(model);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void notifyAdpater(List<ContactModel> selectUsers) {
        _data = selectUsers;
        arraylist.addAll(_data);
        notifyDataSetChanged();
    }

    public interface SelectContactToInvite {
        void addContactToInvite(ContactModel contactModel);
        void checkIfAllSelected();
    }

    static class ViewHolder {
        TextView title, phone;
        LinearLayout rowLayout;
    }

}

