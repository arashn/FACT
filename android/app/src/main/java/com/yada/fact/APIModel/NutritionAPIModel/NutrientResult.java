package com.yada.fact.APIModel.NutritionAPIModel;

/**
 * Created by yadhuprakash on 3/12/18.
 */

public class NutrientResult {
    Report report;

    public NutrientResult(Report report) {
        this.report = report;
    }

    public Report getReport() {
        return report;
    }
}
