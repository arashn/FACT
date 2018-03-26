package com.yada.fact.APIModel.SearchAPIModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by yadhuprakash on 3/12/18.
 */

public interface SearchAPI {
    String BASE_URL = " https://api.nal.usda.gov/ndb/";

    @GET("search/")
    Call<SearchResult> getResults(
            @Query("format") String format,
            @Query("max") Integer max,
            @Query("api_key") String apiKey,
            @Query("q") String query
    );
}
