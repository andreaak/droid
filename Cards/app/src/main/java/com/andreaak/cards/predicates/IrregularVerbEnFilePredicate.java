package com.andreaak.cards.predicates;

import com.andreaak.cards.configs.AppConfigs;
import com.andreaak.common.predicates.DirectoryPredicate;

import java.io.File;
import java.io.Serializable;

public class IrregularVerbEnFilePredicate implements DirectoryPredicate, Serializable {

    @Override
    public boolean isValid(File lesson) {
        String name = lesson.getName();
        return name.startsWith(AppConfigs.SP_IRR_VERB_EN_DEFAULT) && name.endsWith(AppConfigs.getInstance().LessonsExtension);
    }
}
