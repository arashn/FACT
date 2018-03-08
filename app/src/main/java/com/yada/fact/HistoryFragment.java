package com.yada.fact;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;

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
import com.yada.fact.common.logger.Log;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.text.DateFormat.getTimeInstance;

public class HistoryFragment extends Fragment {
    private static final String TAG = "HistoryFragment";

    private CalendarView mCalendarView;
    private RecyclerView mHistoryItems;
    private HistoryAdapter mHistoryAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_fragment, container, false);

        mCalendarView = view.findViewById(R.id.calendar);
        mHistoryItems = view.findViewById(R.id.history_items);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mHistoryItems.setLayoutManager(layoutManager);

        mHistoryItems.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        mHistoryAdapter = new HistoryAdapter();
        mHistoryItems.setAdapter(mHistoryAdapter);

        mCalendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                Log.d(TAG, "Selected year " + year + " month " + month + " day " + dayOfMonth);
                readHistoryDataForDay(year, month, dayOfMonth);
            }
        });

        Log.d(TAG, "Field types: ");
        for (DataType dataType : DataType.getAggregatesForInput(DataType.TYPE_ACTIVITY_SEGMENT)) {
            Log.d(TAG, dataType.getName());
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        readHistoryDataForDay(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        return view;
    }

    private void readHistoryDataForDay(int year, int month, int dayOfMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, dayOfMonth, 0, 0, 0);
        final long startTime = cal.getTimeInMillis();
        Log.d(TAG, "Start time: " + cal.getTime().toLocaleString());
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        final long endTime = cal.getTimeInMillis();
        Log.d(TAG, "End time: " + cal.getTime().toLocaleString());

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
                .bucketByActivitySegment(1, TimeUnit.MINUTES)
                .build();

        final List<HistoryItem> historyItems = new ArrayList<>();
        final List<DataSet> dataSets = new ArrayList<>();

        Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(readRequest).addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
            @Override
            public void onSuccess(DataReadResponse dataReadResponse) {
                Log.d(TAG, "Got history data for AGGREGATE_CALORIES_EXPENDED");
                //mHistoryAdapter.setData(dataReadResponse.getBuckets());
                Log.d(TAG, "Number of buckets: " + dataReadResponse.getBuckets().size());
                Log.d(TAG, "Number of datasets: " + dataReadResponse.getDataSets().size());
                for (Bucket bucket : dataReadResponse.getBuckets()) {
                    Log.d(TAG, "Bucket: " + bucket.getActivity());
                    DataSet dataSet = bucket.getDataSet(DataType.AGGREGATE_CALORIES_EXPENDED);

                    for (DataPoint dp : dataSet.getDataPoints()) {
                        historyItems.add(new HistoryItem(dp.getStartTime(TimeUnit.MILLISECONDS),
                                dp.getEndTime(TimeUnit.MILLISECONDS),
                                bucket.getActivity(), dp.getValue(Field.FIELD_CALORIES).asFloat()));
                    }

                    dumpDataSet(dataSet);

                    dataSets.add(dataSet);
                }

                DataSet dataSet = dataReadResponse.getDataSet(DataType.AGGREGATE_CALORIES_EXPENDED);

                dumpDataSet(dataSet);

                DataReadRequest readRequest1 = new DataReadRequest.Builder()
                        .read(DataType.TYPE_NUTRITION)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build();

                Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                        .readData(readRequest1).addOnSuccessListener(new OnSuccessListener<DataReadResponse>() {
                    @Override
                    public void onSuccess(DataReadResponse dataReadResponse) {
                        Log.d(TAG, "Got history data for TYPE_NUTRITION");
                        Log.d(TAG, "Number of buckets: " + dataReadResponse.getBuckets().size());
                        Log.d(TAG, "Number of datasets: " + dataReadResponse.getDataSets().size());
                        for (DataSet dataSet1 : dataReadResponse.getDataSets()) {
                            for (DataPoint dp : dataSet1.getDataPoints()) {
                                Map<String, Float> nutrients = new HashMap<>();
                                nutrients.put(Field.NUTRIENT_CALORIES, dp.getValue(Field.FIELD_NUTRIENTS).getKeyValue(Field.NUTRIENT_CALORIES));
                                historyItems.add(new HistoryItem(dp.getStartTime(TimeUnit.MILLISECONDS),
                                        dp.getEndTime(TimeUnit.MILLISECONDS),
                                        dp.getValue(Field.FIELD_FOOD_ITEM).asString(),
                                        dp.getValue(Field.FIELD_MEAL_TYPE).asInt(),
                                        nutrients));
                            }
                            dumpDataSet(dataSet1);

                            dataSets.add(dataSet1);
                        }

                        Collections.sort(historyItems, new Comparator<HistoryItem>() {
                            @Override
                            public int compare(HistoryItem historyItem, HistoryItem t1) {
                                if (historyItem.startTime < t1.startTime) {
                                    return -1;
                                }
                                else if (historyItem.startTime > t1.startTime) {
                                    return 1;
                                }

                                return 0;
                            }
                        });

                        mHistoryAdapter.setData(historyItems);
                    }
                });
            }
        });
    }

    private static void dumpDataSet(DataSet dataSet) {
        Log.i(TAG, "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = getTimeInstance();

        Log.d(TAG, "Number of data points: " + dataSet.getDataPoints().size());
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
