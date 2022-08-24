package com.example.newfoodappserver.callback;

import com.example.newfoodappserver.model.BestDealsModel;

import java.util.List;

public interface IBestDealsCallbackListener {

     void onListBestDealsLoadSuccess(List<BestDealsModel> bestDealsModels);
     void onListBestDealsLoadFailed(String message );

}
