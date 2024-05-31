package ru.sultanyarov.nutpartybot.domain.exception;

import org.slf4j.helpers.MessageFormatter;

public class ServiceException extends RuntimeException {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String messagePattern, Object... args) {
        super(getMessage(messagePattern, args));
    }

    public ServiceException(Throwable cause, String messagePattern, Object... args) {
        super(getMessage(messagePattern, args), cause);
    }


    private static String getMessage(String messagePattern, Object[] args) {
        return MessageFormatter.arrayFormat(messagePattern, args).getMessage();
    }

}
