package de.adorsys.psd2.oauth.repository.exception;

public class TokenNotFoundDBException extends Exception {

    public TokenNotFoundDBException() {
    }

    public TokenNotFoundDBException(String message) {
        super(message);
    }

    public TokenNotFoundDBException(String message, Throwable cause) {
        super(message, cause);
    }
}
