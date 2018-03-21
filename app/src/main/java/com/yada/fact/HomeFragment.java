package com.yada.fact;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataDeleteRequest;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.yada.fact.RecommendationsFragment.GOAL_MAINTAIN_WEIGHT;
import static com.yada.fact.RecommendationsFragment.GOAL_WEIGHT_GAIN;
import static com.yada.fact.RecommendationsFragment.GOAL_WEIGHT_LOSS;
import static java.text.DateFormat.getTimeInstance;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private TextView mCaloriesBurned, mCaloriesToConsume, mCaloriesConsumed, mNextMealCaloriesDesc, mNextMealCalories;
    private FloatingActionButton mAddMealFab;
    private Button mClearBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_fragment, container, false);

        mCaloriesBurned = view.findViewById(R.id.daily_calories_burned);
        mCaloriesToConsume = view.findViewById(R.id.daily_calories_consume);
        mCaloriesConsumed = view.findViewById(R.id.daily_calories_consumed);
        mNextMealCaloriesDesc = view.findViewById(R.id.next_meal_calories_desc);
        mNextMealCalories = view.findViewById(R.id.next_meal_calories);
        mAddMealFab = view.findViewById(R.id.add_meal_fab);
        mClearBtn = view.findViewById(R.id.btn_clear);

        mAddMealFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CaptureActivity.class));
            }
        });

        mClearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                long endTime = cal.getTimeInMillis();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long startTime = cal.getTimeInMillis();

                DataDeleteRequest request =
                        new DataDeleteRequest.Builder()
                        .setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS)
                        .addDataType(DataType.TYPE_NUTRITION)
                        .build();

                Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                        .deleteData(request);

                Toast.makeText(getActivity(), "Food log data cleared", Toast.LENGTH_SHORT).show();
            }
        });

        readDailyStats();

        return view;
    }

    private void readDailyStats() {
        Task<DataSet> response =
                Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                        .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED);
        response.addOnSuccessListener(new OnSuccessListener<DataSet>() {
            @Override
            public void onSuccess(DataSet dataSet) {
                DataPoint dataPoint = dataSet.getDataPoints().get(0);
                mCaloriesBurned.setText(String.format(Locale.US, "%.0f", dataPoint.getValue(Field.FIELD_CALORIES).asFloat()));
                dumpDataSet(dataSet);
            }
        });

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
                        mCaloriesConsumed.setText(String.format(Locale.US, "%.0f", numCaloriesConsumed));

                        cal.setTime(new Date());
                        int hour = cal.get(Calendar.HOUR_OF_DAY);
                        Log.d(TAG, "Hour of day: " + hour);
                        if (hour < 12) {
                            numMealsRemaining = 3;
                            mNextMealCaloriesDesc.setText("Calories to consume for breakfast:");
                        }
                        else if (hour >= 12 && hour < 17) {
                            numMealsRemaining = 2;
                            mNextMealCaloriesDesc.setText("Calories to consume for lunch:");
                        }
                        else {
                            numMealsRemaining = 1;
                            mNextMealCaloriesDesc.setText("Calories to consume for dinner:");
                        }

                        Log.d(TAG, "Number of meals remaining: " + numMealsRemaining);

                        float nextMealCalories = Math.max((dailyCalories / numMealsRemaining) - numCaloriesConsumed, 0.0f);

                        Log.d(TAG, "Next meal calories: " + nextMealCalories);
                        if (fitnessGoal == GOAL_MAINTAIN_WEIGHT) {
                            mCaloriesToConsume.setText(dailyCalories > 0 ? String.format(Locale.US, "About %.0f", dailyCalories) : "0");
                            mNextMealCalories.setText(nextMealCalories > 0 ? String.format(Locale.US, "About %.0f", nextMealCalories) : "0");
                        }
                        else if (fitnessGoal == GOAL_WEIGHT_GAIN) {
                            mCaloriesToConsume.setText(dailyCalories > 0 ? String.format(Locale.US, "At least %.0f", dailyCalories) : "0");
                            mNextMealCalories.setText(nextMealCalories > 0 ? String.format(Locale.US, "At least %.0f", nextMealCalories) : "0");
                        }
                        else if (fitnessGoal == GOAL_WEIGHT_LOSS) {
                            mCaloriesToConsume.setText(dailyCalories > 0 ? String.format(Locale.US, "At most %.0f", dailyCalories) : "0");
                            mNextMealCalories.setText(nextMealCalories > 0 ? String.format(Locale.US, "At most %.0f", nextMealCalories) : "0");
                        }
                    }
                });
            }
        });

        /*Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startTime = cal.getTimeInMillis();
        DataReadRequest readRequest = new DataReadRequest.Builder()
                .read(DataType.TYPE_NUTRITION)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();

        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(readRequest).addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
            @Override
            public void onSuccess(DataReadResponse dataReadResponse) {
                for (DataSet dataSet : dataReadResponse.getDataSets()) {
                    dumpDataSet(dataSet);
                }
            }
        });*/
    }

    private static void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getTimeInstance();

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
