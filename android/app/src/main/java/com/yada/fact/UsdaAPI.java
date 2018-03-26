package com.yada.fact;

import android.util.Log;
import android.widget.Toast;

import com.yada.fact.APIModel.NutritionAPIModel.Food;
import com.yada.fact.APIModel.NutritionAPIModel.FoodSuggestion;
import com.yada.fact.APIModel.NutritionAPIModel.Nutrient;
import com.yada.fact.APIModel.NutritionAPIModel.NutrientResult;
import com.yada.fact.APIModel.NutritionAPIModel.NutritionAPI;
import com.yada.fact.APIModel.SearchAPIModel.Item;
import com.yada.fact.APIModel.SearchAPIModel.SearchAPI;
import com.yada.fact.APIModel.SearchAPIModel.SearchResult;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yadhuprakash on 3/12/18.
 */

public class UsdaAPI {

    public SearchResult searchQueryFromUSDA(String query){
        // first API.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SearchAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) //Here we are using the GsonConverterFactory to directly convert json data to object
                .build();

        SearchAPI api = retrofit.create(SearchAPI.class);
        SearchResult result = null;
        Call<SearchResult> call = api.getResults("json",10,"sl3EczHuFZGdwun9s4YZwfS5uGh4msXW6THLUups",query);
        try {
            result = call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;

        /*
        call.enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                SearchResult result = response.body();
                Log.d("query",result.getResultList().getQ());

            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {
            }
        });*/
    }

    public NutrientResult searchNutrientFromUSDA(int ndbno){
        // second API.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SearchAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) //Here we are using the GsonConverterFactory to directly convert json data to object
                .build();

        NutritionAPI api = retrofit.create(NutritionAPI.class);
        NutrientResult result= null;
        Call<NutrientResult> call = api.getCaloriesOfItem(ndbno,"json","sl3EczHuFZGdwun9s4YZwfS5uGh4msXW6THLUups",208);


        try {
            result = call.execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
        call.enqueue(new Callback<NutrientResult>() {
            @Override
            public void onResponse(Call<NutrientResult> call, Response<NutrientResult> response) {
                NutrientResult result = response.body();
                Log.d("Sucessfully got Nutri", result.getReport().getFood().getName());
            }

            @Override
            public void onFailure(Call<NutrientResult> call, Throwable t) {
            }
        });*/


        return result;
    }

    public ArrayList<FoodSuggestion> filterResultsOnCalories(SearchResult sr, String plan, Integer calorieExpected){
        ArrayList<Item> listOfItems = sr.getResultList().getItem();
        ArrayList<FoodSuggestion> listOfSuggestions = new ArrayList<>();

        // for each item from restaurant, query for nutrients...
        for(Item i: listOfItems){
            NutrientResult potentialSuggestion = searchNutrientFromUSDA(i.getNdbno());
            ArrayList<Nutrient> foodNutrients = potentialSuggestion.getReport().getFood().getNutrients();
            for(Nutrient nutri: foodNutrients){
                if(nutri.getNutrient_id().equals("208")){
                    int caloriesInFood = Integer.parseInt(nutri.getValue());
                    if(plan.equals("gain")&& caloriesInFood >= calorieExpected){
                        listOfSuggestions.add(new FoodSuggestion(potentialSuggestion.getReport().getFood().getName(),
                                Integer.parseInt(nutri.getValue())));
                    }
                    else if(plan.equals("loss")&& caloriesInFood <= calorieExpected){
                        listOfSuggestions.add(new FoodSuggestion(potentialSuggestion.getReport().getFood().getName(),
                                Integer.parseInt(nutri.getValue())));
                    }
                }
            }

        }
        return listOfSuggestions;
    }




}
