package com.mergebase.log4j.interpreter;

import com.mergebase.log4j.core.JarResult;

import java.util.List;
import java.util.Optional;

public final class ResultUtils {

    private ResultUtils() {}

    static Optional<JarResult.ClassResult> classMatchingSuffix(List<JarResult.ClassResult> results, String suffix) {
        return results.stream().filter(result -> result.endsWith(suffix)).findAny();
    }


}
