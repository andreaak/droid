package com.andreaak.note.predicates;

import com.andreaak.common.predicates.DirectoryPredicate;
import com.andreaak.note.configs.AppConfigs;

import java.io.File;
import java.io.Serializable;

public class DatabasePredicate implements DirectoryPredicate, Serializable {

    @Override
    public boolean isValid(File file) {
        return file.getName().endsWith(AppConfigs.getInstance().DatabaseExtension);
    }
}
