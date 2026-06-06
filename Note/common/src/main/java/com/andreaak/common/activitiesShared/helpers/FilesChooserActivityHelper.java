package com.andreaak.common.activitiesShared.helpers;

import com.andreaak.common.predicates.DirectoryPredicate;

import java.io.File;
import java.util.ArrayList;

public class FilesChooserActivityHelper {
    public DirectoryPredicate predicate;
    public String title;
    public File currentDir;
    public ArrayList<Integer> selectedPositions = new ArrayList<Integer>();
}
