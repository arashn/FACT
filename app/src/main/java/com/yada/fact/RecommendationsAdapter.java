package com.yada.fact;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecommendationsAdapter extends RecyclerView.Adapter<RecommendationsAdapter.MenuItemViewHolder> {
    private List<MenuItem> mMenuItems;
    public class MenuItemViewHolder extends RecyclerView.ViewHolder {
        public TextView mMenuItemName, mMenuItemRestaurant, mMenuItemCalories;
        public MenuItemViewHolder(LinearLayout v) {
            super(v);
            mMenuItemName = v.findViewById(R.id.menu_item_name);
            mMenuItemRestaurant = v.findViewById(R.id.menu_item_restaurant);
            mMenuItemCalories = v.findViewById(R.id.menu_item_calories);
        }
    }

    public RecommendationsAdapter() {
        mMenuItems = new ArrayList<>();
    }

    public void setData(List<MenuItem> menuItems) {
        mMenuItems = menuItems;
        notifyDataSetChanged();
    }

    @Override
    public MenuItemViewHolder onCreateViewHolder(ViewGroup parent,
                                                 int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.menu_item_view, parent, false);

        return new MenuItemViewHolder(v);

    }

    @Override
    public void onBindViewHolder(MenuItemViewHolder holder, int position) {
        holder.mMenuItemName.setText(mMenuItems.get(position).itemName);
        holder.mMenuItemCalories.setText(Integer.toString(mMenuItems.get(position).calories) + " calories");
        holder.mMenuItemRestaurant.setText(mMenuItems.get(position).restaurantName);
    }

    @Override
    public int getItemCount() {
        return mMenuItems.size();
    }
}
