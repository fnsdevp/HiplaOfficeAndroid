package com.hipla.smartoffice_new.fragment;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.hipla.smartoffice_new.networking.MultipartUtility;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.databinding.FragmentEditProfileBinding;
import com.hipla.smartoffice_new.dialogs.Dialogs;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.utils.CONST;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;
import me.shaohui.advancedluban.Luban;
import me.shaohui.advancedluban.OnCompressListener;

/**
 * A simple {@link Fragment} subclass.
 */
public class EditProfileFragment extends Fragment implements StringRequestListener, Dialogs.OnImageSelect {

    private static final int REQUEST_CAMERA_STORAGE_PERMISSION = 100;
    private FragmentEditProfileBinding binding;
    public static final int REQUEST_IMAGE_CAPTURE = 1;
    public static final int REQUEST_OPEN_GALLERY = 2;
    private static final String LOG_TAG = "Test";
    private Uri imageUri;
    private String mCurrentPhotoPath;
    private SimpleDateFormat dt1 = new SimpleDateFormat("yyyy-MM-dd");
    private static final int CROP_PIC_REQUEST_CODE = 3;
    private File file = null;

    public EditProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false);
        initView();
        return binding.getRoot();
    }

    private void initView() {
        try {

            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                binding.tvUserType.setText(String.format(getResources().getString(R.string.user_format), userData.getUsertype()));
                binding.tvFirstName.setText(String.format(getResources().getString(R.string.first_name_format),
                        userData.getFname()));
                binding.tvLastName.setText(String.format(getResources().getString(R.string.last_name_format),
                        userData.getLname()));
                if (userData.getUsertype().equalsIgnoreCase("Employee")) {
                    binding.tvDepartment.setText(String.format(getResources().getString(R.string.department_format),
                            userData.getDepartment()));
                } else {
                    binding.tvDepartment.setText(String.format(getResources().getString(R.string.company_format),
                            userData.getCompany()));
                }
                binding.tvDesignation.setText(String.format(getResources().getString(R.string.designation_format),
                        userData.getDesignation()));
                binding.tvEmail.setText(String.format(getResources().getString(R.string.email_format),
                        userData.getEmail()));
                binding.tvPhone.setText(String.format(getResources().getString(R.string.phone_format),
                        userData.getPhone()));

                binding.toggleButton.setChecked(Paper.book().read(CONST.DISTANCE_NOTIFICATION, true));

                ImageLoader.getInstance().displayImage(userData.getProfile_image(),
                        binding.ivProfilePic, CONST.ErrorWithLoaderRoundedCorner);

            }

            if (Paper.book().read(CONST.IS_HALF_HOUR_SLOT, true)) {
                binding.rbTimeSlotHalfHours.setChecked(true);
            } else {
                binding.rbTimeSlotOneHours.setChecked(true);
            }

            binding.toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Paper.book().write(CONST.DISTANCE_NOTIFICATION, isChecked);
                }
            });

            binding.tvUpdateDevice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    requestPostData();
                }
            });

            binding.tvChangePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Dialogs.dialogChangePassword(getActivity(), new Dialogs.OnCallback() {
                        @Override
                        public void onSubmit(String password) {

                            changePasswordRequest(password);
                        }
                    });
                }
            });

            binding.btnGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    if (checkedId == binding.rbTimeSlotHalfHours.getId()) {
                        Paper.book().write(CONST.IS_HALF_HOUR_SLOT, true);
                    } else if (checkedId == binding.rbTimeSlotOneHours.getId()) {
                        Paper.book().write(CONST.IS_HALF_HOUR_SLOT, false);
                    }

                }
            });

            binding.ivProfilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkCameraPermission();
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void requestPostData() {

        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

        if (userData != null) {

            HashMap<String, String> requestParameter = new HashMap<>();
            requestParameter.put("userid", "" + userData.getId());
            requestParameter.put("reg", "" + Paper.book().read(NetworkUtility.TOKEN, ""));
            requestParameter.put("type", "Android");

            new PostStringRequest(getActivity(), requestParameter, EditProfileFragment.this, "update",
                    NetworkUtility.BASEURL + NetworkUtility.DEVICE_UPDATE);

        }

    }

    private void changePasswordRequest(String newPassword) {

        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

        if (userData != null) {

            HashMap<String, String> requestParameter = new HashMap<>();
            requestParameter.put("userid", "" + userData.getId());
            requestParameter.put("newpassword", "Android");

            new PostStringRequest(getActivity(), requestParameter, EditProfileFragment.this, "changePassword",
                    NetworkUtility.BASEURL + NetworkUtility.CHANGE_PASSWORD);

        }

    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        try {

            switch (type) {

                case "update":

                    JSONObject responseObject = new JSONObject(result);
                    if (responseObject.optString("status").equalsIgnoreCase("success")
                            && responseObject.optString("message").equalsIgnoreCase("Updated")) {

                        Toast.makeText(getActivity(), getResources().getString(R.string.device_updated), Toast.LENGTH_SHORT).show();
                    }

                    break;

                case "changePassword":

                    JSONObject changePasswordResponse = new JSONObject(result);
                    if (changePasswordResponse.optString("status").equalsIgnoreCase("success") &&
                            changePasswordResponse.optString("message").equalsIgnoreCase("you have successfully update your password")) {

                        Toast.makeText(getActivity(), changePasswordResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), changePasswordResponse.optString("message"), Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onFailure(int responseCode, String responseMessage) {

    }

    @Override
    public void onStarted() {

    }

    public void checkCameraPermission() {
        if (Build.VERSION.SDK_INT > 22) {
            /*if (MarshmallowPermissionHelper.getStorageAndCameraPermission(EditProfileFragment.this
                    , getActivity(), REQUEST_CAMERA_STORAGE_PERMISSION)) {
                Dialogs.dialogFetchImage(getActivity(), this);
            }*/

            if (ActivityCompat.checkSelfPermission(getContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getContext(),
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CAMERA_STORAGE_PERMISSION);
            } else {
                Dialogs.dialogFetchImage(getActivity(), this);
            }

        } else {
            Dialogs.dialogFetchImage(getActivity(), this);
        }
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = getOutputMediaFile();
        imageUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    private void getFromGallery() {
        Intent getFromGallery = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getFromGallery.setType("image/*");
        if (getFromGallery.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(getFromGallery, REQUEST_OPEN_GALLERY);
        }
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraDemo");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
                "" + System.currentTimeMillis() + "_IMG.png");
    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        switch (requestCode) {
            case REQUEST_CAMERA_STORAGE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    Dialogs.dialogFetchImage(getActivity(), this);
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
        if (resultCode == getActivity().RESULT_OK) {

            switch (requestCode) {

                case REQUEST_IMAGE_CAPTURE:

                    try {
                        //String path = file.getAbsolutePath();
                        ImageLoader.getInstance().displayImage("file://" + file.getAbsolutePath(),
                                binding.ivProfilePic, CONST.ErrorWithLoaderRoundedCorner);

                        if (file != null) {
                            Luban.compress(getActivity(), file)
                                    .setMaxSize(50)                // limit the final image size（unit：Kb）
                                    .setMaxHeight(480)             // limit image height
                                    .setMaxWidth(820)              // limit image width
                                    .putGear(Luban.CUSTOM_GEAR)     // use CUSTOM GEAR compression mode
                                    .launch(new OnCompressListener() {
                                        @Override
                                        public void onStart() {

                                        }

                                        @Override
                                        public void onSuccess(File compressFile) {
                                            try {
                                                file = compressFile;

                                                new ServiceTask().execute(NetworkUtility.BASEURL +
                                                        NetworkUtility.UPDATE_PROFILE);
                                            }catch (Exception ex){

                                            }
                                        }

                                        @Override
                                        public void onError(Throwable e) {

                                        }
                                    });
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case REQUEST_OPEN_GALLERY:

                    try {
                        Uri uri = data.getData();
                        String[] projection = {MediaStore.Images.Media.DATA};
                        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
                        cursor.moveToFirst();
                        int columnIndex = cursor.getColumnIndex(projection[0]);
                        String picturePath = cursor.getString(columnIndex); // returns null
                        cursor.close();
                        file = new File(picturePath);
                        imageUri = Uri.fromFile(file);

                        ImageLoader.getInstance().displayImage("file://" + file.getAbsolutePath(),
                                binding.ivProfilePic, CONST.ErrorWithLoaderRoundedCorner);

                        if (file != null) {
                            Luban.compress(getActivity(), file)
                                    .setMaxSize(50)                // limit the final image size（unit：Kb）
                                    .setMaxHeight(480)             // limit image height
                                    .setMaxWidth(820)              // limit image width
                                    .putGear(Luban.CUSTOM_GEAR)     // use CUSTOM GEAR compression mode
                                    .launch(new OnCompressListener() {
                                        @Override
                                        public void onStart() {

                                        }

                                        @Override
                                        public void onSuccess(File compressFile) {
                                            try {
                                                file = compressFile;

                                                new ServiceTask().execute(NetworkUtility.BASEURL +
                                                        NetworkUtility.UPDATE_PROFILE);

                                            }catch (Exception ex){
                                                ex.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void onError(Throwable e) {

                                        }
                                    });
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    @Override
    public void openCamera() {
        takePhoto();
    }

    @Override
    public void openGallery() {
        getFromGallery();
    }

    private class ServiceTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {
            String url = params[0];
            String charset = "UTF-8";

            try {
                final MultipartUtility multipart = new MultipartUtility(url, charset);

                UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

                multipart.addFormField("userid", "" + userData.getId());

                File newfile = getOutputMediaFile();
                if (file != null ) {
                    multipart.addFilePart("photo", file);
                }

                List<String> response = multipart.finish();

                System.out.println("SERVER REPLIED:");

                StringBuilder str = new StringBuilder("");
                for (String line : response) {
                    str.append(line);
                    System.out.println(line);
                }

                return str.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return "{}";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject object = new JSONObject(result);

                if (object.optString("status").equalsIgnoreCase("success")) {

                    UserData userData = Paper.book().read(NetworkUtility.USER_INFO);
                    userData.setProfile_image(object.optString("url",""));
                    Paper.book().write(NetworkUtility.USER_INFO, userData);

                } else {
                    Toast.makeText(getActivity(), "" + object.getString("message"), Toast.LENGTH_SHORT).show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
