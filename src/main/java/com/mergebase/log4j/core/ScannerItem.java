package com.mergebase.log4j.core;

import com.mergebase.log4j.allowlist.AllowlistResult;
import com.mergebase.log4j.allowlist.Allowlists;
import com.mergebase.log4j.interpreter.ResultInterpreter;
import com.mergebase.log4j.utils.Bytes;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Properties;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.mergebase.log4j.core.ScanPaths.*;
import static com.mergebase.log4j.utils.Bytes.digest;

@Data
public class ScannerItem {

    private final ResultInterpreter interpreter = new ResultInterpreter();

    private final File fileArchive;
    @Getter
    private boolean scannerResult;

    private Logger logger = Logger.getLogger(ScannerItem.class.getSimpleName());
    private static final Integer MAX_SIZE_FOR_NON_ARCHIVE = 5000000;

    public ScannerItem(@NonNull File file) {
        fileArchive = file;
    }

    public void process(boolean isVerboseEnabled) {
        try(JarZipper zipper = new JarZipper(fileArchive)) {
            scannerResult = findLog4jDependencies(fileArchive.getPath(), zipper, isVerboseEnabled);
        }
        catch(Exception ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    private boolean findLog4jDependencies(final String zipPath, final Zipper zipper, boolean verbose) throws IOException {

        JarInputStream zin = null;
        boolean depsFound = false;
        boolean isZip = false;
        ZipEntry ze = null;

        try {
        	String upperPath = zipPath.toUpperCase(Locale.ROOT);
            boolean isArchive = ScanPaths.ZIP_FILE_ENDINGS.stream().anyMatch(upperPath::endsWith);
        	if (isArchive) {
        		zin = zipper.getFreshZipStream();
        	} else {
                return false;
            }

        } catch (Exception e) {
            if(verbose) {
                logger.log(Level.SEVERE, zipPath, e);
            }
            System.out.printf("%s failed to analyse%n", zipPath);
        }

        if (verbose) {
            logger.log(Level.INFO, "-- Examining {0}...", zipPath);
        }

        JarResult jarResult = new JarResult();

        while((ze = getZipEntryFromStream(zin)) != null) {

            if (ze.isDirectory()) {
                continue;
            }

            final String path = ze.getName();
            final String fullPath = zipPath + "!/" + path;
            isZip = true;

            try {
                byte[] streamedBytes = null;

                String zipEntryPath = path.toUpperCase(Locale.ENGLISH);
                boolean isArchive = ScanPaths.ZIP_FILE_ENDINGS.stream().anyMatch(zipEntryPath::endsWith);
                if (isArchive) {
                    try(JarZipper nestedZipper = new JarZipper(zin)) {
                        depsFound |= findLog4jDependencies(fullPath, nestedZipper, verbose);
                    }
                } else {
                    if(fileContentCheckNeeded(zipEntryPath)) {
                        streamedBytes = Bytes.streamToBytes(zin, false);
                    }
                    checkClassFiles(zipEntryPath, fullPath, streamedBytes, jarResult);
                    checkMetaFiles(zipEntryPath, fullPath, streamedBytes, jarResult);
                }
            }
            catch (IOException ex) {
                logger.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }

        if (!isZip) {
            handleNonArchiveFile(zipPath);
        }

        String result = interpreter.interpret(jarResult);
        if(result != null && !result.isEmpty()) {
            System.out.print(result);
            return true;
        }
        return depsFound;
    }

    private boolean fileContentCheckNeeded(String zipEntryPath) {
        return FILES_TO_READ.stream().anyMatch(zipEntryPath::endsWith);
    }

    private void checkClassFiles(@NonNull String entryPath,
                                 @NonNull String fullPath,
                                 byte[] fileBytes,
                                 @NonNull JarResult jarResult) {
        boolean isInteresting = CLASS_FILES.stream().anyMatch(entryPath::endsWith);
        if (isInteresting) {
            boolean containsInvalidJNDIUri = entryPath.endsWith(ScanPaths.FILE_LOG4J_SAFE_CONDITION1)
                    && containsMatch(fileBytes);
            AllowlistResult allowlistResult = Allowlists.getInstance().isOnAllowlist(fileBytes);

            JarResult.ClassResult.ClassResultBuilder builder = JarResult.ClassResult.builder()
                    .normalizedPath(entryPath)
                    .fullPath(fullPath)
                    .allowlisted(allowlistResult.isAllowed())
                    .allowlistName(allowlistResult.getAllowlist())
                    .containsInvalidJNDIUri(containsInvalidJNDIUri);

            if(CLASS_FILES_NEED_HASHES.stream().anyMatch(entryPath::endsWith)) {
                builder.hash(digest(fileBytes));
            }

            jarResult.getClassResults().add(builder.build());
        }
    }



    private void checkMetaFiles(@NonNull String zipEntryPath, @NonNull String fullPath,
                                byte[] streamedBytes, @NonNull JarResult jarResult) {
        boolean isRelevantPomPropertiesFile = zipEntryPath.endsWith("pom.properties".toUpperCase(Locale.ROOT))
                && zipEntryPath.contains("log4j".toUpperCase(Locale.ROOT));

        if(isRelevantPomPropertiesFile) {
            if(streamedBytes == null) {
                throw new IllegalStateException("streamed bytes shouldn't be null at this point");
            }
            try {
                Properties properties = new Properties();
                properties.load(new ByteArrayInputStream(streamedBytes));
                String version = properties.getProperty("version");
                String library = properties.getProperty("groupId") + ":" + properties.getProperty("artifactId");
                jarResult.getMetaResults().add(new JarResult.MetaResult(fullPath, library, version));
            } catch (IOException e) {
                logger.log(Level.WARNING, "Failed to parse pom.properties: " + fullPath, e);
            }
        }
    }


    private void handleNonArchiveFile(@NonNull String zipPath) throws IOException {
        File f = new File(zipPath);
        if (f.canRead() && f.length() < MAX_SIZE_FOR_NON_ARCHIVE) {

            try(FileInputStream fin = new FileInputStream(f)) {
                byte[] bytes = Bytes.streamToBytes(fin);
                containsMatch(bytes);
            }
        }
    }

    private boolean containsMatch(byte[] bytes) {
        if(bytes == null) {
            return false;
        }
        for (byte[] needle : Collections.singleton(ScanPaths.IS_LOG4J_SAFE_CONDITION_V2)) {
            int matched = Bytes.kmp(bytes, needle);
            if (matched >= 0) {
                return true;
            }
        }
        return false;
    }

    private ZipEntry getZipEntryFromStream(ZipInputStream stream) {

        if(stream == null) {
            return null;
        }

        ZipEntry zipEntry = null;

        try{
            zipEntry = stream.getNextEntry();
        }
        catch (IOException ex) {
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }
        return zipEntry;
    }
}
