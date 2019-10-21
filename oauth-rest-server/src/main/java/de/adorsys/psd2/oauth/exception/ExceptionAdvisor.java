package de.adorsys.psd2.oauth.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ExceptionAdvisor {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvisor.class);
    private static final String MESSAGE = "message";
    private static final String DEV_MESSAGE = "devMessage";
    private static final String CODE = "code";
    private static final String DATE_TIME = "dateTime";

    @ExceptionHandler(RestException.class)
    public ResponseEntity<Map> handleRestException(RestException ex) {
        logger.error(ex.getMessage(), ex);
        Map<String, String> body = getHandlerContent(ex.getCode(), ex.getMessage(), ex.devMessage);
        return new ResponseEntity<>(body, ex.getStatus());
    }

    private Map<String, String> getHandlerContent(HttpStatus status, String message, String devMessage) {
        return getHandlerContent(String.valueOf(status.value()), message, devMessage);
    }

    private Map<String, String> getHandlerContent(String code, String message, String devMessage) {
        Map<String, String> error = new HashMap<>();
        error.put(CODE, code);
        error.put(MESSAGE, message);
        error.put(DEV_MESSAGE, devMessage);
        error.put(DATE_TIME, LocalDateTime.now().toString());
        return error;
    }
}
