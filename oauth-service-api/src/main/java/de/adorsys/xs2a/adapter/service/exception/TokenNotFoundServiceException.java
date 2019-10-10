package de.adorsys.xs2a.adapter.service.exception;

public class TokenNotFoundServiceException extends Exception {

    public TokenNotFoundServiceException() {
    }

    public TokenNotFoundServiceException(String message) {
        super(message);
    }

    public TokenNotFoundServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
