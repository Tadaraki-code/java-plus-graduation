package ru.yandex.practicum.core.interaction.event.dto.parameters;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.core.interaction.event.enums.SortingEvents;

import java.time.LocalDateTime;
import java.util.List;

@Builder(toBuilder = true)
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SearchPublicEventsParameters {
    String text;
    List<Long> categories;
    Boolean paid;
    LocalDateTime rangeStart;
    LocalDateTime rangeEnd;
    Boolean onlyAvailable;
    SortingEvents sort;
    Integer from;
    Integer size;
}
