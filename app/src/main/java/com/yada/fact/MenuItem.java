package com.yada.fact;

import com.google.gson.annotations.SerializedName;

public class MenuItem {
    @SerializedName("restaurant_name")
    public String restaurantName;
    @SerializedName("item_name")
    public String itemName;
    @SerializedName("item_type")
    public int itemType;
    public int calories;
}
