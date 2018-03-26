package com.yada.fact.APIModel.NutritionAPIModel;

/**
 * Created by yadhuprakash on 3/14/18.
 */

public class FoodSuggestion {

    String NameOfItem;
    Integer calories;

    public FoodSuggestion(String nameOfItem, Integer calories) {
        NameOfItem = nameOfItem;
        this.calories = calories;
    }

    public String getNameOfItem() {
        return NameOfItem;
    }

    public Integer getCalories() {
        return calories;
    }
}
