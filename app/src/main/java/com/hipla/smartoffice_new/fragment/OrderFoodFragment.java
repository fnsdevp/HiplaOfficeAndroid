package com.hipla.smartoffice_new.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hipla.smartoffice_new.networking.NetworkUtility;
import com.hipla.smartoffice_new.networking.PostStringRequest;
import com.hipla.smartoffice_new.networking.StringRequestListener;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.activity.DashboardActivity;
import com.hipla.smartoffice_new.adapter.ExpandableListAdapter;
import com.hipla.smartoffice_new.databinding.FragmentOrderFoodBinding;
import com.hipla.smartoffice_new.model.FoodCategory;
import com.hipla.smartoffice_new.model.FoodItem;
import com.hipla.smartoffice_new.model.UserData;
import com.hipla.smartoffice_new.utils.CONST;
import com.hipla.smartoffice_new.utils.InternetConnectionDetector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.paperdb.Paper;

public class OrderFoodFragment extends Fragment implements StringRequestListener, ExpandableListAdapter.OnFoodProductClickListener {

    private FragmentOrderFoodBinding binding;
    private ExpandableListAdapter listAdapter;
    private List<FoodCategory> listDataHeader = new ArrayList<>();
    private List<FoodItem> cartProductList = new ArrayList<>();

    public OrderFoodFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_food, container, false);
        initView();
        return binding.getRoot();
    }

    public void initView() {

        cartProductList = Paper.book().read(CONST.FOOD_ITEMS_IN_CART, new ArrayList<FoodItem>());

        listAdapter = new ExpandableListAdapter(getActivity(), listDataHeader);
        listAdapter.setOnFoodProductClickListener(this);
        // setting list adapter
        binding.lvExp.setAdapter(listAdapter);


        binding.btnConfirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (totalCount() > 0) {
                    if ((DashboardActivity) getActivity() != null) {
                        ((DashboardActivity) getActivity()).setFragment(new FoodCartFragment(), CONST.FOOD_CART);
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.no_item_added_in_cart), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        cartProductList = Paper.book().read(CONST.FOOD_ITEMS_IN_CART, new ArrayList<FoodItem>());

        getFoodCategories();

    }

    private void getFoodCategories() {
        try {
            UserData userData = Paper.book().read(NetworkUtility.USER_INFO);

            if (userData != null) {
                HashMap<String, String> requestParameter = new HashMap<>();
                requestParameter.put("userid", String.format("%s", userData.getId()));

                new PostStringRequest(getActivity(), requestParameter, OrderFoodFragment.this, "getCategoriesList",
                        NetworkUtility.BASEURL + NetworkUtility.GET_ALL_FOOD_CATEGORY);
            }
        } catch (Exception ex) {

        }
    }

    @Override
    public void onSuccess(String result, String type) throws JSONException {
        try {

            switch (type) {

                case "getCategoriesList":

                    JSONObject resJsonObject = new JSONObject(result);
                    if (resJsonObject.optString("status").equalsIgnoreCase("success")) {

                        GsonBuilder builder = new GsonBuilder();
                        builder.setPrettyPrinting();
                        Gson gson = builder.create();

                        FoodCategory[] foodCategories = gson.fromJson(resJsonObject.getJSONArray("categories_list").toString(),
                                FoodCategory[].class);
                        List<FoodCategory> foodCategoryList = Arrays.asList(foodCategories);

                        listAdapter.notifyDataChange(foodCategoryList, cartProductList);
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
                    if (count == 0) {
                        cartProductList.remove(i);
                    }
                }
            }
        } else {
            foodProduct.setItemCount(count);

            cartProductList.add(foodProduct);
        }

        listAdapter.notifyDataChange(cartProductList);
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

        listAdapter.notifyDataChange(cartProductList);
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

            count += fooditem.getItemCount();
        }

        return count;
    }

}
