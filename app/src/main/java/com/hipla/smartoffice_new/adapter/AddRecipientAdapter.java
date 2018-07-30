package com.hipla.smartoffice_new.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.model.AddRecipientsModel;

import java.util.List;


public class AddRecipientAdapter extends RecyclerView.Adapter<AddRecipientAdapter.MyHolderView> {

    private List<AddRecipientsModel> recipientList;
    private AddRecipientAdapter.ManageRecipientCallback manageRowCallback;


    public AddRecipientAdapter(AddRecipientAdapter.ManageRecipientCallback callback, List<AddRecipientsModel> recipientList) {
        this.recipientList = recipientList;
        this.manageRowCallback = callback;
    }

    @Override
    public MyHolderView onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new MyHolderView(inflater.inflate(R.layout.add_reciepients_row, parent, false));
    }

    @Override
    public void onBindViewHolder(MyHolderView holder, int position) {

        AddRecipientsModel model = recipientList.get(position);
        holder.name.setText(model.getClient_name());
        int guestNo = position + 1;
        holder.guestno.setText("Guest " + guestNo + " : ");
    }

    @Override
    public int getItemCount() {
        return recipientList.size();
    }

    public class MyHolderView extends RecyclerView.ViewHolder {

        public TextView guestno, name;
        public ImageView iv_minus_recipients;

        public MyHolderView(View itemView) {
            super(itemView);
            guestno = itemView.findViewById(R.id.tv_name);
            name = itemView.findViewById(R.id.tv_selected_name);

            iv_minus_recipients = itemView.findViewById(R.id.iv_minus_recipients);
            iv_minus_recipients.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        remove(Integer.parseInt(guestno.getText().toString().substring(6, guestno.getText().toString().indexOf(":") - 1)) - 1);
                    } catch (Exception e) {
                    }
                }
            });
        }
    }


    public interface ManageRecipientCallback {

        public void onAddRecipient();

        public void onRemoveRecipient(int position);

    }


    public void add() {
        Log.d("jos", "add: ");
        manageRowCallback.onAddRecipient();
    }

    public void remove(int position) {
        Log.d("jos", "" + position);
        // if (position > 0)
        manageRowCallback.onRemoveRecipient(position);

    }

    public void notifyDataChange(List<AddRecipientsModel> recipientList) {
        this.recipientList = recipientList;
        notifyDataSetChanged();
    }

}
