package com.andreaak.cards.predicates;

import com.andreaak.cards.utils.Utils;

import java.io.File;
import java.io.Serializable;

public class AlwaysTruePredicate implements DirectoryPredicate, Serializable {

    @Override
    public boolean isValid(File directory) {
        return true;
    }
}
