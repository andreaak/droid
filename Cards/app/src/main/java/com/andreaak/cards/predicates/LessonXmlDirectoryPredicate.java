package com.andreaak.cards.predicates;

import com.andreaak.cards.utils.AppUtils;
import com.andreaak.common.predicates.DirectoryPredicate;

import java.io.File;
import java.io.Serializable;

public class LessonXmlDirectoryPredicate implements DirectoryPredicate, Serializable {

    @Override
    public boolean isValid(File directory) {
        return AppUtils.getLessons(directory.getAbsolutePath()).length != 0;
    }
}
