package com.yada.fact;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.fitness.data.Field;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<HistoryItem> mHistoryItems;
    private SimpleDateFormat sdf;

    public static class ActivityViewHolder extends RecyclerView.ViewHolder {
        public TextView mTimeTextView;
        public TextView mActivityTextView;
        public TextView mCaloriesExpendedTextView;

        public ActivityViewHolder(LinearLayout v) {
            super(v);
            mTimeTextView = v.findViewById(R.id.time_text_view);
            mActivityTextView = v.findViewById(R.id.activity_text_view);
            mCaloriesExpendedTextView = v.findViewById(R.id.calories_expended_text_view);
        }
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        public TextView mTimeTextView;
        public TextView mFoodItemTextView;
        public TextView mMealTypeTextView;
        public TextView mCaloriesTextView;

        public FoodViewHolder(LinearLayout v) {
            super(v);
            mTimeTextView = v.findViewById(R.id.time_text_view);
            mFoodItemTextView = v.findViewById(R.id.food_item_view);
            mMealTypeTextView = v.findViewById(R.id.meal_type_view);
            mCaloriesTextView = v.findViewById(R.id.calories_text_view);
        }
    }

    public HistoryAdapter() {
        mHistoryItems = new ArrayList<>();
        sdf = new SimpleDateFormat("h:mm a", Locale.US);
    }

    public void setData(List<HistoryItem> historyItems) {
        mHistoryItems = historyItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mHistoryItems.get(position).eating ? 1 : 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == 0) {
            LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_item_view, parent, false);

            vh = new ActivityViewHolder(v);
        }
        else {
            LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.food_item_view, parent, false);

            vh = new FoodViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == 0) {
            ((ActivityViewHolder) holder).mTimeTextView.setText(sdf.format(mHistoryItems.get(position).startTime));
            ((ActivityViewHolder) holder).mActivityTextView.setText(mHistoryItems.get(position).activityItem.activity);
            ((ActivityViewHolder) holder).mCaloriesExpendedTextView.setText(String.format(Locale.US, "%.2f", mHistoryItems.get(position).activityItem.calories));
        }
        else if (holder.getItemViewType() == 1) {
            ((FoodViewHolder) holder).mTimeTextView.setText(sdf.format(mHistoryItems.get(position).startTime));
            ((FoodViewHolder) holder).mFoodItemTextView.setText(mHistoryItems.get(position).foodItem.foodItem);
            ((FoodViewHolder) holder).mMealTypeTextView.setText(HistoryItem.myMap.get(mHistoryItems.get(position).foodItem.mealType));
            ((FoodViewHolder) holder).mCaloriesTextView.setText(String.format(Locale.US, "%.2f", mHistoryItems.get(position).foodItem.nutrients.get(Field.NUTRIENT_CALORIES)));
        }
    }

    @Override
    public int getItemCount() {
        return mHistoryItems.size();
    }
}
