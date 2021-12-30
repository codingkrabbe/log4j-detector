package com.mergebase.log4j.core;

import java.util.*;

import com.mergebase.log4j.utils.Bytes;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ScanPaths {

    public static final Set<String> ZIP_FILE_ENDINGS = new HashSet<>();

    public static final String FILE_LOG4J_1 = "core/LogEvent.class".toUpperCase(Locale.ROOT);
    public static final String FILE_LOG4J_2 = "core/Appender.class".toUpperCase(Locale.ROOT);
    public static final String FILE_LOG4J_3 = "core/Filter.class".toUpperCase(Locale.ROOT);
    public static final String FILE_LOG4J_4 = "core/Layout.class".toUpperCase(Locale.ROOT);
    public static final String FILE_LOG4J_5 = "core/LoggerContext.class".toUpperCase(Locale.ROOT);
    public static final String FILE_LOG4J_6 = "core/lookup/Interpolator.class".toUpperCase(Locale.ROOT);
    public static final String FILE_LOG4J_6_DIGEST_V_2_3_1 = "dd5ef015e3e04f25fed12275dd93769a9a5817df";
    public static final String FILE_LOG4J_6_DIGEST_V_2_12_2 = "18b557e04c73edbb53fbfc7ebb954771fde4eeba";
    public static final String FILE_LOG4J_6_DIGEST_V_2_12_3 = "77fac2ed9e73836ebc2a8eb7c9c53033c08d9ae0";
    public static final String FILE_LOG4J_6_DIGEST_V_2_16_0 = "a79a30bba6419a518c46e30c93bc5ea5c47c34d7";
    public static final String FILE_LOG4J_6_DIGEST_V_2_17_0 = "7940b6cb1ff6fd7c5ec0419524dd3a0840a5ab86";
    public static final String FILE_LOG4J_2_10 = "appender/nosql/NoSqlAppender.class".toUpperCase(Locale.ROOT);
    public static final String FILE_LOG4J_VULNERABLE = "JndiLookup.class".toUpperCase(Locale.ROOT);
    public static final String FILE_LOG4J1_JMS_APPENDER = "JMSAppender.class".toUpperCase(Locale.ROOT);
    public static final String FILE_LOG4J_SAFE_CONDITION1 = "JndiManager.class".toUpperCase(Locale.ROOT);
    public static final String FILE_LOG4J_V1X = "org/apache/log4j/Logger.class".toUpperCase(Locale.ROOT);

    public static final List<String> CLASS_FILES = new ArrayList<>();
    public static final List<String> FILES_TO_READ = new ArrayList<>();
    public static final List<String> CLASS_FILES_NEED_HASHES = new ArrayList<>();

    static {
        CLASS_FILES.add(FILE_LOG4J_1);
        CLASS_FILES.add(FILE_LOG4J_2);
        CLASS_FILES.add(FILE_LOG4J_3);
        CLASS_FILES.add(FILE_LOG4J_4);
        CLASS_FILES.add(FILE_LOG4J_5);
        CLASS_FILES.add(FILE_LOG4J_6);
        CLASS_FILES.add(FILE_LOG4J_2_10);
        CLASS_FILES.add(FILE_LOG4J_VULNERABLE);
        CLASS_FILES.add(FILE_LOG4J1_JMS_APPENDER);
        CLASS_FILES.add(FILE_LOG4J_SAFE_CONDITION1);
        CLASS_FILES.add(FILE_LOG4J_V1X);

        CLASS_FILES_NEED_HASHES.add(FILE_LOG4J_6);

        FILES_TO_READ.addAll(CLASS_FILES);
        FILES_TO_READ.add("POM.PROPERTIES");

        //TODO: think of proper solution :)
        //ZIP_FILE_ENDINGS.add(".ZIP");
        ZIP_FILE_ENDINGS.add(".JAR");
        ZIP_FILE_ENDINGS.add(".JPI");
        ZIP_FILE_ENDINGS.add(".HPI");
        ZIP_FILE_ENDINGS.add(".WAR");
        ZIP_FILE_ENDINGS.add(".EAR");
        ZIP_FILE_ENDINGS.add(".AAR");
    }

    public static final String CONFIG_REGEX = "^javaadditionalopts[^=]*=[^=]*-dlog4j2.formatmsgnolookups[^=]*=[^=]*true";

    protected static final byte[] IS_LOG4J_SAFE_CONDITION_V2 = Bytes.fromString("Invalid JNDI URI - {}");

    protected static final Set<String> RELEVANT_POM_PROPERTIES_PATHS = new HashSet<>();

    static {
        RELEVANT_POM_PROPERTIES_PATHS.add("LOG4J/");
        RELEVANT_POM_PROPERTIES_PATHS.add("LOG4J-CORE/");
    }
}
