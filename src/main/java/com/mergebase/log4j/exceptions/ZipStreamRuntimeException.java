package com.mergebase.log4j.exceptions;

public class ZipStreamRuntimeException extends RuntimeException {
    public ZipStreamRuntimeException(String msg) {
        super(msg);
    }

    public ZipStreamRuntimeException(String msg, Throwable tx) {
        super(msg, tx);
    }
}
