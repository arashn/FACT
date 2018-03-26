package com.yada.fact.APIModel.NutritionAPIModel;

import java.util.ArrayList;

/**
 * Created by yadhuprakash on 3/12/18.
 */

public class Nutrient {

    private String nutrient_id;
    private String name;
    private String derivation;
    private String group;
    private String unit;
    private String value;
    private ArrayList<Measure> measures;

    public Nutrient(String nutrient_id, String name, String derivation, String group, String unit, String value, ArrayList<Measure> measures) {
        this.nutrient_id = nutrient_id;
        this.name = name;
        this.derivation = derivation;
        this.group = group;
        this.unit = unit;
        this.value = value;
        this.measures = measures;
    }

    public String getNutrient_id() {
        return nutrient_id;
    }

    public String getName() {
        return name;
    }

    public String getDerivation() {
        return derivation;
    }

    public String getGroup() {
        return group;
    }

    public String getUnit() {
        return unit;
    }

    public String getValue() {
        return value;
    }

    public ArrayList<Measure> getMeasures() {
        return measures;
    }
}
