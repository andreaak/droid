package com.andreaak.cards.activities;

import androidx.lifecycle.ViewModel;

import com.andreaak.cards.activities.helpers.SelectVerbFormHelper;
import com.andreaak.cards.model.VerbForm;
import com.andreaak.cards.utils.AppUtils;

import java.util.ArrayList;

public class SelectVerbFormViewModel extends ViewModel {

    private SelectVerbFormHelper helper;

    public SelectVerbFormHelper getHelper() {
        return helper;
    }

    public void setHelper(SelectVerbFormHelper helper) {
        this.helper = helper;
    }

    public void loadVerbForms(String directory) {
        if (helper == null) {
            helper = new SelectVerbFormHelper();
        }
        if (helper.verbForms == null || helper.verbForms.isEmpty()) {
            ArrayList<VerbForm> verbForms = AppUtils.getVerbForms(directory);
            helper.verbForms = verbForms != null ? verbForms : new ArrayList<>();
        }
    }
}
