package com.example.newfoodappserver.callback;

import android.widget.Button;
import android.widget.RadioButton;

import com.example.newfoodappserver.model.OrderModel;
import com.example.newfoodappserver.model.ShipperModel;

import java.util.List;

import androidx.appcompat.app.AlertDialog;

public interface IShipperLoadCallbackListener {
    void onShipperLoadSuccess(List<ShipperModel> shipperModelList);
    void onShipperLoadSuccess(int pos, OrderModel orderModel,List<ShipperModel> tempList, AlertDialog dialog,
                              Button btn_ok, Button btn_cancel, RadioButton rdi_shipping, RadioButton rdi_shipped,
                              RadioButton rdi_cancelled, RadioButton rdi_delete, RadioButton rdi_restore_placed);

    void onShipperLoadFailed(String message);


}
