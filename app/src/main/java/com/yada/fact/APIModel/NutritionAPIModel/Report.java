package com.yada.fact.APIModel.NutritionAPIModel;

/**
 * Created by yadhuprakash on 3/12/18.
 */

public class Report {
    private String sr;
    private String type;
    private Food food;
    private String[] footnotes;

    public Report(String sr, String type, Food food, String[] footnotes) {
        this.sr = sr;
        this.type = type;
        this.food = food;
        this.footnotes = footnotes;
    }

    public String getSr() {
        return sr;
    }

    public String getType() {
        return type;
    }

    public Food getFood() {
        return food;
    }

    public String[] getFootnotes() {
        return footnotes;
    }
}
