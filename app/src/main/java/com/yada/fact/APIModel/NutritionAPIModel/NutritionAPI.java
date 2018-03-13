package com.yada.fact.APIModel.NutritionAPIModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by yadhuprakash on 3/12/18.
 */

public interface NutritionAPI {

    String BASE_URL = " https://api.nal.usda.gov/ndb/";

    @GET("reports/")
    Call<NutrientResult> getCaloriesOfItem(
            @Query("ndbno") Integer ndbno,
            @Query("format") String format,
            @Query("api_key") String apiKey,
            @Query("nutrients") Integer nutrients
    );
}
