package com.mergebase.log4j.core;

import com.mergebase.log4j.comparators.FileNameComparator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileAnalyzer {
    
    private File rootFile;
    private Logger logger = Logger.getLogger(FileAnalyzer.class.getSimpleName());
    private boolean verbose = false;

    @Getter
    private boolean overallScanResult = false;

    public FileAnalyzer(File rootFile) {
        this.rootFile = rootFile;
    }

    public FileAnalyzer enableVerboseMode() {
        verbose = true;
        return this;
    }

    public void scan() {
        walk(rootFile);
    }

    private void processPath(File file) {
        if(!file.canRead()) {
            return;
        }
        try {
            ScannerItem item = new ScannerItem(file);
            item.process(verbose);
            overallScanResult |= item.isScannerResult();
        } catch (Exception e) {
            System.out.println(file.getAbsolutePath() + " failed to analyse");
            if(verbose) {
                logger.log(Level.SEVERE, String.format("Unable to process file %s", file.getAbsolutePath()), e);
            }
        }
    }

    private void walk(File file) {
        boolean isSymlink = Files.isSymbolicLink(file.toPath());
        boolean cannotRead = !file.canRead();

        if (isSymlink || cannotRead) {
            return;
        }

        if (file.isDirectory()) {
            File[] fileList = file.listFiles();
            if (fileList != null) {
                Arrays.sort(fileList, new FileNameComparator());
                for (File item : fileList) {
                    walk(item);
                }
            }
        } else {
            processPath(file);
        }
    }
}
