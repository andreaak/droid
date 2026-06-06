package com.andreaak.common.predicates;

import java.io.File;
import java.io.Serializable;

public class StudyFilesPredicate implements DirectoryPredicate, Serializable {

    @Override
    public boolean isValid(File directory) {
        return true;
    }
}
