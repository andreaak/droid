package com.andreaak.cards.predicates;

import com.andreaak.cards.configs.Configs;

import java.io.File;
import java.io.Serializable;

public class LessonFilePredicate implements DirectoryPredicate, Serializable {

    @Override
    public boolean isValid(File lesson) {
        String name = lesson.getName();
        return name.startsWith(Configs.LessonsPrefix) && name.endsWith(Configs.LessonsExtension);
    }
}