package com.example.newfoodappserver.callback;

import com.example.newfoodappserver.model.BestDealsModel;
import com.example.newfoodappserver.model.MostPopularModel;

import java.util.List;

public interface IMostPopularCallbackListener {

    void onListMostPopularLoadSuccess(List<MostPopularModel> mostPopularModels);
    void onListMostPopularLoadFailed(String message );




}
