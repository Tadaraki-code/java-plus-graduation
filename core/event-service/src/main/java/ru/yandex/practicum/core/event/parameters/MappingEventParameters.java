package ru.yandex.practicum.core.event.parameters;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.core.event.model.Event;
import ru.yandex.practicum.core.interaction.category.dto.CategoryDto;
import ru.yandex.practicum.core.interaction.comments.dto.CommentShortDto;
import ru.yandex.practicum.core.interaction.user.dto.UserShortDto;

import java.util.List;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MappingEventParameters {
    Event event;
    CategoryDto categoryDto;
    UserShortDto initiator;
    Long confirmedRequests;
    Long views;
    List<CommentShortDto> comments;
}
