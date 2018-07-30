package com.hipla.smartoffice_new.fragment;


import android.app.Dialog;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Toast;

import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.adapter.PhoneContactAdapter;
import com.hipla.smartoffice_new.model.ContactModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectContactFragment extends DialogFragment implements PhoneContactAdapter.SelectContactToInvite {

    private View mView;
    List<ContactModel> selectUsers = new ArrayList<>();
    // Contact List
    ListView listView;
    // Cursor to load contacts list
    Cursor phones;
    // Pop up
    ContentResolver resolver;
    SearchView search;
    private PhoneContactAdapter adapter;
    private OnContactSelected mListener;

    public SelectContactFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.activity_show_contact, container, false);
        initView(mView);
        return mView;
    }

    private void initView(View mView) {
        search = (SearchView) mView.findViewById(R.id.searchView);
        listView = (ListView) mView.findViewById(R.id.contacts_list);

        adapter = new PhoneContactAdapter(selectUsers, getActivity(), this);
        listView.setAdapter(adapter);

        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.e("hasFocus", "" + hasFocus);
            }
        });

        //*** setOnQueryTextListener ***
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO Auto-generated method stub

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO Auto-generated method stub
                if (adapter != null)
                    adapter.filter(newText);

                return false;
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                search.setIconified(false);
                search.setQueryHint(Html.fromHtml("<font color = #bbbcbc>Search...</font>"));

            }
        });

        phones = getActivity().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                        + " ASC");

        if (phones != null) {
            Log.e("count", "" + phones.getCount());
            if (phones.getCount() == 0) {
                Toast.makeText(getActivity(), "No contacts in your contact list.", Toast.LENGTH_LONG).show();
            }

            HashMap<String, ContactModel> data = new HashMap<>();

            while (phones.moveToNext()) {
                String id = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                ContactModel selectUser = new ContactModel();
                selectUser.setName(name);
                selectUser.setPhone(phoneNumber);
                data.put(phoneNumber,selectUser);
            }

            for (Map.Entry<String, ContactModel> wp : data.entrySet()) {
                String key = wp.getKey();
                ContactModel value = wp.getValue();

                selectUsers.add(value);
            }

            adapter.notifyAdpater(selectUsers);

        } else {
            Log.e("Cursor close 1", "----------------");
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //super.onCreateDialog(savedInstanceState);

        Dialog dialog = new Dialog(getActivity());
        final RelativeLayout root = new RelativeLayout(getActivity());
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(root);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        return dialog;
    }


    @Override
    public void addContactToInvite(ContactModel contactModel) {
        dismiss();
        if(mListener!=null){
            mListener.onContactSelected(contactModel);
        }
    }

    @Override
    public void checkIfAllSelected() {

    }

    public interface OnContactSelected{
        void onContactSelected(ContactModel contactModel);
    }

    public void setOnContactSelected(OnContactSelected mListener){
        this.mListener = mListener;
    }

}
