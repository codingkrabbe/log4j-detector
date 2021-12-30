package com.mergebase.log4j.interpreter;

import com.mergebase.log4j.core.JarResult;
import com.mergebase.log4j.core.ScanPaths;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.mergebase.log4j.interpreter.ResultUtils.classMatchingSuffix;
import static com.mergebase.log4j.utils.CollectionUtils.allFilteredMatchNonEmpty;

public class Log4j2Interpreter implements LibraryInterpreter {
    @Override
    public List<Library> detectLibrary(JarResult jarResult) {
        List<JarResult.ClassResult> classResults = jarResult.getClassResults();
        List<JarResult.MetaResult> metaResults = jarResult.getMetaResults();

        List<Library> libs = metaResults.stream().filter(result -> result.getLibrary().endsWith(":log4j-core"))
                .map(result -> new Library(result.getPath(), result.getLibrary(), result.getVersion(), "", false))
                .collect(Collectors.toList());

        Optional<JarResult.ClassResult> result1 = classMatchingSuffix(classResults, ScanPaths.FILE_LOG4J_1);
        Optional<JarResult.ClassResult> result2 = classMatchingSuffix(classResults, ScanPaths.FILE_LOG4J_2);
        Optional<JarResult.ClassResult> result3 = classMatchingSuffix(classResults, ScanPaths.FILE_LOG4J_3);
        Optional<JarResult.ClassResult> result4 = classMatchingSuffix(classResults, ScanPaths.FILE_LOG4J_4);
        Optional<JarResult.ClassResult> result5 = classMatchingSuffix(classResults, ScanPaths.FILE_LOG4J_5);


        if(result1.isPresent()
                && result2.isPresent()
                && result3.isPresent()
                && result4.isPresent()
                && result5.isPresent()) {
            String path = classResults.stream()
                    .filter(result -> result.endsWith(ScanPaths.FILE_LOG4J_1))
                    .findAny()
                    .map(JarResult.ClassResult::getFullPath)
                    .orElse("");
            String version = guessLog4j2Version(classResults);
            boolean allowlisted = result1.get().isAllowlisted()
                    && result2.get().isAllowlisted()
                    && result3.get().isAllowlisted()
                    && result4.get().isAllowlisted()
                    && result5.get().isAllowlisted();
            libs.add(new Library(path, "org.apache.logging.log4j:log4j-core", version, result1.get().getAllowlistName(), allowlisted));
        }

        return libs;
    }

    private String guessLog4j2Version(List<JarResult.ClassResult> classResults) {
        boolean version2_10Plus = classMatchingSuffix(classResults, ScanPaths.FILE_LOG4J_2_10).isPresent();
        boolean version2_15Plus = allFilteredMatchNonEmpty(
                classResults,
                result -> result.endsWith(ScanPaths.FILE_LOG4J_SAFE_CONDITION1),
                JarResult.ClassResult::isContainsInvalidJNDIUri
        );
        boolean version2_16_plus = allFilteredMatchNonEmpty(
                classResults,
                result -> result.endsWith(ScanPaths.FILE_LOG4J_6),
                result -> ScanPaths.FILE_LOG4J_6_DIGEST_V_2_16_0.equals(result.getHash())
        );
        boolean version2_17_plus = allFilteredMatchNonEmpty(
                classResults,
                result -> result.endsWith(ScanPaths.FILE_LOG4J_6),
                result -> ScanPaths.FILE_LOG4J_6_DIGEST_V_2_17_0.equals(result.getHash())
        );
        boolean version2_12_2 = allFilteredMatchNonEmpty(
                classResults,
                result -> result.endsWith(ScanPaths.FILE_LOG4J_6),
                result -> ScanPaths.FILE_LOG4J_6_DIGEST_V_2_12_2.equals(result.getHash())
        );
        boolean version2_12_3 = allFilteredMatchNonEmpty(
                classResults,
                result -> result.endsWith(ScanPaths.FILE_LOG4J_6),
                result -> ScanPaths.FILE_LOG4J_6_DIGEST_V_2_12_3.equals(result.getHash())
        );
        boolean version2_3_1 = allFilteredMatchNonEmpty(
                classResults,
                result -> result.endsWith(ScanPaths.FILE_LOG4J_6),
                result -> ScanPaths.FILE_LOG4J_6_DIGEST_V_2_3_1.equals(result.getHash())
        );
        if(version2_17_plus) {
            return ">= 2.17.0";
        }
        if(version2_3_1) {
            return "2.3.1";
        }
        if(version2_12_3) {
            return "2.12.3";
        }
        if(version2_12_2) {
            return "2.12.2";
        }
        if(version2_16_plus) {
            return "2.16.0";
        }
        if(version2_15Plus) {
            return "2.15.0";
        }
        if(version2_10Plus) {
            return ">= 2.10.0";
        }
        return "< 2.10.0";
    }

    @Override
    public StringBuilder printResults(Collection<Library> log4j2Libs, JarResult jarResult, StringBuilder builder) {
        boolean jndiLookupPresent = classMatchingSuffix(jarResult.getClassResults(), ScanPaths.FILE_LOG4J_VULNERABLE).isPresent();
        List<Library> distinctLibraries = log4j2Libs.stream().distinct().collect(Collectors.toList());
        for (Library lib : distinctLibraries) {
            builder = printResult(lib, jarResult, jndiLookupPresent, builder);
        }
        return builder;
    }

    private StringBuilder printResult(Library log4j2Lib, JarResult jarResult,
                                            boolean jndiLookupPresent, StringBuilder builder) {
        builder.append(log4j2Lib.path).append(" contains Log4J-2.x   ")
                .append(log4j2Lib.version);
        if(log4j2Lib.version.equals("2.17.0")
                || log4j2Lib.version.equals("2.17.1")
                || log4j2Lib.version.equals("2.18.0")
                || log4j2Lib.version.equals(">= 2.17.0")
                || log4j2Lib.version.equals("2.12.3")
                || log4j2Lib.version.equals("2.3.1")) {
            builder.append(" __UP_TO_DATE__");
        } else {
            builder.append(" __CVE_2021_45105__");
            if(log4j2Lib.version.equals("2.15.0")) {
                builder.append(" __CVE_2021_45046__");
                if(!jndiLookupPresent) {
                    builder.append(" (__MITIGATED_BY_JNDILOOKUP_REMOVAL__)");
                }
            }
            if(!log4j2Lib.version.equals("2.16.0")
                    && !log4j2Lib.version.equals("2.12.2")
                    && !log4j2Lib.version.equals("2.15.0")) {
                builder.append(" __CVE_2021_44228__");
                if(!jndiLookupPresent) {
                    builder.append(" (__MITIGATED_BY_JNDILOOKUP_REMOVAL__)");
                }
            }
        }
        return builder.append("\n");
    }
}
