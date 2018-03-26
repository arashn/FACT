package com.yada.fact;

import com.google.gson.annotations.SerializedName;

public class FoodDbSearchParams {
    @SerializedName("restaurants")
    public String[] restaurantNames;
    @SerializedName("item_type")
    public int mealType;
    public float calories;
    @SerializedName("fitness_goal")
    public int fitnessGoal;
}
