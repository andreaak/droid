package com.andreaak.cards.predicates;

import com.andreaak.cards.configs.Configs;

import java.io.Serializable;

public class IrregularVerbEnFileNamePredicate implements DirectoryNamePredicate, Serializable {
    @Override
    public boolean isValid(String name) {
        return Configs.SP_IRR_VERB_EN_DEFAULT.equals(name);
    }
}
