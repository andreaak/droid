package com.andreaak.cards.predicates;

import com.andreaak.cards.configs.Configs;

import java.io.Serializable;

public class LessonFileNamePredicate implements DirectoryNamePredicate, Serializable {
    @Override
    public boolean isValid(String name) {
        return name.startsWith(Configs.LessonsPrefix) && name.endsWith(Configs.LessonsExtension);
    }
}

