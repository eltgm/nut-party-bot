package ru.sultanyarov.nutpartybot.domain.exception;

public class NotFoundException extends ServiceException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String messagePattern, Object... args) {
        super(messagePattern, args);
    }

    public NotFoundException(Throwable cause, String messagePattern, Object... args) {
        super(cause, messagePattern, args);
    }

}
