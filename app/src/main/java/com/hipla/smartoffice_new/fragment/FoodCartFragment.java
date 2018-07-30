package com.hipla.smartoffice_new.fragment;

import android.app.ProgressDialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.activity.DashboardActivity;
import com.hipla.smartoffice_new.adapter.CartItemListAdapter;
import com.hipla.smartoffice_new.databinding.FragmentCartFoodBinding;
import com.hipla.smartoffice_new.model.FoodItem;
import com.hipla.smartoffice_new.model.UpcomingMeetings;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.utils.CONST;
import com.hipla.smartoffice_new.utils.InternetConnectionDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;

public class FoodCartFragment extends Fragment implements StringRequestListener, CartItemListAdapter.OnFoodProductClickListener {

    private FragmentCartFoodBinding binding;
    private CartItemListAdapter listAdapter;
    private List<FoodItem> cartProductList = new ArrayList<>();
    private ProgressDialog mDialog;

    public FoodCartFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cart_food, container, false);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        initView();
        return binding.getRoot();
    }


    public void initView() {

        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage(getResources().getString(R.string.please_wait));

        cartProductList = Paper.book().read(CONST.FOOD_ITEMS_IN_CART, new ArrayList<FoodItem>());

        listAdapter = new CartItemListAdapter(cartProductList, getActivity());
        listAdapter.setOnFoodProductClickListener(this);
        // setting list adapter
        binding.lvExp.setAdapter(listAdapter);

        binding.btnConfirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (InternetConnectionDetector.getInstant(getActivity()).isConnectingToInternet()) {
                    createOrder();
                }
            }
        });

        binding.tvTotalCount.setText(String.format(getString(R.string.total),""+totalCount()));
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void createOrder() {

        if(!mDialog.isShowing()){
            mDialog.show();
        }

        HashMap<String, String> requestParameter = new HashMap<>();
        UpcomingMeetings meetingDetail = Paper.book().read(CONST.CURRENT_MEETING_DATA);
        UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

        int count = 0;

        if (cartProductList != null && meetingDetail!=null) {

            try {
                JSONArray jsonArray = new JSONArray();

                for (FoodItem foodProduct :
                        cartProductList) {

                    if(foodProduct.getItemCount()>0) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("product_id", "" + foodProduct.getFood_id());
                        jsonObject.put("quantity", "" + foodProduct.getItemCount());

                        jsonArray.put(jsonObject);

                        count = count + foodProduct.getItemCount();
                    }
                }

                //need to get location id in upcoming meeting
                requestParameter.put("room_id", "" + meetingDetail.getLocation());
                requestParameter.put("user_id", "" + userData.getId());
                requestParameter.put("total_quantity", "" + count);
                requestParameter.put("note", "" + binding.etNote.getText().toString().trim());
                requestParameter.put("meeting_id", "" + meetingDetail.getId());
                requestParameter.put("order_date", "" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                requestParameter.put("product", "" + jsonArray.toString());

                new PostStringRequest(getActivity(), requestParameter, FoodCartFragment.this, "creteOrder",
                        NetworkUtility.BASEURL + NetworkUtility.CREATE_ORDER);

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        try {

            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            switch (type) {

                case "creteOrder":

                    JSONObject resJsonObject = new JSONObject(result);
                    if (resJsonObject.optString("status").equalsIgnoreCase("success")) {

                        cartProductList.clear();
                        Paper.book().delete(CONST.FOOD_ITEMS_IN_CART);

                        if((DashboardActivity)getActivity()!=null){
                            if (getChildFragmentManager().getBackStackEntryCount() > 0) {
                                boolean done = getChildFragmentManager().popBackStackImmediate();
                            }

                            if((DashboardActivity)getActivity()!=null){
                                ((DashboardActivity)getActivity()).setFragment(new HomeFragment(), CONST.HOME_FRAGMENT);
                            }
                        }
                    }

                    break;
            }

        } catch (Exception ex) {
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

    @Override
    public void onPause() {
        super.onPause();

        if (cartProductList != null && cartProductList.size() > 0)
            Paper.book().write(CONST.FOOD_ITEMS_IN_CART, cartProductList);
        else
            Paper.book().delete(CONST.FOOD_ITEMS_IN_CART);
    }

    @Override
    public void onDecreseItem(int count, FoodItem foodProduct) {

        if (checkForProductInCart(foodProduct)) {
            for (int i = 0; i < cartProductList.size(); i++) {
                if (cartProductList.get(i).getFood_id() == foodProduct.getFood_id()) {
                    cartProductList.get(i).setItemCount(count);
                }
            }
        } else {
            foodProduct.setItemCount(count);

            cartProductList.add(foodProduct);
        }

        binding.tvTotalCount.setText(String.format(getString(R.string.total),""+totalCount()));
    }

    @Override
    public void onIncreseItem(int count, FoodItem foodProduct) {
        if (checkForProductInCart(foodProduct)) {
            for (int i = 0; i < cartProductList.size(); i++) {
                if (cartProductList.get(i).getFood_id() == foodProduct.getFood_id()) {
                    cartProductList.get(i).setItemCount(count);
                }
            }
        } else {
            foodProduct.setItemCount(count);

            cartProductList.add(foodProduct);
        }

        binding.tvTotalCount.setText(String.format(getString(R.string.total),""+totalCount()));
    }

    private boolean checkForProductInCart(FoodItem foodProduct) {
        for (FoodItem fooditem :
                cartProductList) {

            if (fooditem.getFood_id() == foodProduct.getFood_id()) {
                return true;
            }
        }

        return false;
    }

    private int totalCount() {
        int count = 0;
        for (FoodItem fooditem :
                cartProductList) {

            count+=fooditem.getItemCount();
        }

        return count;
    }
}
