package com.andreaak.cards.predicates;

import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.common.predicates.DirectoryNamePredicate;

import java.io.Serializable;

public class LessonFileNamePredicate implements DirectoryNamePredicate, Serializable {
    @Override
    public boolean isValid(String name) {
        return name.startsWith(AppConfigs.getInstance().LessonsPrefix) && name.endsWith(AppConfigs.getInstance().LessonsExtension);
    }
}

