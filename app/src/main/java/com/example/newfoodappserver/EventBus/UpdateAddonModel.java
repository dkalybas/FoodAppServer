package com.example.newfoodappserver.EventBus;

import com.example.newfoodappserver.model.AddonModel;

import java.util.List;

public class UpdateAddonModel {

    private List<AddonModel>  addonModels;

    public UpdateAddonModel() {

    }

    public List<AddonModel> getAddonModels() {
        return addonModels;
    }

    public void setAddonModels(List<AddonModel> addonModels) {
        this.addonModels = addonModels;
    }
}
