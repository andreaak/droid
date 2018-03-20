package com.andreaak.cards.predicates;

import com.andreaak.cards.configs.Configs;

import java.io.File;
import java.io.Serializable;

public class LessonXmlPredicate implements DirectoryPredicate, Serializable {

    @Override
    public boolean isValid(File lesson) {
        return lesson.getName().endsWith(Configs.LessonsExtension);
    }
}
