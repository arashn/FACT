package com.yada.fact;

import android.util.Log;
import android.widget.Toast;

import com.yada.fact.APIModel.NutritionAPIModel.NutrientResult;
import com.yada.fact.APIModel.NutritionAPIModel.NutritionAPI;
import com.yada.fact.APIModel.SearchAPIModel.SearchAPI;
import com.yada.fact.APIModel.SearchAPIModel.SearchResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by yadhuprakash on 3/12/18.
 */

public class UsdaAPI {

    public void searchQueryFromUSDA(String query){
        // first API.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SearchAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) //Here we are using the GsonConverterFactory to directly convert json data to object
                .build();

        SearchAPI api = retrofit.create(SearchAPI.class);

        Call<SearchResult> call = api.getResults("json",10,"sl3EczHuFZGdwun9s4YZwfS5uGh4msXW6THLUups",query);

        call.enqueue(new Callback<SearchResult>() {
            @Override
            public void onResponse(Call<SearchResult> call, Response<SearchResult> response) {
                SearchResult result = response.body();
                Log.d("query",result.getResultList().getQ());

            }

            @Override
            public void onFailure(Call<SearchResult> call, Throwable t) {
            }
        });
    }

    public void searchNutrientFromUSDA(int ndbno){
        // second API.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SearchAPI.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) //Here we are using the GsonConverterFactory to directly convert json data to object
                .build();

        NutritionAPI api = retrofit.create(NutritionAPI.class);

        Call<NutrientResult> call = api.getCaloriesOfItem(ndbno,"json","sl3EczHuFZGdwun9s4YZwfS5uGh4msXW6THLUups",208);

        call.enqueue(new Callback<NutrientResult>() {
            @Override
            public void onResponse(Call<NutrientResult> call, Response<NutrientResult> response) {
                NutrientResult result = response.body();
                Log.d("query term",result.getReport().getSr());

            }

            @Override
            public void onFailure(Call<NutrientResult> call, Throwable t) {
            }
        });
    }




}
