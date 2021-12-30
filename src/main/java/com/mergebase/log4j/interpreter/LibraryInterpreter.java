package com.mergebase.log4j.interpreter;

import com.mergebase.log4j.core.JarResult;

import java.util.Collection;
import java.util.List;

public interface LibraryInterpreter {

    List<Library> detectLibrary(JarResult jarResult);
    StringBuilder printResults(Collection<Library> log4j2Libs, JarResult jarResult, StringBuilder builder);

}
