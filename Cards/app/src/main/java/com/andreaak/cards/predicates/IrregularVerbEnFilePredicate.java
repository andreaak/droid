package com.andreaak.cards.predicates;

import com.andreaak.cards.configs.Configs;

import java.io.File;
import java.io.Serializable;

public class IrregularVerbEnFilePredicate implements DirectoryPredicate, Serializable {

    @Override
    public boolean isValid(File lesson) {
        String name = lesson.getName();
        return name.startsWith(Configs.SP_IRR_VERB_EN_DEFAULT) && name.endsWith(Configs.LessonsExtension);
    }
}
