package com.hipla.smartoffice_new.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hipla.smartoffice_new.R;
import com.hipla.smartoffice_new.model.FoodItem;
import com.hipla.smartoffice_new.model.ReviewMessage;

import java.util.List;

/**
 * Created by avishek on 15/11/16.
 */

public class CartItemListAdapter extends BaseAdapter {

    public List<FoodItem> _data;
    private Context _c;
    private ViewHolder v;
    private SelectContactToInvite callback;
    private OnFoodProductClickListener mListener;

    public CartItemListAdapter(List<FoodItem> selectUsers, Context context) {
        _data = selectUsers;
        _c = context;
    }

    @Override
    public int getCount() {
        return _data.size();
    }

    @Override
    public Object getItem(int i) {
        return _data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_food_item, viewGroup, false);
            Log.e("Inside", "here--------------------------- In view1");
        } else {
            view = convertView;
            Log.e("Inside", "here--------------------------- In view2");
        }

        v = new ViewHolder();

        final FoodItem foodItem = _data.get(i);

        TextView txtListChild = (TextView) view
                .findViewById(R.id.lblListItem);

        final TextView tv_quantity_cart = (TextView) view
                .findViewById(R.id.tv_quantity_cart);

        ImageView iv_min_cart = (ImageView) view
                .findViewById(R.id.iv_min_cart);

        ImageView iv_add_cart = (ImageView) view
                .findViewById(R.id.iv_add_cart);

        txtListChild.setText("" + foodItem.getFood_name());

        tv_quantity_cart.setText("" + foodItem.getItemCount());
        ImageView food_menu_img = (ImageView) view.findViewById(R.id.food_menu_img);

        Glide.with(_c)
                .load(foodItem.getFood_image())
                .into(food_menu_img);

        iv_add_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(tv_quantity_cart.getText().toString().trim());
                count++;
                tv_quantity_cart.setText("" + count);

                if (mListener != null) {
                    mListener.onIncreseItem(count, foodItem);
                }
            }
        });

        iv_min_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int count = Integer.parseInt(tv_quantity_cart.getText().toString().trim());
                if (count > 0) {
                    count--;
                } else {
                    count = 0;
                }
                tv_quantity_cart.setText("" + count);

                if (mListener != null) {
                    mListener.onDecreseItem(count, foodItem);
                }
            }
        });

        view.setTag(foodItem);
        return view;
    }

    public void notifyAdpater(List<FoodItem> selectUsers) {
        _data = selectUsers;
        notifyDataSetChanged();
    }

    public interface SelectContactToInvite {
        void selectedRoom(ReviewMessage review);
    }

    static class ViewHolder {
        TextView tv_name, tv_message;
        LinearLayout rowLayout;
    }

    public interface OnFoodProductClickListener {
        void onDecreseItem(int count, FoodItem foodProduct);

        void onIncreseItem(int count, FoodItem foodProduct);
    }

    public void setOnFoodProductClickListener(OnFoodProductClickListener mListener) {
        this.mListener = mListener;
    }
}

