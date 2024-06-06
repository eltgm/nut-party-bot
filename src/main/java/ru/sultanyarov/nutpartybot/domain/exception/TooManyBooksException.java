package ru.sultanyarov.nutpartybot.domain.exception;

public class TooManyBooksException extends ServiceException {

    public TooManyBooksException(String message) {
        super(message);
    }

    public TooManyBooksException(String messagePattern, Object... args) {
        super(messagePattern, args);
    }

    public TooManyBooksException(Throwable cause, String messagePattern, Object... args) {
        super(cause, messagePattern, args);
    }
}