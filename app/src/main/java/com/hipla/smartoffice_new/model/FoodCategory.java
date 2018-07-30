package com.hipla.smartoffice_new.model;

/**
 * Created by FNSPL on 5/11/2018.
 */

public class FoodCategory {

    private int cat_id;
    private String category_name;
    private String category_image;
    private FoodItem[] prod_list;

    public int getCat_id() {
        return cat_id;
    }

    public void setCat_id(int cat_id) {
        this.cat_id = cat_id;
    }

    public String getCategory_name() {
        return category_name;
    }

    public void setCategory_name(String category_name) {
        this.category_name = category_name;
    }

    public String getCategory_image() {
        return category_image;
    }

    public void setCategory_image(String category_image) {
        this.category_image = category_image;
    }

    public FoodItem[] getProd_list() {
        return prod_list;
    }

    public void setProd_list(FoodItem[] prod_list) {
        this.prod_list = prod_list;
    }
}
