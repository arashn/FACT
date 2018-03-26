package com.yada.fact;

import android.Manifest;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.RankBy;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Transformer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecommendationsFragment extends Fragment {
    private static final String TAG = "RecommendationsFragment";

    private static final String FOOD_BASE_URL = "http://169.234.29.69:3000/";

    public static final int GOAL_MAINTAIN_WEIGHT = 0;
    public static final int GOAL_WEIGHT_GAIN = 1;
    public static final int GOAL_WEIGHT_LOSS = 2;

    private FusedLocationProviderClient mFusedLocationClient;
    private GeoApiContext mGeoApi;

    private RecyclerView mRecyclerView;
    private RecommendationsAdapter mAdapter;
    private LinearLayout mContent;
    private ProgressBar mProgressBar;
    private TextView mHeader, mDailyCalories, mNextMealCaloriesDesc, mNextMealCalories, mNoMoreFood;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recommendations_fragment, container, false);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        mGeoApi = new GeoApiContext.Builder().apiKey("AIzaSyAdZiz-qiclq7TCGLPavHXVqCIkpKACZto").build();

        mContent = view.findViewById(R.id.recommendations_content);
        mProgressBar = view.findViewById(R.id.recommendations_progress_bar);
        mRecyclerView = view.findViewById(R.id.menu_items_recycler_view);
        mHeader = view.findViewById(R.id.recommendations_header);
        mDailyCalories = view.findViewById(R.id.daily_calories);
        mNextMealCaloriesDesc = view.findViewById(R.id.next_meal_calories_desc);
        mNextMealCalories = view.findViewById(R.id.next_meal_calories);
        mNoMoreFood = view.findViewById(R.id.no_more_food);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        mAdapter = new RecommendationsAdapter();
        mRecyclerView.setAdapter(mAdapter);

        getNextMealRecommendation();

        return view;
    }

    private void getNextMealRecommendation() {
        mProgressBar.setVisibility(View.VISIBLE);
        mContent.setVisibility(View.GONE);
        mNoMoreFood.setVisibility(View.GONE);

        final Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MILLISECOND, -1);
        long endTime = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.WEEK_OF_YEAR, -4);
        long startTime = cal.getTimeInMillis();

        final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();
        Log.i(TAG, "Range Start: " + dateFormat.format(startTime));
        Log.i(TAG, "Range End: " + dateFormat.format(endTime));

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(readRequest).addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
            @Override
            public void onSuccess(DataReadResponse dataReadResponse) {
                Log.d(TAG, "Got response");
                Log.d(TAG, "Number of buckets: " + dataReadResponse.getBuckets().size());
                Log.d(TAG, "Number of datasets: " + dataReadResponse.getDataSets().size());
                float avgCaloriesBurned = 0.0f;
                boolean foundDataPoint;
                cal.setTime(new Date());
                for (int i = 1; i <= 4; i++) {
                    foundDataPoint = false;
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    cal.add(Calendar.WEEK_OF_YEAR, -1);
                    long startTime = cal.getTimeInMillis();
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    cal.add(Calendar.MILLISECOND, -1);
                    long endTime = cal.getTimeInMillis();
                    Log.d(TAG, "Day " + i + " start: " + dateFormat.format(startTime));
                    Log.d(TAG, "Day " + i + " end: " + dateFormat.format(endTime));
                    for (Bucket bucket : dataReadResponse.getBuckets()) {
                        DataSet dataSet = bucket.getDataSet(DataType.AGGREGATE_CALORIES_EXPENDED);
                        for (DataPoint dataPoint : dataSet.getDataPoints()) {
                            if (dataPoint.getStartTime(TimeUnit.MILLISECONDS) >= startTime
                                    && dataPoint.getStartTime(TimeUnit.MILLISECONDS) <= endTime) {
                                Log.i(TAG, "Data point:");
                                Log.i(TAG, "\tType: " + dataPoint.getDataType().getName());
                                Log.i(TAG, "\tStart: " + dateFormat.format(dataPoint.getStartTime(TimeUnit.MILLISECONDS)));
                                Log.i(TAG, "\tEnd: " + dateFormat.format(dataPoint.getEndTime(TimeUnit.MILLISECONDS)));
                                for (Field field : dataPoint.getDataType().getFields()) {
                                    Log.i(TAG, "\tField: " + field.getName() + " Value: " + dataPoint.getValue(Field.FIELD_CALORIES));
                                }
                                avgCaloriesBurned = ((avgCaloriesBurned * (i - 1)) + dataPoint.getValue(Field.FIELD_CALORIES).asFloat()) / i;
                                foundDataPoint = true;
                                break;
                            }
                        }
                        if (foundDataPoint) break;
                        //dumpDataSet(bucket.getDataSet(DataType.AGGREGATE_CALORIES_EXPENDED));
                    }
                }
                Log.d(TAG, "Average calories burned: " + avgCaloriesBurned);

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                final int fitnessGoal = Integer.parseInt(sharedPref.getString("pref_fitness_goal", "0"));
                int numPounds = sharedPref.getInt("pref_fitness_pounds", 1);
                int timePeriod = sharedPref.getInt("pref_fitness_days", 1);

                if (fitnessGoal == GOAL_WEIGHT_LOSS) {
                    numPounds *= -1;
                }

                final float dailyCalories;
                if (fitnessGoal == GOAL_MAINTAIN_WEIGHT) {
                    dailyCalories = avgCaloriesBurned;
                }
                else {
                    dailyCalories = ((numPounds * 3500) / timePeriod) + avgCaloriesBurned;
                }

                Log.d(TAG, "Number of calories to consume today: " + dailyCalories);

                cal.setTime(new Date());
                long endTime = cal.getTimeInMillis();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long startTime = cal.getTimeInMillis();

                DataReadRequest foodReadRequest = new DataReadRequest.Builder()
                        .read(DataType.TYPE_NUTRITION)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .enableServerQueries()
                        .build();

                Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                        .readData(foodReadRequest).addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        float numCaloriesConsumed = 0.0f;
                        int numMealsRemaining;
                        for (DataSet dataSet : dataReadResponse.getDataSets()) {
                            for (DataPoint dataPoint : dataSet.getDataPoints()) {
                                numCaloriesConsumed += dataPoint.getValue(Field.FIELD_NUTRIENTS).getKeyValue(Field.NUTRIENT_CALORIES);
                            }
                        }
                        Log.d(TAG, "Number of calories consumed today: " + numCaloriesConsumed);

                        cal.setTime(new Date());
                        int hour = cal.get(Calendar.HOUR_OF_DAY);
                        Log.d(TAG, "Hour of day: " + hour);
                        final String searchQuery;
                        final int mealType;
                        if (hour < 12) {
                            numMealsRemaining = 3;
                            searchQuery = "breakfast";
                            mealType = Field.MEAL_TYPE_BREAKFAST;
                            mHeader.setText("Recommendations for breakfast");
                            mNextMealCaloriesDesc.setText("Number of calories to consume for breakfast:");
                        }
                        else if (hour >= 12 && hour < 17) {
                            numMealsRemaining = 2;
                            searchQuery = "lunch";
                            mealType = Field.MEAL_TYPE_LUNCH;
                            mHeader.setText("Recommendations for lunch");
                            mNextMealCaloriesDesc.setText("Number of calories to consume for lunch:");
                        }
                        else {
                            numMealsRemaining = 1;
                            searchQuery = "dinner";
                            mealType = Field.MEAL_TYPE_LUNCH;
                            mHeader.setText("Recommendations for dinner");
                            mNextMealCaloriesDesc.setText("Number of calories to consume for dinner:");
                        }

                        Log.d(TAG, "Number of meals remaining: " + numMealsRemaining);

                        final float nextMealCalories = Math.max((dailyCalories / numMealsRemaining) - numCaloriesConsumed, 0.0f);

                        Log.d(TAG, "Next meal calories: " + nextMealCalories);

                        if (nextMealCalories > 0) {
                            if (fitnessGoal == GOAL_MAINTAIN_WEIGHT) {
                                mDailyCalories.setText(String.format(Locale.US, "About %.0f", dailyCalories));
                                mNextMealCalories.setText(String.format(Locale.US, "About %.0f", nextMealCalories));
                            }
                            else if (fitnessGoal == GOAL_WEIGHT_GAIN) {
                                mDailyCalories.setText(String.format(Locale.US, "At least %.0f", dailyCalories));
                                mNextMealCalories.setText(String.format(Locale.US, "At least %.0f", nextMealCalories));
                            }
                            else if (fitnessGoal == GOAL_WEIGHT_LOSS) {
                                mDailyCalories.setText(String.format(Locale.US, "At most %.0f", dailyCalories));
                                mNextMealCalories.setText(String.format(Locale.US, "At most %.0f", nextMealCalories));
                            }

                            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                mFusedLocationClient.getLastLocation()
                                        .addOnSuccessListener(new OnSuccessListener<Location>() {
                                            @Override
                                            public void onSuccess(Location location) {
                                                if (location != null) {
                                                    Log.d(TAG, "Got location: " + location.getLatitude() + " " + location.getLongitude());
                                                    try {
                                                        PlacesSearchResult[] results = PlacesApi.nearbySearchQuery(mGeoApi, new LatLng(location.getLatitude(), location.getLongitude()))
                                                                .rankby(RankBy.DISTANCE).keyword(searchQuery).await().results;

                                                        Log.d(TAG, "Unique items");
                                                        List<Restaurant> restaurants = new ArrayList<>();
                                                        for (PlacesSearchResult result : results) {
                                                            Restaurant restaurant = new Restaurant(result);
                                                            if (!restaurants.contains(restaurant)) {
                                                                restaurants.add(restaurant);
                                                                Log.d(TAG, "ID: " + result.placeId + " Name: " + result.name);
                                                            }
                                                        }

                                                        Collection<String> restaurantNames = CollectionUtils.collect(restaurants, new Transformer<Restaurant, String>() {
                                                            @Override
                                                            public String transform(Restaurant input) {
                                                                return input.restaurant.name;
                                                            }
                                                        });

                                                        FoodDbSearchParams params = new FoodDbSearchParams();
                                                        params.restaurantNames = restaurantNames.toArray(new String[restaurantNames.size()]);
                                                        params.mealType = mealType;
                                                        params.calories = nextMealCalories;
                                                        params.fitnessGoal = fitnessGoal;

                                                        Retrofit retrofit = new Retrofit.Builder()
                                                                .baseUrl(FOOD_BASE_URL)
                                                                .addConverterFactory(GsonConverterFactory.create())
                                                                .build();

                                                        FoodDbApi service = retrofit.create(FoodDbApi.class);

                                                        Call<List<MenuItem>> searchResults = service.search(params);
                                                        searchResults.enqueue(new Callback<List<MenuItem>>() {
                                                            @Override
                                                            public void onResponse(Call<List<MenuItem>> call, Response<List<MenuItem>> response) {
                                                                Log.d(TAG, "Got search results");
                                                                List<MenuItem> menuItems = new ArrayList<>(response.body());
                                                                Collections.sort(menuItems, new Comparator<MenuItem>() {
                                                                    @Override
                                                                    public int compare(MenuItem menuItem, MenuItem t1) {
                                                                        if (menuItem.calories < t1.calories) {
                                                                            return -1;
                                                                        } else if (menuItem.calories > t1.calories) {
                                                                            return 1;
                                                                        } else {
                                                                            return 0;
                                                                        }
                                                                    }
                                                                });

                                                                for (MenuItem menuItem : menuItems) {
                                                                    Log.d(TAG, "Restaurant name: " + menuItem.restaurantName);
                                                                    Log.d(TAG, "Item name: " + menuItem.itemName);
                                                                    Log.d(TAG, "Item type: " + menuItem.itemType);
                                                                    Log.d(TAG, "Calories: " + menuItem.calories);
                                                                }

                                                                mAdapter.setData(menuItems);

                                                                mProgressBar.setVisibility(View.GONE);
                                                                mContent.setVisibility(View.VISIBLE);
                                                                mNoMoreFood.setVisibility(View.GONE);
                                                            }

                                                            @Override
                                                            public void onFailure(Call<List<MenuItem>> call, Throwable t) {
                                                                Log.w(TAG, "Failed to get search results from Food DB");
                                                                Log.w(TAG, t.getMessage());

                                                                mProgressBar.setVisibility(View.GONE);
                                                                mContent.setVisibility(View.GONE);
                                                                mNoMoreFood.setVisibility(View.GONE);
                                                            }
                                                        });

                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                        else {
                            Log.d(TAG, "No more food for now LOL :D");
                            mProgressBar.setVisibility(View.GONE);
                            mContent.setVisibility(View.GONE);
                            mNoMoreFood.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }

    public double calculateNextMealCalories(){

        double multiplier = generateMultiplier(); // TODO: have to generate using data provided by googleFIT
        double BMR = calculateBMR();
        String dietPlan[] = {"WL","WG","M"}; //TODO: dietPlan from user
        String userDietPlan = dietPlan[0];
        double percChange = 10; //TODO: percChange from user
        double dailyCalorieRequirement = BMR * multiplier;
        if(userDietPlan.equals("WL")){
            dailyCalorieRequirement -= percChange*dailyCalorieRequirement/100;
        }
        else if(userDietPlan.equals("WG")){
            dailyCalorieRequirement += percChange*dailyCalorieRequirement/100;
        }
        //TODO: get users meals today;
        int todaysIntake = 600;
        int mealsLeft = 2;
        return (dailyCalorieRequirement - todaysIntake) / mealsLeft;

    }

    /**
     *1.2: Sedentary (You don’t move much. No exercise, desk job, lots of TV
     *1.3-1.4: Lightly Active (Active a few days a week, exercise 1-3 days)
     *1.5-1.6: Moderately Active (Where I would assume most people are at.
     Train 4-5 days a week and active lifestyle)
     *1.7-1.8: Very Active (Training hard for a specific sport or purpose 5-6 hours a week.
     Typically one with a hard labor job as well)
     *1.9-2.2: Extremely Active (Endurance training or hard charging athlete who spends
     10 or more hours training a week and/or lots of activity outside of training.
     */
    public double generateMultiplier(){
        //TODO: generate multiplier

        return 1.2;
    }

    public int calculateLBM(){
        int bodyFat = 12; // TODO: need from user
        int weight = 62; // TODO: need from user
        return weight * (100-(bodyFat))/100;
    }

    public double calculateBMR(){
        return 370 + (21.6 * calculateLBM());

    }

    private static void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance();

        Log.d(TAG, "Dataset has " + dataSet.getDataPoints().size() + " datapoints");
        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i(TAG, "Data point:");
            Log.i(TAG, "\tType: " + dp.getDataType().getName());
            Log.i(TAG, "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i(TAG, "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)));
            for (Field field : dp.getDataType().getFields()) {
                Log.i(TAG, "\tField: " + field.getName() + " Value: " + dp.getValue(field));
            }
        }
    }

    private class Restaurant {
        PlacesSearchResult restaurant;

        Restaurant(PlacesSearchResult restaurant) {
            this.restaurant = restaurant;
        }

        @Override
        public boolean equals(Object v) {
            if (v instanceof Restaurant) {
                Restaurant res = (Restaurant) v;
                return this.restaurant != null && res.restaurant != null
                        && this.restaurant.name.equals(res.restaurant.name);
            }

            return false;
        }
    }
}



