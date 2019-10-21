package de.adorsys.psd2.oauth.service.exception;

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
