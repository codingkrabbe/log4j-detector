package com.mergebase.log4j.core;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@ToString
public class JarResult {

    private final List<MetaResult> metaResults = new ArrayList<>();
    private final List<ClassResult> classResults = new ArrayList<>();

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @ToString
    public static class MetaResult {
        private String path;
        private String library;
        private String version;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Getter
    @ToString
    public static class ClassResult {
        private String normalizedPath;
        private String fullPath;
        // "Invalid JNDI URI - {}"
        private boolean containsInvalidJNDIUri;
        private boolean allowlisted;
        private String allowlistName;
        private String hash;

        public boolean endsWith(String suffix) {
            return normalizedPath.endsWith(suffix);
        }
    }
}
