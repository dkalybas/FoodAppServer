package com.example.newfoodappserver.callback;

import com.example.newfoodappserver.model.CategoryModel;

import java.util.List;

public  interface ICategoryCallbackListener {

    void onCategoryLoadSuccess(List<CategoryModel> categoryModelList);
    void onCategoryLoadFailed(String message);





}
