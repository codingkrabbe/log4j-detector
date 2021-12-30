package com.mergebase.log4j;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mergebase.log4j.core.FileAnalyzer;
import com.mergebase.log4j.core.ScanPaths;

public class Log4JDetector {

    public static boolean matchesTrueConfig(String text) {
    	return text.toLowerCase().matches(ScanPaths.CONFIG_REGEX);
    }

    static Logger logger = Logger.getLogger(Log4JDetector.class.getSimpleName());
    static final String LINE_SEPARATOR = "line.separator";

    public static void main(String[] args) {        

        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        Iterator<String> it = argsList.iterator();
        boolean verboseMode = false;
        boolean vulnerabilityFound = false;

        verboseMode = parseApplicationArgs(it);

        if (argsList.isEmpty()) {
            printHelpInfo();
        }

        //Process scan
        for (String arg : argsList) {
            FileAnalyzer analyer = new FileAnalyzer(new File(arg));
            if(verboseMode)
                analyer.enableVerboseMode();
            analyer.scan();
            vulnerabilityFound |= analyer.isOverallScanResult();
        }
        
        if(!vulnerabilityFound) {
           logger.log(Level.INFO, "-- No vulnerable Log4J 2.x samples found in supplied paths: {0}", argsList);
           logger.log(Level.INFO, "-- Congratulations, the supplied paths are not vulnerable to CVE-2021-44228 !");

           System.out.println("-- No vulnerable Log4J 2.x samples found in supplied paths: " + String.join(", ", argsList));
           System.out.println("-- Congratulations, the supplied paths are not vulnerable to CVE-2021-44228 !");
        }
    }

    private static void printHelpInfo() {
        StringBuilder builder = new StringBuilder(System.getProperty(LINE_SEPARATOR));
            builder.append("Usage: java -jar log4j-detector-2021.12.13.jar [--verbose] [paths to scan...]");
            builder.append(System.getProperty(LINE_SEPARATOR));
            builder.append("Exit codes:  0 = No vulnerable Log4J versions found.\n");
            builder.append("             2 = At least one vulnerable Log4J version found.\n");
            builder.append(System.getProperty(LINE_SEPARATOR));
            builder.append("About - MergeBase log4j detector (version 2021.12.13)\n");
            builder.append("Docs  - https://github.com/mergebase/log4j-detector\n");
            builder.append("(C) Copyright 2021 Mergebase Software Inc. Licensed to you via GPLv3.");
            builder.append(System.getProperty(LINE_SEPARATOR));
            logger.log(Level.INFO, "{0}", builder);
            System.exit(100);
    }

    private static boolean parseApplicationArgs(Iterator<String> it) {
        boolean verboseModeEnabled = false;
        
        while (it.hasNext()) {
            final String argOrig = it.next();
            if ("--verbose".equals(argOrig)) {
                verboseModeEnabled = true;
                it.remove();
            } 
            else {                
                File f = new File(argOrig);
                try {
                    if (!f.exists())
                        logger.log(Level.INFO, "Invalid file: [{0}] Continue with the next...", f.getPath());
                }
                catch(SecurityException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }                
            }
        }
        return verboseModeEnabled;
    }
} 
