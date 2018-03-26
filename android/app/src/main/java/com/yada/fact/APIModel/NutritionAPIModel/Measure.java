package com.yada.fact.APIModel.NutritionAPIModel;

/**
 * Created by yadhuprakash on 3/12/18.
 */

public class Measure {
    private String label;
    private String eqv;
    private String qty;
    private String eunit;
    private String value;

    public Measure(String label, String eqv, String qty, String eunit, String value) {
        this.label = label;
        this.eqv = eqv;
        this.qty = qty;
        this.eunit = eunit;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getEqv() {
        return eqv;
    }

    public String getQty() {
        return qty;
    }

    public String getEunit() {
        return eunit;
    }

    public String getValue() {
        return value;
    }
}
