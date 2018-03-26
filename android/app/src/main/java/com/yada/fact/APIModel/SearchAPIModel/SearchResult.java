package com.yada.fact.APIModel.SearchAPIModel;

/**
 * Created by yadhuprakash on 3/12/18.
 */
public class SearchResult {


    private List list;

    public List getResultList() {
        return list;
    }

    public SearchResult(List resultList) {

        this.list = resultList;
    }
}
