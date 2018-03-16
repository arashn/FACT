package com.yada.fact;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

public class RecommendationsFragment extends Fragment {
    private static final String TAG = "RecommendationsFragment";

    public static final int GOAL_MAINTAIN_WEIGHT = 0;
    public static final int GOAL_WEIGHT_GAIN = 1;
    public static final int GOAL_WEIGHT_LOSS = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.recommendations_fragment, container, false);

        getDailyCalorieIntake();

        return view;
    }

    private void getDailyCalorieIntake() {
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
                boolean foundDataPoint = false;
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

                int numPounds = -1;
                int timePeriod = 7;

                final float dailyCalories = ((numPounds * 3500) / timePeriod) + avgCaloriesBurned;

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
                        if (hour < 12) {
                            numMealsRemaining = 3;
                        }
                        else if (hour >= 12 && hour < 17) {
                            numMealsRemaining = 2;
                        }
                        else {
                            numMealsRemaining = 1;
                        }

                        Log.d(TAG, "Number of meals remaining: " + numMealsRemaining);

                        float nextMealCalories = dailyCalories - numCaloriesConsumed / numMealsRemaining;

                        Log.d(TAG, "Next meal calories: " + nextMealCalories);
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
}



