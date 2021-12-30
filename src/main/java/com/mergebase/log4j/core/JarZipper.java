package com.mergebase.log4j.core;

import com.mergebase.log4j.exceptions.ZipStreamRuntimeException;
import com.mergebase.log4j.utils.Util;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.*;
import java.util.jar.JarInputStream;
import java.util.zip.ZipInputStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JarZipper implements Zipper {

    private FileInputStream fileStream;
    private BufferedInputStream binStream;
    private JarInputStream jarInputStream;
    private JarInputStream parentStream;

    private File zipFile;
    
    public JarZipper(File file) {
        zipFile = file;
    }

    public JarZipper(JarInputStream stream) {
        this.parentStream = stream;
    }

    @Override
    public JarInputStream getFreshZipStream() throws RuntimeException {

        try {
            if(zipFile != null) {
                fileStream = new FileInputStream(zipFile);
                binStream = new BufferedInputStream(fileStream);
                jarInputStream = new JarInputStream(binStream, false);
            } else {
                jarInputStream = new JarInputStream(parentStream, false);
            }
        }
        catch(IOException ex) {
            throw new ZipStreamRuntimeException(ex.getMessage(), ex);
        }
        return jarInputStream;
    }

    @Override
    public void close() {
        if(zipFile != null) {
            Util.close(fileStream, binStream, jarInputStream);
        } 
        // we don't want to close the jarInputStream in this case, as this would close the underlying stream
    }
    
}
