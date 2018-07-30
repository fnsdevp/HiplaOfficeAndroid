package com.hipla.smartoffice_new.adapter;

/**
 * Created by User on 8/3/2017.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.model.Notification;

import java.util.List;


public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> values;
    private List<Notification> valuesCopy;
    private Context context;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_FOOTER = 2;

    public class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView tv_notification_text, tv_notification_date;
        public View layout;

        public ViewHolder(View v) {
            super(v);
            layout = v;

            tv_notification_text = (TextView) v.findViewById(R.id.tv_notification_text);
            tv_notification_date = (TextView) v.findViewById(R.id.tv_notification_date);
        }
    }

    public void notifyDataChange(List<Notification> data) {
        this.values = data;
        this.valuesCopy = data;
        notifyDataSetChanged();
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NotificationAdapter(Context context, List<Notification> myDataset) {
        this.context = context;
        values = myDataset;
        valuesCopy = myDataset;
    }

    @Override
    public int getItemViewType(int position) {
        if (values.size() > 0 && position == values.size()) {
            return TYPE_FOOTER;
        }
        return TYPE_ITEM;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                             int viewType) {
        if (viewType == TYPE_ITEM) {
            // create a new view
            LayoutInflater inflater = LayoutInflater.from(
                    parent.getContext());
            View v =
                    inflater.inflate(R.layout.notification_row, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v);
            return vh;
        } else {
            LayoutInflater inflater = LayoutInflater.from(
                    parent.getContext());
            View v =
                    inflater.inflate(R.layout.outbox_footer, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        try {
            if (position != values.size()) {
                Notification notificationData = values.get(position);

                holder.tv_notification_text.setText("" + notificationData.getNotificationText());

                holder.tv_notification_date.setText("Date : " + notificationData.getDate());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return values.size();
    }

}