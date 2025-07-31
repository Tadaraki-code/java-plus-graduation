package ru.yandex.practicum.core.interaction.event.dto.parameters;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.core.interaction.event.dto.UpdateEventUserRequest;


@Builder(toBuilder = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventParameters {
    Long userId;
    Long eventId;
    UpdateEventUserRequest updateEventUserRequest;
}
