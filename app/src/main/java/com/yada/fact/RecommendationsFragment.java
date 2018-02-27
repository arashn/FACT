package com.yada.fact;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RecommendationsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.recommendations_fragment, container, false);
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


}



