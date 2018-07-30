package com.hipla.smartoffice_new.dialogs;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hipla.smartoffice_new.fragment.SelectContactFragment;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.databinding.FragmentCreateMessageDialogBinding;
import com.hipla.smartoffice_new.model.ContactModel;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.utils.CONST;
import com.hipla.smartoffice_new.utils.MarshmallowPermissionHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreateMessageDialogFragment extends DialogFragment implements SelectContactFragment.OnContactSelected, StringRequestListener {

    private OnDialogEvent mListener;
    private FragmentCreateMessageDialogBinding binding;
    private ContactModel choosenContact;
    private ProgressDialog mDialog;
    private static final int READ_CONTACT_PERMISSION = 101;

    public CreateMessageDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_message_dialog, container, false);
        initView();
        return binding.getRoot();
    }

    private void initView() {
        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage(getResources().getString(R.string.please_wait));

        binding.ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        binding.btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (MarshmallowPermissionHelper.getReadContactPermission(CreateMessageDialogFragment.this, getActivity(),
                            READ_CONTACT_PERMISSION)) {

                        SelectContactFragment mDialog = new SelectContactFragment();
                        mDialog.setOnContactSelected(CreateMessageDialogFragment.this);
                        mDialog.show(getChildFragmentManager(), CONST.CONTACT_DIALOG);
                    }
                }else{

                    SelectContactFragment mDialog = new SelectContactFragment();
                    mDialog.setOnContactSelected(CreateMessageDialogFragment.this);
                    mDialog.show(getChildFragmentManager(), CONST.CONTACT_DIALOG);
                }

            }
        });

        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(choosenContact!=null){
                    createMessage();
                }else{
                    Toast.makeText(getActivity(), getString(R.string.please_select_contact), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void createMessage() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("phone", String.format("%s", choosenContact.getPhone()));
                requestParameter.put("title", String.format("%s", binding.etTitle.getText().toString().trim()));
                requestParameter.put("message", String.format("%s", binding.etMessage.getText().toString().trim()));

                new PostStringRequest(getActivity(), requestParameter, CreateMessageDialogFragment.this, "createMessages",
                        NetworkUtility.BASEURL + NetworkUtility.CREATE_MESSAGES);

                if (!mDialog.isShowing()) {
                    mDialog.show();
                }
            }

        } catch (Exception ex) {

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
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if(mListener!=null){
            mListener.onDismissListener();
        }
    }

    @Override
    public void onContactSelected(ContactModel contactModel) {
        this.choosenContact = contactModel;
        binding.tvContactName.setText(""+contactModel.getName());
    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        try{

            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            switch (type){
                case "createMessages":

                    JSONObject jsonObject = new JSONObject(result);

                    if(jsonObject.optString("status").equalsIgnoreCase("success") &&
                            jsonObject.optString("message").equalsIgnoreCase("Message Sent")){
                        dismiss();
                    }else{
                        Toast.makeText(getActivity(), jsonObject.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    public void onStarted() {

    }

    public interface OnDialogEvent{
        void onDismissListener();
    }

    public void setOnDismissClickListener(OnDialogEvent mListener){
        this.mListener = mListener;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case READ_CONTACT_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ){

                    SelectContactFragment mDialog = new SelectContactFragment();
                    mDialog.setOnContactSelected(CreateMessageDialogFragment.this);
                    mDialog.show(getChildFragmentManager(), CONST.CONTACT_DIALOG);
                }
                return;
            }

            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

}
