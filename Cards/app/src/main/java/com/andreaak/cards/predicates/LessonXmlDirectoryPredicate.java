package com.andreaak.cards.predicates;

import com.andreaak.cards.utils.Utils;

import java.io.File;
import java.io.Serializable;

public class LessonXmlDirectoryPredicate implements DirectoryPredicate, Serializable {

    @Override
    public boolean isValid(File directory) {
        return Utils.getLessons(directory.getAbsolutePath()).length != 0;
    }
}
