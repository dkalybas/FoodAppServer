package com.example.newfoodappserver.ui.discount;

import com.example.newfoodappserver.callback.IDiscountCallbackListener;
import com.example.newfoodappserver.common.Common;
import com.example.newfoodappserver.model.DiscountModel;
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

public class DiscountViewModel extends ViewModel implements IDiscountCallbackListener {

    private MutableLiveData<String> messageError = new MutableLiveData<>();
    private MutableLiveData<List<DiscountModel>>  discountMutableLiveData;
    private IDiscountCallbackListener discountCallbackListener;

    public DiscountViewModel() {

        discountCallbackListener = this ;
    }

    public MutableLiveData<List<DiscountModel>> getDiscountMutableLiveData() {

        if (discountMutableLiveData==null)
            discountMutableLiveData=new MutableLiveData<>();
        loadDiscount();

        return discountMutableLiveData;
    }

    public void loadDiscount() {

        List<DiscountModel> temp = new ArrayList<>();
        DatabaseReference discountRef = FirebaseDatabase.getInstance()
                .getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.DISCOUNT);

        discountRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildren().iterator().hasNext()) {

                    for (DataSnapshot discountSnapshot : dataSnapshot.getChildren()) {

                        DiscountModel discountModel = discountSnapshot.getValue(DiscountModel.class);
                        discountModel.setKey(discountSnapshot.getKey());
                        temp.add(discountModel);

                    }
                    discountCallbackListener.onListDiscountLoadSuccess(temp);

                }else
                    discountCallbackListener.onListDiscountLoadFailed("Empty Data");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                discountCallbackListener.onListDiscountLoadFailed(databaseError.getMessage());

            }
        });

    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    public void setMessageError(MutableLiveData<String> messageError) {
        this.messageError = messageError;
    }

    public void setDiscountMutableLiveData(MutableLiveData<List<DiscountModel>> discountMutableLiveData) {
        this.discountMutableLiveData = discountMutableLiveData;
    }

    @Override
    public void onListDiscountLoadSuccess(List<DiscountModel> discountModelList) {

        discountMutableLiveData.setValue(discountModelList);

    }

    @Override
    public void onListDiscountLoadFailed(String message) {

        if(message.equals("Empty Data"))
            discountMutableLiveData.setValue(null);

        messageError.setValue(message);
    }
}