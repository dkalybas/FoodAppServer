package com.example.newfoodappserver.ui.order;

import com.example.newfoodappserver.callback.IOrderCallbackListener;
import com.example.newfoodappserver.common.Common;
import com.example.newfoodappserver.model.OrderModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OrderViewModel extends ViewModel implements IOrderCallbackListener {

    private MutableLiveData<List<OrderModel>> orderModelMutableLiveData ;
    private MutableLiveData<String> messageError ;

    private IOrderCallbackListener listener;

    public OrderViewModel() {
        orderModelMutableLiveData = new MutableLiveData<>();
        messageError = new MutableLiveData<>();
        listener =this;
    }

    public MutableLiveData<List<OrderModel>> getOrderModelMutableLiveData() {

        loadOrderByStatus(0);
        return orderModelMutableLiveData;
    }

    public void loadOrderByStatus(int i) {
        List<OrderModel> tempList = new ArrayList<>();
        Query  orderRef = FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                .child(Common.currentServerUser.getRestaurant())
                .child(Common.ORDER_REF)
                .orderByChild("orderStatus")
                .equalTo(i);
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot itemSnapShot:dataSnapshot.getChildren()){

                    OrderModel orderModel = itemSnapShot.getValue(OrderModel.class);
                    orderModel.setKey(itemSnapShot.getKey());
                    tempList.add(orderModel);


                }
                listener.onOrderLoadSuccess(tempList);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onOrderLoadFailed(databaseError.getMessage());
            }
        });

    }

    public void setOrderModelMutableLiveData(MutableLiveData<List<OrderModel>> orderModelMutableLiveData) {
        this.orderModelMutableLiveData = orderModelMutableLiveData;
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    public void setMessageError(MutableLiveData<String> messageError) {
        this.messageError = messageError;
    }

    @Override
    public void onOrderLoadSuccess(List<OrderModel> orderModelList) {

        if (orderModelList.size() > 0 ){

            Collections.sort(orderModelList,(orderModel,t1)->{

                if (orderModel.getCreateDate() < t1.getCreateDate())
                    return -1;

                return orderModel.getCreateDate() == t1.getCreateDate() ? 0:1 ;

            });


        }

         orderModelMutableLiveData.setValue(orderModelList);
    }

    @Override
    public void onOrderLoadFailed(String message) {
        messageError.setValue(message);
    }
}