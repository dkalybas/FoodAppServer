package com.example.newfoodappserver.callback;

import com.example.newfoodappserver.model.DiscountModel;

import java.util.List;

public interface IDiscountCallbackListener {

        void onListDiscountLoadSuccess(List<DiscountModel> discountModelList);
        void onListDiscountLoadFailed(String message);



}
