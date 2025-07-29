package ru.yandex.practicum.core.interaction.error.exception;


public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}