package com.redrock.jade.shared.dao;

/**
 * Copyright RedRock 2013-14
 */
public final class DocumentException extends Exception {

    public DocumentException(String message) {
        super(message);
    }

    public DocumentException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
