package com.example.newfoodappserver.ui.most_popular;

import com.example.newfoodappserver.callback.IBestDealsCallbackListener;
import com.example.newfoodappserver.callback.IMostPopularCallbackListener;
import com.example.newfoodappserver.common.Common;
import com.example.newfoodappserver.model.BestDealsModel;
import com.example.newfoodappserver.model.MostPopularModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MostPopularViewModel extends ViewModel implements IMostPopularCallbackListener {

    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private MutableLiveData<List<MostPopularModel>>  mostPopularListMutable;
    private IMostPopularCallbackListener mostPopularCallbackListener;

    public MostPopularViewModel() {
        mostPopularCallbackListener = this;
    }


    public MutableLiveData<List<MostPopularModel>> getMostPopularListMutable() {
        if (mostPopularListMutable== null)
            mostPopularListMutable = new MutableLiveData<>();

        loadMostPopular();
        return mostPopularListMutable;


    }

    public void loadMostPopular() {

        List<MostPopularModel> temp = new ArrayList<>();
        DatabaseReference mostPopularRef = FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.MOST_POPULAR);


        mostPopularRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot mostPopularSnapShot:dataSnapshot.getChildren()){

                    MostPopularModel mostPopularModel = mostPopularSnapShot.getValue(MostPopularModel.class);
                    mostPopularModel.setKey(mostPopularSnapShot.getKey());
                    temp.add(mostPopularModel);

                }
                mostPopularCallbackListener.onListMostPopularLoadSuccess(temp);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                mostPopularCallbackListener.onListMostPopularLoadFailed(databaseError.getMessage());
            }
        });

    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onListMostPopularLoadSuccess(List<MostPopularModel> mostPopularModels) {
            mostPopularListMutable.setValue(mostPopularModels);
    }

    @Override
    public void onListMostPopularLoadFailed(String message) {
            messageError.setValue(message);
    }


}