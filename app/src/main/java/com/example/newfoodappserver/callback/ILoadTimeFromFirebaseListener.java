package com.example.newfoodappserver.callback;

public interface ILoadTimeFromFirebaseListener {

    void onLoadOnlyTimeSuccess(long estimateTimeInMs);
    void onLoadTimeFailed(String message);

}
