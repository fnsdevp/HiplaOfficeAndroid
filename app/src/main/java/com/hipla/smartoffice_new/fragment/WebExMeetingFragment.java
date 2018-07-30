package com.hipla.smartoffice_new.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.databinding.FragmentWebexMeetingBinding;
import com.hipla.smartoffice_new.model.ContactModel;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.utils.CONST;
import com.hipla.smartoffice_new.utils.MarshmallowPermissionHelper;

import java.util.List;
import java.util.Random;

import io.paperdb.Paper;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class WebExMeetingFragment extends BlankFragment implements SelectContactFragment.OnContactSelected {

    private static final int READ_CONTACT_PERMISSION = 101;
    private FragmentWebexMeetingBinding binding;
    private ContactModel selectedContact;
    private ProgressDialog mDialog;
    private String emailId;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private static final int START_SIGN_ON = 555;
    private static final int START_CREATE_MEETING = 55;
    private static final String TAG = "HiRit";


    public WebExMeetingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_webex_meeting, container, false);
        binding.setFragment(WebExMeetingFragment.this);

        initView();
        return binding.getRoot();
    }

    private void initView() {

        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage(getResources().getString(R.string.please_wait));

        binding.ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (MarshmallowPermissionHelper.getReadContactPermission(WebExMeetingFragment.this, getActivity(),
                            READ_CONTACT_PERMISSION)) {
                        SelectContactFragment mDialog = new SelectContactFragment();
                        mDialog.setOnContactSelected(WebExMeetingFragment.this);
                        mDialog.show(getChildFragmentManager(), CONST.CONTACT_DIALOG);
                    }
                } else {
                    SelectContactFragment mDialog = new SelectContactFragment();
                    mDialog.setOnContactSelected(WebExMeetingFragment.this);
                    mDialog.show(getChildFragmentManager(), CONST.CONTACT_DIALOG);
                }
            }
        });


    }

    @Override
    public void onContactSelected(ContactModel contactModel) {
        selectedContact = contactModel;

        binding.tvSelectedName.setText(String.format("%s", contactModel.getName()));

    }

    public void submitCreateMeeting() {

        if (selectedContact != null && !binding.etEmail.getText().toString().isEmpty()
                && !binding.etAgenda.getText().toString().isEmpty() && binding.etEmail.getText().toString().matches(emailPattern)) {

            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
            startMeeting();

        } else if (selectedContact == null) {
            Toast.makeText(getActivity(), getResources().getString(R.string.please_select_contact), Toast.LENGTH_SHORT).show();
        } else if (binding.etEmail.getText().toString().isEmpty() || !binding.etEmail.getText().toString().matches(emailPattern)) {
            Toast.makeText(getActivity(), getResources().getString(R.string.please_select_email), Toast.LENGTH_SHORT).show();
        } else if (binding.etAgenda.getText().toString().isEmpty()) {
            Toast.makeText(getActivity(), getResources().getString(R.string.enter_agenda_for_the_meeting), Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case READ_CONTACT_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SelectContactFragment mDialog = new SelectContactFragment();
                    mDialog.setOnContactSelected(WebExMeetingFragment.this);
                    mDialog.show(getChildFragmentManager(), CONST.CONTACT_DIALOG);
                }
                return;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case START_SIGN_ON:
                if (resultCode == RESULT_OK) {
                    /*List<String> attendees=new ArrayList<String>();
                    attendees.add("arijit.das76@gmail.com");
                    attendees.add("ritamkumarbasu@gmail.com");*/
                    createMeeting(binding.etEmail.getText().toString().trim().toLowerCase());
                } else {
                    Toast.makeText(getActivity(),"Sign on failed", Toast.LENGTH_SHORT).show();
                }
                break;
            case START_CREATE_MEETING:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(getActivity(),"Meeting created successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(),"Meeting creation failed", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public boolean isPackageExisted(String targetPackage){
        PackageManager pm = getActivity().getPackageManager();
        try {
            PackageInfo info=pm.getPackageInfo(targetPackage,PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    private void startMeeting() {
        if(isPackageExisted("com.cisco.webex.meetings")) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addCategory(Intent.CATEGORY_BROWSABLE);
            i.putExtra("INTENT_EXTRA_NO_ANIM", true);
            i.setData(Uri.parse("wbx://WbxSignIn"));
            startActivityForResult(i, START_SIGN_ON);
        }else{
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.cisco.webex.meetings")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.cisco.webex.meetings")));
            }
        }
    }

    private void createMeeting(String attendee) {
        if(isPackageExisted("com.cisco.webex.meetings")) {
            String attendeesString = "wbx://WbxSchedule?attendees=" + attendee;
            attendeesString += "&password=";
            attendeesString += "" + ((int) (Math.random() * 9000) + 1000);
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addCategory(Intent.CATEGORY_BROWSABLE);
            i.putExtra("INTENT_EXTRA_NO_ANIM", true);
            i.setData(Uri.parse(attendeesString));
            startActivityForResult(i, START_CREATE_MEETING);
        }else{
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.cisco.webex.meetings")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.cisco.webex.meetings")));
            }
        }
    }

    private void createMeeting(List<String> attendees) {
        if(isPackageExisted("com.cisco.webex.meetings")) {
            String attendeesString = "wbx://WbxSchedule?attendees=" + createAttendeesString(attendees);
            attendeesString += "&password=";
            Random rn = new Random();
            attendeesString += rn.nextLong();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addCategory(Intent.CATEGORY_BROWSABLE);
            i.putExtra("INTENT_EXTRA_NO_ANIM", true);
            i.setData(Uri.parse(attendeesString));
            startActivityForResult(i, START_CREATE_MEETING);
        }else{
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.cisco.webex.meetings")));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.cisco.webex.meetings")));
            }
        }
    }

    private String createAttendeesString(List<String> attendees) {
        String attendString = "";
        for (int i = 0; i < attendees.size() - 1; i++) {
            attendString += attendees.get(i) + ",";
        }
        attendString += attendees.get(attendees.size() - 1);
        return attendString;
    }
}
