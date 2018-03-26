package com.yada.fact.APIModel.SearchAPIModel;

import java.util.ArrayList;

/**
 * Created by yadhuprakash on 3/12/18.
 */

public class List {

    private String  q;
    private String  sr;
    private String  ds;
    private String  start;
    private String  end;
    private String  total;
    private String  group;
    private String  sort;

    private ArrayList<Item> item;


    public List(String q, String sr, String ds, String start, String end, String total, String group, String sort) {

        this.q = q;
        this.sr = sr;
        this.ds = ds;
        this.start = start;
        this.end = end;
        this.total = total;
        this.group = group;
        this.sort = sort;
        this.item = item;

    }

    public ArrayList<Item> getListOfItems() {
        return item;
    }

    public String getQ() {
        return q;
    }

    public String getSr() {
        return sr;
    }

    public String getDs() {
        return ds;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getTotal() {
        return total;
    }

    public String getGroup() {
        return group;
    }

    public String getSort() {
        return sort;
    }

    public ArrayList<Item> getItem() {
        return item;
    }

}
