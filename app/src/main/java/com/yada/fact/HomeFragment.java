package com.yada.fact;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.yada.fact.common.logger.Log;
import com.yada.fact.common.logger.LogView;
import com.yada.fact.common.logger.LogWrapper;
import com.yada.fact.common.logger.MessageOnlyLogFilter;

import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private LogView mLogView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_fragment, container, false);

        mLogView = view.findViewById(R.id.logview);

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
                Fitness.getHistoryClient(getContext(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                        .readDailyTotalFromLocalDevice(DataType.TYPE_CALORIES_EXPENDED);
        response.addOnSuccessListener(new OnSuccessListener<DataSet>() {
            @Override
            public void onSuccess(DataSet dataSet) {
                dumpDataSet(dataSet);
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
