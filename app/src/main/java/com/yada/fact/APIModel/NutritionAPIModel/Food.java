package com.yada.fact.APIModel.NutritionAPIModel;

import java.util.ArrayList;

/**
 * Created by yadhuprakash on 3/12/18.
 */

public class Food {
    private Integer ndbno;
    private String name;
    private String ds;
    private String manu;
    private String ru;
    private ArrayList<Nutrient> nutrients;

    public Food(Integer ndbno, String name, String ds, String manu, String ru, ArrayList<Nutrient> nutrients) {
        this.ndbno = ndbno;
        this.name = name;
        this.ds = ds;
        this.manu = manu;
        this.ru = ru;
        this.nutrients = nutrients;
    }

    public Integer getNdbno() {
        return ndbno;
    }

    public String getName() {
        return name;
    }

    public String getDs() {
        return ds;
    }

    public String getManu() {
        return manu;
    }

    public String getRu() {
        return ru;
    }

    public ArrayList<Nutrient> getNutrients() {
        return nutrients;
    }
}
