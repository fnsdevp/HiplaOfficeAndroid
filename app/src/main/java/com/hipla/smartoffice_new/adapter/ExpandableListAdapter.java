package com.hipla.smartoffice_new.adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.model.FoodCategory;
import com.hipla.smartoffice_new.model.FoodItem;

import java.util.ArrayList;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<FoodCategory> _listDataHeader;
    private OnFoodProductClickListener mListener;
    private List<FoodItem> cartProductList=new ArrayList<>();

    public ExpandableListAdapter(Context context, List<FoodCategory> listDataHeader) {
        this._context = context;
        this._listDataHeader = listDataHeader;
    }

    @Override
    public Object getChild(int groupPosition, int childPosititon) {
        return this._listDataHeader.get(groupPosition)
                .getProd_list()[childPosititon];
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return super.getChildType(groupPosition, childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final FoodItem foodItem = (FoodItem) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.order_food_item, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);

        final TextView tv_quantity_cart = (TextView) convertView
                .findViewById(R.id.tv_quantity_cart);

        ImageView iv_min_cart = (ImageView) convertView
                .findViewById(R.id.iv_min_cart);

        ImageView iv_add_cart = (ImageView) convertView
                .findViewById(R.id.iv_add_cart);

        ImageView food_menu_img=(ImageView)convertView.findViewById(R.id.food_menu_img);

        Glide.with(_context)
                .load(foodItem.getFood_image())
                .into(food_menu_img);
        txtListChild.setText(foodItem.getFood_name());

        iv_add_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(tv_quantity_cart.getText().toString().trim());
                count++ ;
                tv_quantity_cart.setText(""+count);

                if(mListener!=null){
                    mListener.onIncreseItem(count, foodItem);
                }
            }
        });

        iv_min_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(tv_quantity_cart.getText().toString().trim());
                if(count>0){
                    count-- ;
                }else {
                    count = 0 ;
                }
                tv_quantity_cart.setText(""+count);

                if(mListener!=null){
                    mListener.onDecreseItem(count, foodItem);
                }
            }
        });

        if(checkForProductInCart((FoodItem) getChild(groupPosition, childPosition))){
            tv_quantity_cart.setText(""+productCount((FoodItem) getChild(groupPosition, childPosition)));
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (this._listDataHeader.get(groupPosition)
                .getProd_list() != null)
            return this._listDataHeader.get(groupPosition)
                    .getProd_list().length;
        else
            return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this._listDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        if (this._listDataHeader != null)
            return this._listDataHeader.size();
        else
            return 0;

    }

    @Override
    public int getGroupType(int groupPosition) {
        return super.getGroupType(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        FoodCategory foodCategory = (FoodCategory) getGroup(groupPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.order_food_category, null);
        }

        ImageView food_menu_img=(ImageView)convertView.findViewById(R.id.food_menu_hdr_img);

        Glide.with(_context)
                .load(foodCategory.getCategory_image())
                .into(food_menu_img);
        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.lblListHeader);

        lblListHeader.setText(foodCategory.getCategory_name());

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public void notifyDataChange(List<FoodCategory> foodCategoryList, List<FoodItem> cartProductList) {
        this._listDataHeader = foodCategoryList;
        this.cartProductList = cartProductList;
        notifyDataSetChanged();
    }

    public void notifyDataChange(List<FoodItem> cartProductList){
        this.cartProductList = cartProductList;
        notifyDataSetChanged();
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

    private int productCount(FoodItem foodProduct) {
        for (FoodItem fooditem :
                cartProductList) {

            if (fooditem.getFood_id() == foodProduct.getFood_id()) {
                return fooditem.getItemCount();
            }
        }

        return 0;
    }

    public interface OnFoodProductClickListener{
        void onDecreseItem(int count , FoodItem foodProduct);
        void onIncreseItem(int count, FoodItem foodProduct);
    }

    public void setOnFoodProductClickListener(OnFoodProductClickListener mListener){
        this.mListener = mListener;
    }

}
