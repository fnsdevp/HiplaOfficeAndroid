package com.hipla.smartoffice_new.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.hipla.smartoffice_new.R;

public class ErrorMessageDialog {

    private Context context;
    private static ErrorMessageDialog errorMessageDialog;
    private static Context prevContext;
    private static OkCancelClickListener mListener;

    private ErrorMessageDialog(Context context) {
        this.context = context;
    }

    public static ErrorMessageDialog getInstant(Context context, OkCancelClickListener listener) {
        mListener = listener;

        if (errorMessageDialog == null) {
            prevContext = context;
            errorMessageDialog = new ErrorMessageDialog(context);
        }

        if (prevContext != context){
            errorMessageDialog = null;
            errorMessageDialog = new ErrorMessageDialog(context);
        }
        prevContext = context;
        return errorMessageDialog;
    }

    public static ErrorMessageDialog getInstant(Context context) {

        if (errorMessageDialog == null) {
            prevContext = context;
            errorMessageDialog = new ErrorMessageDialog(context);
        }

        if (prevContext != context){
            errorMessageDialog = null;
            errorMessageDialog = new ErrorMessageDialog(context);
        }
        prevContext = context;
        return errorMessageDialog;
    }

    public void show(String msg) {
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle(context.getResources().getString(R.string.app_name));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage(msg);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                dialog.dismiss();
                return;
            }
        });
        dialog.show();
    }
    public void show(String msg, String hdr) {
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle(hdr);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage(msg);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                dialog.dismiss();
                return;
            }
        });
        dialog.show();
    }

    public void show(String msg, String hdr, String okButton, String cancelButton) {
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setTitle(hdr);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage(msg);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, okButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                dialog.dismiss();
                if(mListener!=null){
                    mListener.onOkClickListener();
                }
                return;
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, cancelButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                dialog.dismiss();
                if(mListener!=null){
                    mListener.onCancelClickListener();
                }
                return;
            }
        });
        dialog.show();
    }

    public interface OkCancelClickListener{
        void onOkClickListener();
        void onCancelClickListener();
    }

}
