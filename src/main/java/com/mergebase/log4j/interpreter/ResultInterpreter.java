package com.mergebase.log4j.interpreter;

import com.mergebase.log4j.core.JarResult;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ResultInterpreter {

    private final Log4j1Interpreter log4j1Interpreter = new Log4j1Interpreter();
    private final Log4j2Interpreter log4j2Interpreter = new Log4j2Interpreter();

    /**
     * Interprets a JarResult and returns a report.
     * @param jarResult to be interpreted
     * @return report (String)
     */
    public String interpret(JarResult jarResult) {
        Collection<Library> log4j2Libs = reduceByPath(log4j2Interpreter.detectLibrary(jarResult));
        Collection<Library> log4j1Libs = reduceByPath(log4j1Interpreter.detectLibrary(jarResult));

        StringBuilder builder = new StringBuilder();
        builder = log4j2Interpreter.printResults(log4j2Libs, jarResult, builder);
        builder = log4j1Interpreter.printResults(log4j1Libs, jarResult, builder);
        return builder.toString();
    }

    /**
     * Find and merge Libraries that are the same to avoid duplicated reporting
     */
    private Collection<Library> reduceByPath(Collection<Library> libs) {
        Map<String, Library> reduced = new HashMap<>();
        for(Library lib : libs) {
            String key = calculateLibraryKey(lib);

            if(!reduced.containsKey(key)) {
                // first with that key
                reduced.put(key, lib);
            } else {
                // merge
                Library base = reduced.get(key);
                base.allowlisted |= lib.allowlisted; // libs detected by metadata cannot be whitelisted
                if(lib.allowlist != null && !lib.allowlist.isEmpty()) {
                    base.allowlist = lib.allowlist;
                }
            }
        }
        return reduced.values();
    }

    /**
     * Calculates a key for given library, that is capable of grouping multiple libraries that belong together.
     *
     * For example those matches (library paths) are clearly from the same log4j lib:
     * C:\Users\z004dp7c\Downloads\logstash-7.16.1-windows-x86_64\logstash-7.16.1\logstash-core\lib\jars\log4j-core-2.15.0.jar!/META-INF/maven/org.apache.logging.log4j/log4j-core/pom.properties
     * C:\Users\z004dp7c\Downloads\logstash-7.16.1-windows-x86_64\logstash-7.16.1\logstash-core\lib\jars\log4j-core-2.15.0.jar!/org/apache/logging/log4j/core/LogEvent.class
     *
     * However, it is theoretically possible to have multiple versions of log4j classes inside a single library by
     * relocating one of them to another root package (e.g. shaded.org.apache.logging.log4j...). Those cases will not be
     * grouped to avoid hiding results. Duplicate findings are the lesser evil.
     */
    private String calculateLibraryKey(Library lib) {
        String[] parts = lib.path.split("!");
        String innerPath = parts[parts.length - 1];
        if(innerPath.toLowerCase(Locale.ROOT).startsWith("/meta-inf/maven")) {
            innerPath = innerPath.substring("/meta-inf/maven".length());
        }
        innerPath = innerPath.replace('.', '/');
        String key = innerPath;
        if(innerPath.startsWith("/org/apache/log4j")) {
            key = "/org/apache/log4j";
        }
        if(innerPath.startsWith("/org/apache/logging/log4j")) {
            key = "/org/apache/logging/log4j";
        }
        return key;
    }

}
