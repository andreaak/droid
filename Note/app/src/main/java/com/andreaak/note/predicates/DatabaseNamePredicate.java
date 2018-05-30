package com.andreaak.note.predicates;

import com.andreaak.common.predicates.DirectoryNamePredicate;
import com.andreaak.note.configs.AppConfigs;

import java.io.Serializable;

public class DatabaseNamePredicate implements DirectoryNamePredicate, Serializable {

    @Override
    public boolean isValid(String fileName) {
        return fileName.endsWith(AppConfigs.getInstance().DatabaseExtension);
    }
}