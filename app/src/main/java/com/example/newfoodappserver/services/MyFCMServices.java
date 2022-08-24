package com.example.newfoodappserver.services;

import android.content.Intent;

import com.example.newfoodappserver.MainActivity;
import com.example.newfoodappserver.common.Common;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

import androidx.annotation.NonNull;

public class MyFCMServices extends FirebaseMessagingService {


    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        Map<String,String> dataRecv = remoteMessage.getData();
        if (dataRecv!=null)
        {

            if(dataRecv.get(Common.NOTI_TITLE).equals("New Order")){
                    //here we call MainActivity cuz we must assign value for Common.currentUser
                //so thats why we call Main 1st as if we call HomeActivty it will crash  as Common.currentUser can only be assigned in MainActivity
                // after Login

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(Common.IS_OPEN_ACTIVITY_NEW_ORDER,true); // we use extra to detect if app is open from notification
                Common.showNotification(this, new Random().nextInt(),
                        dataRecv.get(Common.NOTI_TITLE),
                        dataRecv.get(Common.NOTI_CONTENT),
                        intent);


            }else {
                Common.showNotification(this, new Random().nextInt(),
                        dataRecv.get(Common.NOTI_TITLE),
                        dataRecv.get(Common.NOTI_CONTENT),
                        null);
            }

        }




    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Common.updateToken(this,s,true,false); // we are on server app so isServer = true


    }





}
