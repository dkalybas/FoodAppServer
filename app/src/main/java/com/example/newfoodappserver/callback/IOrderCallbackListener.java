package com.example.newfoodappserver.callback;

import com.example.newfoodappserver.model.CategoryModel;
import com.example.newfoodappserver.model.OrderModel;

import java.util.List;

public interface IOrderCallbackListener {


    void onOrderLoadSuccess(List<OrderModel> orderModelList);
    void onOrderLoadFailed(String message);







}
