package ru.yandex.practicum.core.interaction.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.core.interaction.event.enums.UserEventAction;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventUserRequest {
    @Size(min = 3, max = 120)
    String title;

    @Size(min = 20, max = 7000)
    String description;

    @Size(min = 20, max = 2000)
    String annotation;

    Long category;
    LocationDto location;
    Boolean requestModeration;
    Boolean paid;
    Integer participantLimit;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;
    UserEventAction stateAction;
}