package ru.yandex.practicum.core.interaction.decoders;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.core.interaction.error.ApiError;
import ru.yandex.practicum.core.interaction.error.exception.ClientApiException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Component
public class CommonFeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    public CommonFeignErrorDecoder() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            int httpCode = response.status();
            if (response.body() == null) {
                log.error("Пустое тела ответа из метода {}", methodKey);
                return new ClientApiException(
                        "EMPTY_RESPONSE",
                        "Server returned an empty response body",
                        httpCode,
                        HttpStatus.valueOf(httpCode).toString(),
                        Collections.singletonList("No response body provided"),
                        LocalDateTime.now(),
                        response.request()
                );
            }

            ApiError errorResponse = objectMapper.readValue(
                    response.body().asInputStream(), ApiError.class);

            return new ClientApiException(
                    errorResponse.getMessage(),
                    errorResponse.getReason(),
                    httpCode,
                    HttpStatus.valueOf(httpCode).toString(),
                    errorResponse.getErrors(),
                    errorResponse.getTimestamp(),
                    response.request());

        } catch (IOException e) {
            log.error("Ошибка парсинга тела ответа от Feign", e);
            int httpCode = response.status();
            return new ClientApiException(
                    "PARSING_ERROR",
                    "Failed to parse server response: " + e.getMessage(),
                    httpCode,
                    HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                    Collections.singletonList(e.getMessage()),
                    LocalDateTime.now(),
                    response.request()
            );
        }

    }
}
