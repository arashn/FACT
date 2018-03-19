package com.yada.fact.APIModel.SearchAPIModel;

/**
 * Created by yadhuprakash on 3/12/18.
 */

public class Item {
    private String  offset;
    private String group;
    private String name;
    private Integer ndbno;
    private String ds;

    public Item(String offset, String group, String name, Integer ndbno, String ds) {
        this.offset = offset;
        this.group = group;
        this.name = name;
        this.ndbno = ndbno;
        this.ds = ds;
    }

    public String getOffset() {
        return offset;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public Integer getNdbno() {
        return ndbno;
    }

    public String getDs() {
        return ds;
    }



}
