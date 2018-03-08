package com.yada.fact;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.yada.fact.common.logger.Log;
import com.yada.fact.common.logger.LogView;
import com.yada.fact.common.logger.LogWrapper;
import com.yada.fact.common.logger.MessageOnlyLogFilter;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private LogView mLogView;
    private FloatingActionButton mAddMealFab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_fragment, container, false);

        mLogView = view.findViewById(R.id.logview);
        mAddMealFab = view.findViewById(R.id.add_meal_fab);

        mAddMealFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                long startTime = cal.getTimeInMillis();
                cal.add(Calendar.MINUTE, 1);
                long endTime = cal.getTimeInMillis();
                DataSource nutritionSource = new DataSource.Builder()
                        .setAppPackageName(getActivity())
                        .setDataType(DataType.TYPE_NUTRITION)
                        .setType(DataSource.TYPE_RAW)
                        .build();

                DataPoint banana = DataPoint.create(nutritionSource);
                banana.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
                banana.getValue(Field.FIELD_FOOD_ITEM).setString("banana");
                banana.getValue(Field.FIELD_MEAL_TYPE).setInt(Field.MEAL_TYPE_SNACK);
                banana.getValue(Field.FIELD_NUTRIENTS).setKeyValue(Field.NUTRIENT_CALORIES, 105f);

                DataPoint orange = DataPoint.create(nutritionSource);
                orange.setTimeInterval(startTime, endTime, TimeUnit.MILLISECONDS);
                orange.getValue(Field.FIELD_FOOD_ITEM).setString("orange");
                orange.getValue(Field.FIELD_MEAL_TYPE).setInt(Field.MEAL_TYPE_SNACK);
                orange.getValue(Field.FIELD_NUTRIENTS).setKeyValue(Field.NUTRIENT_CALORIES, 45f);

                DataSet dataSet = DataSet.create(nutritionSource);
                dataSet.add(banana);
                dataSet.add(orange);

                Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                        .insertData(dataSet).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Successfully added banana");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to add banana");
                        Log.d(TAG, e.getMessage());
                    }
                });
            }
        });

        // This method sets up our custom logger, which will print all log messages to the device
        // screen, as well as to adb logcat.
        initializeLogging();

        setupFitnessHistory();

        return view;
    }

    /** Initializes a custom log class that outputs both to in-app targets and logcat. */
    private void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);
        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // Fixing this lint errors adds logic without benefit.
        // noinspection AndroidLintDeprecation
        mLogView.setTextAppearance(R.style.Log);

        mLogView.setBackgroundColor(Color.WHITE);
        msgFilter.setNext(mLogView);
        Log.i(TAG, "Ready");
    }

    private void setupFitnessHistory() {
        Task<DataSet> response =
                Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                        .readDailyTotal(DataType.TYPE_CALORIES_EXPENDED);
        response.addOnSuccessListener(new OnSuccessListener<DataSet>() {
            @Override
            public void onSuccess(DataSet dataSet) {
                dumpDataSet(dataSet);
            }
        });

        Calendar cal = Calendar.getInstance();
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
        });
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
