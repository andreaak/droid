package com.andreaak.cards.activities;

import androidx.lifecycle.ViewModel;

import com.andreaak.cards.activities.helpers.SimpleCardActivityHelper;
import com.andreaak.cards.model.WordItem;
import com.andreaak.cards.utils.xml.XmlParser;

public class SimpleCardViewModel extends ViewModel {

    private SimpleCardActivityHelper helper;
    private boolean isExample;
    private boolean isDescription;

    public SimpleCardActivityHelper getHelper() {
        return helper;
    }

    public void setHelper(SimpleCardActivityHelper helper) {
        this.helper = helper;
    }

    public boolean isExample() {
        return isExample;
    }

    public void toggleExample() {
        this.isExample = !this.isExample;
        this.isDescription = false;
    }

    public boolean isDescription() {
        return isDescription;
    }

    public void setDescription(boolean description) {
        this.isDescription = description;
    }

    public void initWordItem() {
        if (helper != null && helper.currentSimpleWord != null && helper.language != null) {
            String lang = helper.language.getPrimaryLanguage();
            WordItem res = XmlParser.getWordItem(helper.currentSimpleWord, lang);
            if (res != null) {
                helper.currentWord = res;
            }
        }
    }
}
