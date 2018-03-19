package com.andreaak.cards.utils;

import java.io.File;

public interface DirectoryPredicate {
    boolean isValid(File directory);
}
