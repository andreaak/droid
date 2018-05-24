package com.andreaak.note.predicates;

import com.andreaak.note.configs.Configs;

import java.io.Serializable;

public class DatabaseNamePredicate implements DirectoryNamePredicate, Serializable {

    @Override
    public boolean isValid(String fileName) {
        return fileName.endsWith(Configs.DatabaseExtension);
    }
}