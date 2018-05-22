package com.andreaak.note.predicates;

import com.andreaak.note.configs.Configs;

import java.io.File;
import java.io.Serializable;

/**
 * Created by ANDREA on 5/22/2018.
 */
public class DatabasePredicate implements DirectoryPredicate, Serializable {

    @Override
    public boolean isValid(File file) {
        return file.getName().endsWith(Configs.DatabaseExtension);
    }
}
