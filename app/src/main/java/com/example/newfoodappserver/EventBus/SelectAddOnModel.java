package com.example.newfoodappserver.EventBus;

import com.example.newfoodappserver.model.AddonModel;

public class SelectAddOnModel {

    private AddonModel addonModel;

    public SelectAddOnModel(AddonModel addonModel) {
        this.addonModel = addonModel;
    }


    public AddonModel getAddonModel() {
        return addonModel;
    }

    public void setAddonModel(AddonModel addonModel) {
        this.addonModel = addonModel;
    }
}
