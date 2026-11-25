package com.bigun.kzp;

/**
 * Reports a failure of storing, formatting, or restoring data and keeps its cause.
 */
public class DataStorageException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a message.
     *
     * @param message failed operation
     */
    public DataStorageException(String message) {
        super(message);
    }

    /**
     * Creates an exception with a message and the original cause.
     *
     * @param message failed operation
     * @param cause original failure
     */
    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
