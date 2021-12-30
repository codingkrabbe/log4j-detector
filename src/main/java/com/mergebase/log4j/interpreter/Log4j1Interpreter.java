package com.mergebase.log4j.interpreter;

import com.mergebase.log4j.core.JarResult;
import com.mergebase.log4j.core.ScanPaths;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static com.mergebase.log4j.interpreter.ResultUtils.classMatchingSuffix;

public class Log4j1Interpreter implements LibraryInterpreter {
    @Override
    public List<Library> detectLibrary(JarResult jarResult) {
        return detectLog4j1(jarResult);
    }

    @Override
    public StringBuilder printResults(Collection<Library> log4j2Libs, JarResult jarResult, StringBuilder builder) {
        return printLog4j1Results(log4j2Libs, jarResult, builder);
    }

    private StringBuilder printLog4j1Results(Collection<Library> log4j1Libs, JarResult jarResult, StringBuilder builder) {
        boolean jmsAppenderPresent = classMatchingSuffix(jarResult.getClassResults(), ScanPaths.FILE_LOG4J1_JMS_APPENDER).isPresent();
        for (Library lib : log4j1Libs) {
            builder = printLog4j1Result(lib, jarResult, jmsAppenderPresent, builder);
        }
        return builder;
    }

    private StringBuilder printLog4j1Result(Library log4j1Lib, JarResult jarResult,
                                            boolean jmsAppenderPresent, StringBuilder builder) {
        builder.append(log4j1Lib.path)
                .append(String.format(" contains Log4J-1.x    %s", log4j1Lib.version));

        if(log4j1Lib.allowlisted) {
            builder.append(" __FALSE_POSITIVE__");
        } else {
            builder.append(" __CVE_2021_4104__");
            if(!jmsAppenderPresent) {
                builder.append(" (__MITIGATED_BY_JMSAPPENDER_REMOVAL__)");
            }
            builder.append(" __CVE_2019_17571__");
        }
        return builder.append("\n");
    }

    private List<Library> detectLog4j1(JarResult jarResult) {
        List<JarResult.ClassResult> classResults = jarResult.getClassResults();
        List<JarResult.MetaResult> metaResults = jarResult.getMetaResults();

        List<Library> libs = metaResults.stream().filter(result -> result.getLibrary().endsWith(":log4j"))
                .map(result -> new Library(result.getPath(), result.getLibrary(), result.getVersion(), "", false))
                .collect(Collectors.toList());

        if(!libs.isEmpty()) {
            return libs;
        }

        return classResults.stream()
                .filter(result -> result.endsWith(ScanPaths.FILE_LOG4J_V1X))
                .map(classResult -> new Library(classResult.getFullPath(), "log4j", "1.x",
                        classResult.getAllowlistName(), classResult.isAllowlisted()
                ))
                .collect(Collectors.toList());
    }
}
