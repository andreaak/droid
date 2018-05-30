package com.andreaak.cards.predicates;

import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.common.predicates.DirectoryPredicate;

import java.io.File;
import java.io.Serializable;

public class LessonFilePredicate implements DirectoryPredicate, Serializable {

    @Override
    public boolean isValid(File lesson) {
        String name = lesson.getName();
        return name.startsWith(AppConfigs.LessonsPrefix) && name.endsWith(AppConfigs.LessonsExtension);
    }
}