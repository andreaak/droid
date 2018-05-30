package com.andreaak.cards.predicates;

import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.common.predicates.DirectoryNamePredicate;

import java.io.Serializable;

public class IrregularVerbEnFileNamePredicate implements DirectoryNamePredicate, Serializable {
    @Override
    public boolean isValid(String name) {
        return name.startsWith(AppConfigs.SP_IRR_VERB_EN_DEFAULT) && name.endsWith(AppConfigs.LessonsExtension);
    }
}
