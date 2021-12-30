package com.mergebase.log4j.interpreter;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
class Library {
    String path;
    String name;
    String version;
    String allowlist;
    boolean allowlisted;

    public Library(String path, String name, String version, String allowlist, boolean allowlisted) {
        this.path = path;
        this.name = name;
        this.version = version;
        this.allowlist = allowlist;
        this.allowlisted = allowlisted;
    }
}
