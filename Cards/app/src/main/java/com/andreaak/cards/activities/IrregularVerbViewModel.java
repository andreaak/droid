package com.andreaak.cards.activities;

import androidx.lifecycle.ViewModel;

import com.andreaak.cards.activities.helpers.VerbActivityHelper;
import com.andreaak.cards.model.VerbItem;
import com.andreaak.cards.utils.AppUtils;
import com.andreaak.common.utils.Utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;

public class IrregularVerbViewModel extends ViewModel {

    private VerbActivityHelper helper;

    public VerbActivityHelper getHelper() {
        return helper;
    }

    public void setHelper(VerbActivityHelper helper) {
        this.helper = helper;
    }

    public List<String> getLevels(ArrayList<VerbItem> words) {
        Set<String> s = new TreeSet<>();

        if (words != null) {
            for (VerbItem w : words) {
                if (!Utils.isEmpty(w.getLevel())) {
                    s.add(w.getLevel());
                } else {
                    s.add("CC");
                }
            }
        }

        List<String> list = new ArrayList<>(s);
        list.add(0, IrregularVerbActivity.ALL);
        list.add(IrregularVerbActivity.A1B2);
        return list;
    }

    public Queue<String> getSoundFiles(String language) {
        Queue<String> files = new ArrayDeque<>();
        if (helper == null || helper.currentWord == null) {
            return files;
        }

        List<String> words = AppUtils.getWords(helper.currentWord._1);
        words.addAll(AppUtils.getWords(helper.currentWord._2));
        words.addAll(AppUtils.getWords(helper.currentWord._3));
        if (!Utils.isEmpty(helper.currentWord._4)) {
            words.addAll(AppUtils.getWords(helper.currentWord._4));
        }

        for (String word : words) {
            String fileTemplate = AppUtils.getVerbSoundFile(language, word);
            boolean added = AppUtils.addSoundFile(files, fileTemplate);

            if (!added) {
                fileTemplate = AppUtils.getSoundFile(language, word);
                AppUtils.addSoundFile(files, fileTemplate);
            }
        }

        return files;
    }
}
