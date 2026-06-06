package com.andreaak.common.predicates;

import java.io.Serializable;

public class CompositeFileNamePredicate implements DirectoryNamePredicate, Serializable {

    private DirectoryNamePredicate[] predicates;

    public CompositeFileNamePredicate(DirectoryNamePredicate... predicates) {
        this.predicates = predicates;
    }

    @Override
    public boolean isValid(String name) {
        for (DirectoryNamePredicate predicate : predicates) {
            if (predicate.isValid(name)) {
                return true;
            }
        }
        return false;
    }
}
