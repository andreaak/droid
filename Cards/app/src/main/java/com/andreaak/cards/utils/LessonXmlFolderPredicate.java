package com.andreaak.cards.utils;

import java.io.File;
import java.io.Serializable;

public class LessonXmlFolderPredicate implements DirectoryPredicate, Serializable {

    @Override
    public boolean isValid(File directory) {
        return Utils.getLessons(directory.getAbsolutePath()).length != 0;
    }
}
