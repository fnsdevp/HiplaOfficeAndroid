package com.hipla.smartoffice_new.dialogs;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.model.IndboxMessages;
import com.hipla.smartoffice_new.model.UserData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.paperdb.Paper;

/**
 * A simple {@link Fragment} subclass.
 */
public class IndboxReadDialogFragment extends DialogFragment implements StringRequestListener {

    private OnDialogEvent mListener;
    private ProgressDialog mDialog;
    private IndboxMessages mData;
    private View binding;
    private ImageView ivClose;
    private TextView tvName, tvEmail, tvPhone, tvTitle, tvMessage, btnClose;

    public IndboxReadDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = inflater.inflate(R.layout.fragment_message_details, container, false);
        initView(binding);
        return binding;
    }

    public void setData(IndboxMessages mData){
        this.mData = mData;
    }

    private void initView(View binding) {
        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage(getResources().getString(R.string.please_wait));

        ivClose = (ImageView) binding.findViewById(R.id.iv_close);
        tvName = (TextView) binding.findViewById(R.id.tv_name);
        tvEmail = (TextView) binding.findViewById(R.id.tv_email);
        tvPhone = (TextView) binding.findViewById(R.id.tv_phone);
        tvTitle = (TextView) binding.findViewById(R.id.tv_title);
        tvMessage = (TextView) binding.findViewById(R.id.tv_message);
        btnClose = (TextView) binding.findViewById(R.id.btn_close);

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        tvName.setText(String.format(getString(R.string.name_format), mData.getFrom().getName()));
        tvEmail.setText(String.format(getString(R.string.email_format), mData.getFrom().getEmail()));
        tvPhone.setText(String.format(getString(R.string.phone_format), mData.getFrom().getPhone()));
        tvTitle.setText(String.format(getString(R.string.title_format), mData.getTitle()));
        tvMessage.setText(""+ mData.getMsg());

        updateMessage();
    }

    public void updateMessage() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));
                requestParameter.put("id", String.format("%s", mData.getId()));
                requestParameter.put("status", String.format("%s", "read"));

                new PostStringRequest(getActivity(), requestParameter, IndboxReadDialogFragment.this, "updateMessages",
                        NetworkUtility.BASEURL + NetworkUtility.UPDATE_MESSAGES_STATUS);

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
    public void onSuccess(String result, String type) throws JSONException {
        try{

            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            switch (type){
                case "updateMessages":

                    JSONObject jsonObject = new JSONObject(result);

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

}
