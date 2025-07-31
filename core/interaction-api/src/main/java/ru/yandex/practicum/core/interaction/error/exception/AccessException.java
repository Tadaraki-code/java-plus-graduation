package ru.yandex.practicum.core.interaction.error.exception;

public class AccessException extends RuntimeException {
    public AccessException(String message) {
        super(message);
    }
}