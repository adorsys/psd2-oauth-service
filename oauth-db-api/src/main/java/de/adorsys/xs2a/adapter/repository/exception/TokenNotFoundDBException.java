package de.adorsys.xs2a.adapter.repository.exception;

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
