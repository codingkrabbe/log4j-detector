package com.mergebase.log4j.core;

import java.io.Closeable;
import java.util.jar.JarInputStream;


/**
 * An interface that allows us to re-read a ZipInputStream as many times as we want.
 */
public interface Zipper extends Closeable, AutoCloseable {
    JarInputStream getFreshZipStream();
    void close();
}

