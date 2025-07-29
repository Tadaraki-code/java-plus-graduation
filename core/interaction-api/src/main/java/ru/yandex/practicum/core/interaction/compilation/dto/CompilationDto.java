package ru.yandex.practicum.core.interaction.compilation.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.core.interaction.event.dto.EventShortDto;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompilationDto {

    Long id;

    String title;

    boolean pinned;

    List<EventShortDto> events;

}