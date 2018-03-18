package com.yada.fact;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface FoodDbApi {
    @POST("search")
    Call<List<MenuItem>> search(@Body FoodDbSearchParams params);
}
