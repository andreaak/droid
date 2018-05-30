package com.andreaak.common.predicates;

import java.io.File;

public interface DirectoryPredicate {
    boolean isValid(File directory);
}