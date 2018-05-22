package com.andreaak.note.predicates;

import java.io.File;

public interface DirectoryPredicate {
    boolean isValid(File directory);
}