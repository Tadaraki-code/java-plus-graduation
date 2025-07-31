package ru.yandex.practicum.core.interaction.error.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import feign.FeignException;
import feign.Request;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class ClientApiException extends FeignException {
    private final String status;
    private final String reason;
    private final String message;
    private final List<String> errors;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime timestamp;

    public ClientApiException(String message, String reason, int httpStatus,
                              String status, List<String> errors, LocalDateTime timestamp, Request request) {
        super(httpStatus, message, request);
        this.message = message;
        this.reason = reason;
        this.status = status;
        this.errors = errors;
        this.timestamp = timestamp;
    }
}
