package com.example.newfoodappserver.ui.category;

import com.example.newfoodappserver.callback.ICategoryCallbackListener;
import com.example.newfoodappserver.common.Common;
import com.example.newfoodappserver.model.CategoryModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CategoryViewModel extends ViewModel implements ICategoryCallbackListener {

    private MutableLiveData<List<CategoryModel>> categoryListMutable;
    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private ICategoryCallbackListener categoryCallbackListener;


    public CategoryViewModel() {
        categoryCallbackListener =this;

    }
    public MutableLiveData<List<CategoryModel>> getCategoryListMutable() {
        if (categoryListMutable==null){

            categoryListMutable = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadCategories();

        }
        return categoryListMutable;
    }

    public void loadCategories() {

        List<CategoryModel> tempList = new ArrayList<>();
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.CATEGORY_REF);
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {

                    for (DataSnapshot itemSnapShot : snapshot.getChildren()) {



                        //Evala exception kai try catch allios  ekane crash  se afto to kommati
                  try {
                      CategoryModel categoryModel = itemSnapShot.getValue(CategoryModel.class);
                      categoryModel.setMenu_id(itemSnapShot.getKey());
                      tempList.add(categoryModel);
                  }catch (Exception e){
                      e.getMessage();
                  }

                    }

                    if (tempList.size() > 0 )


                         categoryCallbackListener.onCategoryLoadSuccess(tempList);

                    else
                        categoryCallbackListener.onCategoryLoadFailed("Category is empty");
                }
                else
                    categoryCallbackListener.onCategoryLoadFailed("Category does not exist !!");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                categoryCallbackListener.onCategoryLoadFailed(error.getMessage());

            }
        });


    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }



    @Override
    public void onCategoryLoadSuccess(List<CategoryModel> categoryModelList) {
            categoryListMutable.setValue(categoryModelList);
    }

    @Override
    public void onCategoryLoadFailed(String message) {
            messageError.setValue(message);
    }
}