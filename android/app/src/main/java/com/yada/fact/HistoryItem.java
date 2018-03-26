package com.yada.fact;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryItem {
    public boolean eating;
    public long startTime, endTime;
    public ActivityItem activityItem;
    public FoodItem foodItem;

    public static final Map<Integer, String> myMap;
    static {
        Map<Integer, String> aMap = new HashMap<>();
        aMap.put(0, "Unknown");
        aMap.put(1, "Breakfast");
        aMap.put(2, "Lunch");
        aMap.put(3, "Dinner");
        aMap.put(4, "Snack");
        myMap = Collections.unmodifiableMap(aMap);
    }

    public HistoryItem(long startTime, long endTime, String activity, float calories) {
        this.eating = false;
        this.startTime = startTime;
        this.endTime = endTime;
        this.activityItem = new ActivityItem();
        this.activityItem.activity = activity;
        this.activityItem.calories = calories;
    }

    public HistoryItem(long startTime, long endTime, String foodItem, int mealType, Map<String, Float> nutrients) {
        this.eating = true;
        this.startTime = startTime;
        this.endTime = endTime;
        this.foodItem = new FoodItem();
        this.foodItem.foodItem = foodItem;
        this.foodItem.mealType = mealType;
        this.foodItem.nutrients = nutrients;
    }

    public class ActivityItem {
        public String activity;
        public float calories;
    }

    public class FoodItem {
        public String foodItem;
        public int mealType;
        public Map<String, Float> nutrients;
    }
}
