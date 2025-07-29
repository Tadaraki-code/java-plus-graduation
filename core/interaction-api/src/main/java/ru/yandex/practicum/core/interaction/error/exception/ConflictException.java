package ru.yandex.practicum.core.interaction.error.exception;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
