package com.andreaak.cards.predicates;

import java.io.File;

public interface DirectoryPredicate {
    boolean isValid(File directory);
}
