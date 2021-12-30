package com.mergebase.log4j.comparators;

import java.io.File;
import java.util.Comparator;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class FileNameComparator implements Comparator<File>{

    @Override
    public int compare(File o1, File o2) {
        
        String s1 = o1 != null ? o1.getName() : "";
        String s2 = o2 != null ? o2.getName() : "";

        int c = s1.compareTo(s2);
        if (c == 0) {
            c = s1.compareTo(s2);
            if (c == 0 && o1 != null) {
                c = o1.compareTo(o2);
            }
        }
        return c;
    }
    
}
